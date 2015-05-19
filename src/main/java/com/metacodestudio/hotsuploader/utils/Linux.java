package com.metacodestudio.hotsuploader.utils;

import java.io.IOException;

public class Linux {
  public static boolean browse(String url) {
    try {
      Process p = Runtime.getRuntime().exec("xdg-open " + url);
      if (p == null) return false;

          int retval = p.exitValue();
          if (retval == 0) {
              System.err.println("Process ended immediately.");
              return false;
          } else {
              System.err.println("Process crashed.");
              return false;
          }        
    } catch (IOException e) {
        System.err.println("Error running command.");
        return false;
    }
}
}
