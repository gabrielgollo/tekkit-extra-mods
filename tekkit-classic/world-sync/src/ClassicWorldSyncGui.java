final class ClassicWorldSyncGui extends vp {
    private final mod_WorldSync mod;
    private final boolean startup;
    private final String[] values = new String[6];
    private final String[] labels = {"Bucket endpoint URL", "Bucket name", "Region", "Access key ID", "Secret key", "Sync user ID"};
    private final String[] labelsPt = {"Endereco do bucket", "Nome do bucket", "Regiao", "Access key ID", "Secret key", "ID de sincronizacao"};
    private final boolean[] secret = {false, false, false, false, true, false};
    private int focused = -1;

    ClassicWorldSyncGui(mod_WorldSync mod, boolean startup) {
        this.mod = mod;
        this.startup = startup;
        ClassicWorldSyncConfig config = mod.config();
        values[0] = config == null ? "http://192.168.31.16:9006" : config.endpoint;
        values[1] = config == null ? "minecraft" : config.bucket;
        values[2] = config == null ? "us-east-1" : config.region;
        values[3] = config == null ? "" : config.accessKey;
        values[4] = "";
        values[5] = config == null ? "GabrielGollo" : config.userId;
    }

    @SuppressWarnings("unchecked")
    public void c() {
        s.clear();
        int width = Math.min(430, q - 16);
        int left = Math.max(8, (q - width) / 2);
        int top = Math.max(8, (r - 234) / 2);
        int buttonWidth = (width - 42) / 2;
        int right = left + width / 2 + 2;
        s.add(new abp(0, left + 15, top + 184, buttonWidth, 20, localized("Save and Sync", "Salvar e sincronizar")));
        s.add(new abp(1, right, top + 184, buttonWidth, 20, localized("Continue", "Continuar")));
        s.add(new abp(2, left + 15, top + 158, buttonWidth, 20, localized("Use local world", "Usar mundo local")));
        s.add(new abp(3, right, top + 158, buttonWidth, 20, localized("Use cloud world", "Usar mundo da nuvem")));
    }

    private void updateButtons() {
        for (Object item : s) {
            abp button = (abp)item;
            button.i = button.f < 2 || mod.conflict() != null;
            button.h = !mod.isBusy() && (button.f != 0 || mod.conflict() == null);
        }
    }

    protected void a(abp button) {
        if (!button.h) return;
        if (button.f == 0) saveAndSync();
        else if (button.f == 1) mod.returnToTitle();
        else mod.resolveConflict(button.f == 2);
    }

    public void a(int mouseX, int mouseY, float tick) {
        e(0);
        updateButtons();
        int left = Math.max(8, (q - Math.min(430, q - 16)) / 2);
        int width = Math.min(430, q - 16);
        int colGap = 12;
        int colWidth = (width - 30 - colGap) / 2;
        int right = left + 15 + colWidth + colGap;
        int top = Math.max(8, (r - 234) / 2);

        b(p.q, "gabrielgollo_World Sync", left + 12, top + 9, 0xFFFFFF);
        for (int i = 0; i < values.length; i++) {
            int x = (i % 2 == 0) ? left + 15 : right;
            int y = top + 36 + (i / 2) * 42;
            b(p.q, label(i), x, y, 0xCCCCCC);
            a(x, y + 12, x + colWidth, y + 30, focused == i ? 0xFF17202A : 0xFF10151B);
            String shown = secret[i] ? stars(values[i].length()) : values[i];
            int max = Math.max(1, (colWidth - 10) / 6);
            if (shown.length() > max) shown = shown.substring(shown.length() - max);
            b(p.q, shown, x + 5, y + 17, 0xFFFFFF);
            if (focused == i && (System.currentTimeMillis() / 500L) % 2L == 0L) {
                int cursor = x + 5 + p.q.a(shown);
                a(cursor, y + 16, cursor + 1, y + 27, 0xFFFFFFFF);
            }
        }
        if (mod.conflict() != null) {
            b(p.q, localized("Local and cloud copies differ: " + mod.conflict().remote.name,
                    "As copias local e remota diferem: " + mod.conflict().remote.name), left + 15, top + 211, 0xFFFF66);

        } else {
            String current = mod.status();
            int available = width - 30;
            if (p.q.a(current) > available) current = p.q.a(current, available);
            b(p.q, current, left + 15, top + 215, 0xFFD36A);
        }
        if (startup && mod.conflict() == null) b(p.q, localized("Configure here, then continue to the title screen.", "Configure aqui e depois continue ao menu inicial."), left + 15, top + 169, 0xBCC7D4);
        super.a(mouseX, mouseY, tick);
    }

    protected void a(int x, int y, int button) {
        updateButtons();
        super.a(x, y, button);
        if (button != 0 || mod.isBusy() || mod.conflict() != null) return;
        int left = Math.max(8, (q - Math.min(430, q - 16)) / 2);
        int width = Math.min(430, q - 16);
        int colGap = 12;
        int colWidth = (width - 30 - colGap) / 2;
        int right = left + 15 + colWidth + colGap;
        int top = Math.max(8, (r - 234) / 2);
        focused = -1;
        for (int i = 0; i < values.length; i++) {
            int x0 = (i % 2 == 0) ? left + 15 : right;
            int y0 = top + 48 + (i / 2) * 42;
            if (x >= x0 && x <= x0 + colWidth && y >= y0 && y <= y0 + 18) focused = i;
        }
    }

    protected void a(char character, int code) {
        if (code == 1) { mod.returnToTitle(); return; }
        if (focused < 0) return;
        String value = values[focused];
        if (code == 14) {
            if (!value.isEmpty()) values[focused] = value.substring(0, value.length() - 1);
        } else if (character >= 32 && character != 127 && value.length() < (focused == 4 ? 256 : 512)) {
            values[focused] = value + character;
        }
    }

    private void saveAndSync() {
        try {
            mod.updateConnection(values[0], values[1], values[2], values[3], values[4], values[5]);
            values[4] = "";
            mod.retrySync();
        } catch (Exception error) {
            System.err.println("[World Sync] Could not save settings: " + error.getMessage());
        }
    }

    private String label(int index) {
        return "pt_BR".equalsIgnoreCase(p.A.R) ? labelsPt[index] : labels[index];
    }

    private String localized(String en, String pt) {
        return "pt_BR".equalsIgnoreCase(p.A.R) ? pt : en;
    }

    private static String stars(int count) {
        StringBuilder result = new StringBuilder(count);
        for (int i = 0; i < count; i++) result.append('*');
        return result.toString();
    }
}
