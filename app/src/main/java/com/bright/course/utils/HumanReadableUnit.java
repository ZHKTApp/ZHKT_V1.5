package com.bright.course.utils;

/**
 * Created by jinbangzhu on 10/20/15.
 */
public class HumanReadableUnit {
    public static String ByteWithUnitSuffixes(long bytes) {
        long kb = bytes / 1024;
        long m = kb / 1024;
        long g = m / 1024;

        if (g > 0)
            return g + "G";
        if (m > 0)
            return m + "M";
        if (kb > 0)
            return kb + "KB";

        return bytes + "B";
    }

    public static String KBWithUnitSuffixes(long kb) {
        long m = kb / 1024;
        long g = m / 1024;

        if (g > 0)
            return g + "G";
        if (m > 0)
            return m + "M";

        return kb + "KB";
    }
}
