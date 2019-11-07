/**
 * @file UtilConfig.java
 */
package util;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;

/**
 * @since JavaSE-1.8
 */
public class UtilConfig {

   static File getWorkspaceDir() {
      URL url = Platform.getInstanceLocation().getURL();
      return new File(url.getPath());
   }

   public static void load() {
      File workspaceDir = getWorkspaceDir();
      System.setProperty("RUNTIME_PRJ_PATH", workspaceDir.getAbsolutePath());
   }
}
