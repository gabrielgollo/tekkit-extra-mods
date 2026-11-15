import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

final class ClassicS3WorldStore {
    private static final String EMPTY_HASH = hex(sha256(new byte[0]));
    private final ClassicWorldSyncConfig config;
    private final String prefix;
    private final URI endpoint;

    ClassicS3WorldStore(ClassicWorldSyncConfig config, String minecraftUser) throws IOException {
        this.config = config;
        if (!config.isConfigured()) throw new IOException("S3 connection is not configured");
        try {
            endpoint = URI.create(config.endpoint.trim());
            if (!("http".equalsIgnoreCase(endpoint.getScheme()) || "https".equalsIgnoreCase(endpoint.getScheme()))
                    || endpoint.getHost() == null || endpoint.getRawQuery() != null || endpoint.getRawFragment() != null) {
                throw new IllegalArgumentException("Endpoint must be an HTTP or HTTPS URL without a query or fragment");
            }
        } catch (IllegalArgumentException error) {
            throw new IOException("Invalid S3 endpoint URL", error);
        }
        String user = config.userId.isEmpty() ? minecraftUser : config.userId;
        prefix = "tekkit-classic/1.2.5/" + userSegment(user) + "/worlds/";
    }

    List<RemoteWorld> listWorlds() throws Exception {
        ArrayList<RemoteWorld> worlds = new ArrayList<RemoteWorld>();
        String token = null;
        do {
            Map<String, String> query = new HashMap<String, String>();
            query.put("list-type", "2");
            query.put("prefix", prefix);
            if (token != null) query.put("continuation-token", token);
            HttpURLConnection connection = request("GET", "", query, Collections.<String, String>emptyMap(), null);
            byte[] response;
            try { response = readResponse(connection); }
            finally { connection.disconnect(); }
            Document document = parseXml(response);
            NodeList contents = document.getElementsByTagNameNS("*", "Contents");
            for (int i = 0; i < contents.getLength(); i++) {
                Element content = (Element)contents.item(i);
                String key = childText(content, "Key");
                if (key == null || !key.startsWith(prefix) || !key.endsWith(".zip")) continue;
                String encoded = key.substring(prefix.length(), key.length() - 4);
                try {
                    String name = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
                    if (!safeWorldName(name)) continue;
                    String modified = childText(content, "LastModified");
                    long timestamp = modified == null ? 0L : parseTimestamp(modified);
                    worlds.add(new RemoteWorld(name, key, timestamp));
                } catch (IllegalArgumentException ignored) { }
            }
            String truncated = firstText(document, "IsTruncated");
            token = "true".equalsIgnoreCase(truncated) ? firstText(document, "NextContinuationToken") : null;
        } while (token != null && !token.isEmpty());
        return worlds;
    }

    void upload(File archive, String worldName, long localModified) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("x-amz-meta-local-modified", String.valueOf(localModified));
        HttpURLConnection connection = request("PUT", key(worldName), Collections.<String, String>emptyMap(), headers, archive);
        try {
            int response = connection.getResponseCode();
            if (response < 200 || response >= 300) throw responseError(connection, response);
            closeResponse(connection);
        } finally { connection.disconnect(); }
    }

    void download(RemoteWorld world, File archive) throws IOException {
        HttpURLConnection connection = request("GET", world.key, Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), null);
        try {
            int response = connection.getResponseCode();
            if (response < 200 || response >= 300) throw responseError(connection, response);
            try (InputStream input = connection.getInputStream(); OutputStream output = new java.io.FileOutputStream(archive)) {
                copy(input, output);
            }
        } finally { connection.disconnect(); }
    }

    long remoteLocalModified(RemoteWorld world) throws IOException {
        HttpURLConnection connection = request("HEAD", world.key, Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), null);
        try {
            int response = connection.getResponseCode();
            if (response < 200 || response >= 300) throw responseError(connection, response);
            String value = connection.getHeaderField("x-amz-meta-local-modified");
            if (value != null) {
                try { return Long.parseLong(value); }
                catch (NumberFormatException ignored) { }
            }
            String lastModified = connection.getHeaderField("Last-Modified");
            return lastModified == null ? world.modified : parseHttpTimestamp(lastModified, world.modified);
        } finally { connection.disconnect(); }
    }

    private String key(String name) {
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(name.getBytes(StandardCharsets.UTF_8)) + ".zip";
    }

    private HttpURLConnection request(String method, String key, Map<String, String> query,
            Map<String, String> extraHeaders, File body) throws IOException {
        String path = requestPath(key);
        String canonicalQuery = canonicalQuery(query);
        String target = endpoint.getScheme() + "://" + endpoint.getRawAuthority() + path
                + (canonicalQuery.isEmpty() ? "" : "?" + canonicalQuery);
        URL url = new URL(target);
        String payloadHash = body == null ? EMPTY_HASH : hashFile(body);
        String timestamp = timestamp("yyyyMMdd'T'HHmmss'Z'", new Date());
        String date = timestamp.substring(0, 8);
        String host = endpoint.getHost().toLowerCase(Locale.ROOT);
        int port = endpoint.getPort();
        if (port >= 0 && !("http".equalsIgnoreCase(endpoint.getScheme()) && port == 80)
                && !("https".equalsIgnoreCase(endpoint.getScheme()) && port == 443)) host += ":" + port;

        TreeHeaders headers = new TreeHeaders();
        headers.put("host", host);
        headers.put("x-amz-content-sha256", payloadHash);
        headers.put("x-amz-date", timestamp);
        for (Map.Entry<String, String> entry : extraHeaders.entrySet()) headers.put(entry.getKey().toLowerCase(Locale.ROOT), normalizeHeader(entry.getValue()));
        String canonicalHeaders = headers.canonical();
        String signedHeaders = headers.signedHeaders();
        String canonicalRequest = method + "\n" + path + "\n" + canonicalQuery + "\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash;
        String scope = date + "/" + config.region + "/s3/aws4_request";
        String stringToSign = "AWS4-HMAC-SHA256\n" + timestamp + "\n" + scope + "\n" + hex(sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        byte[] signingKey = signingKey(config.secretKey, date, config.region);
        String signature = hex(hmac(signingKey, stringToSign));

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setUseCaches(false);
        connection.setRequestProperty("x-amz-date", timestamp);
        connection.setRequestProperty("x-amz-content-sha256", payloadHash);
        for (Map.Entry<String, String> entry : extraHeaders.entrySet()) connection.setRequestProperty(entry.getKey(), entry.getValue());
        connection.setRequestProperty("Authorization", "AWS4-HMAC-SHA256 Credential=" + config.accessKey + "/" + scope
                + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature);
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/zip");
            connection.setFixedLengthStreamingMode(body.length());
            try (InputStream input = new FileInputStream(body); OutputStream output = connection.getOutputStream()) { copy(input, output); }
        }
        return connection;
    }

    private String requestPath(String key) throws IOException {
        String base = endpoint.getRawPath();
        if (base == null || base.isEmpty()) base = "";
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        StringBuilder path = new StringBuilder(base).append('/').append(encodePathSegment(config.bucket));
        if (key != null && !key.isEmpty()) {
            for (String segment : key.split("/", -1)) path.append('/').append(encodePathSegment(segment));
        }
        return path.toString();
    }

    private static String canonicalQuery(Map<String, String> query) throws IOException {
        ArrayList<String> parts = new ArrayList<String>();
        for (Map.Entry<String, String> entry : query.entrySet()) {
            parts.add(encodeQuery(entry.getKey()) + "=" + encodeQuery(entry.getValue()));
        }
        Collections.sort(parts);
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) result.append('&');
            result.append(part);
        }
        return result.toString();
    }

    private static String encodeQuery(String value) throws IOException {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("%7E", "~");
    }

    private static String encodePathSegment(String value) throws IOException {
        return encodeQuery(value).replace("%2F", "%252F");
    }

    private byte[] signingKey(String secret, String date, String region) throws IOException {
        byte[] key = hmac(("AWS4" + secret).getBytes(StandardCharsets.UTF_8), date);
        key = hmac(key, region);
        key = hmac(key, "s3");
        return hmac(key, "aws4_request");
    }

    private static byte[] hmac(byte[] key, String value) throws IOException { return hmac(key, value.getBytes(StandardCharsets.UTF_8)); }

    private static byte[] hmac(byte[] key, byte[] value) throws IOException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(value);
        } catch (Exception error) { throw new IOException("Could not sign S3 request", error); }
    }

    private static byte[] sha256(byte[] bytes) {
        try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }

    private static String hashFile(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new FileInputStream(file)) {
                byte[] buffer = new byte[32768];
                int count;
                while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
            }
            return hex(digest.digest());
        } catch (IOException error) { throw error; }
        catch (Exception error) { throw new IOException("Could not hash world archive", error); }
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) result.append(String.format(Locale.ROOT, "%02x", value & 255));
        return result.toString();
    }

    private static String timestamp(String pattern, Date date) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }

    private static long parseTimestamp(String value) {
        try { return javax.xml.bind.DatatypeConverter.parseDateTime(value).getTime().getTime(); }
        catch (Throwable ignored) { return 0L; }
    }

    private static long parseHttpTimestamp(String value, long fallback) {
        try { return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(value).getTime(); }
        catch (Exception ignored) { return fallback; }
    }

    private static String userSegment(String value) {
        if (value != null && value.matches("[A-Za-z0-9._-]{1,64}")) return value;
        String user = value == null ? "unknown" : value;
        return "b64-" + Base64.getUrlEncoder().withoutPadding().encodeToString(user.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean safeWorldName(String name) {
        return name != null && !name.isEmpty() && !name.equals(".") && !name.equals("..")
                && name.indexOf('/') < 0 && name.indexOf('\\') < 0 && name.indexOf('\0') < 0;
    }

    private static Document parseXml(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) { }
        try { factory.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Exception ignored) { }
        try { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Exception ignored) { }
        return factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(bytes));
    }

    private static String childText(Element parent, String name) {
        NodeList nodes = parent.getElementsByTagNameNS("*", name);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    private static String firstText(Document document, String name) {
        NodeList nodes = document.getElementsByTagNameNS("*", name);
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent();
    }

    private static byte[] readResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) throw responseError(connection, status);
        try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            copy(input, output);
            return output.toByteArray();
        }
    }

    private static IOException responseError(HttpURLConnection connection, int status) {
        String detail = "";
        try {
            InputStream input = connection.getErrorStream();
            if (input != null) try (InputStream error = input) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int count = error.read(buffer);
                if (count > 0) detail = new String(buffer, 0, count, StandardCharsets.UTF_8).replaceAll("<[^>]+>", " ").trim();
            }
        } catch (Exception ignored) { }
        return new IOException("S3 request failed with HTTP " + status + (detail.isEmpty() ? "" : ": " + detail));
    }

    private static void closeResponse(HttpURLConnection connection) throws IOException {
        InputStream input = connection.getInputStream();
        if (input != null) input.close();
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[32768];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
    }

    static final class RemoteWorld {
        final String name;
        final String key;
        final long modified;
        RemoteWorld(String name, String key, long modified) {
            this.name = name;
            this.key = key;
            this.modified = modified;
        }
    }

    private static final class TreeHeaders extends java.util.TreeMap<String, String> {
        private static final long serialVersionUID = 1L;
        String canonical() {
            StringBuilder value = new StringBuilder();
            for (Map.Entry<String, String> entry : entrySet()) value.append(entry.getKey()).append(':').append(normalizeHeader(entry.getValue())).append('\n');
            return value.toString();
        }
        String signedHeaders() {
            StringBuilder value = new StringBuilder();
            for (String key : keySet()) {
                if (value.length() > 0) value.append(';');
                value.append(key);
            }
            return value.toString();
        }
    }

    private static String normalizeHeader(String value) { return value == null ? "" : value.trim().replaceAll("\\s+", " "); }
}
