package it.util;

import java.io.File;

public class Util {
    public static String getPath(File file) {
        try {
            String fileName = file.getName();
            String dir = file.getCanonicalPath();
            int dimFileName = fileName.length();
            int dimDir = dir.length();

            return dir.substring(0, dimDir - dimFileName);
        } catch (Exception ignored) {
        }
        return null;
    }
}
