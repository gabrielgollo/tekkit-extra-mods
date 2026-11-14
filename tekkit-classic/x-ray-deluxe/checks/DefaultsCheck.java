import java.nio.file.*;
public class DefaultsCheck {
 public static void main(String[] args) throws Exception {
  Path file=Paths.get("config/x-ray-deluxe-defaults.json"); Files.createDirectories(file.getParent());
  Files.write(file,"{\"oreIds\":[56,247,56]}".getBytes("UTF-8"));
  int[] ids=mod_XRayDeluxe.defaultOreIds();
  if(ids.length!=2||ids[0]!=56||ids[1]!=247) throw new AssertionError("custom defaults");
  Files.write(file,"{\"oreIds\":[]}".getBytes("UTF-8"));
  if(mod_XRayDeluxe.defaultOreIds().length!=0) throw new AssertionError("empty defaults");
  Files.write(file,"{\"oreIds\":[73]}".getBytes("UTF-8"));
  if(mod_XRayDeluxe.defaultOreIds().length!=2) throw new AssertionError("redstone pair");
  Files.write(file,"{ broken".getBytes("UTF-8"));
  if(mod_XRayDeluxe.defaultOreIds().length!=12) throw new AssertionError("safe fallback");
  System.out.println("PASS: JSON defaults, duplicates, empty defaults, redstone pair, invalid JSON fallback");
 }
}
