package com.alumni.back.util;

public class StringUtil {
    public static String[] split(String str, String delimiter) {
        if (str == null || str.trim().isEmpty()) {
            return new String[0];
        }
        return str.split(delimiter);
    }
} 