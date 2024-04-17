package multiverse.utils;

import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.json.QuiltRelease;

public class VersionComparator {
    public static int compare(CosmicReachVersion o1, CosmicReachVersion o2) {
        return compare(o1.getVersion(), o2.getVersion());
    }

    public static int compare(QuiltRelease quiltRelease, QuiltRelease quiltRelease1) {
        return compare(quiltRelease.getVersionNumber(), quiltRelease1.getVersionNumber());
    }

    public static int compare(String o1, String o2) {
        // Split version numbers into parts
        String[] v1Parts = o1.split("\\.");
        String[] v2Parts = o2.split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length && !v1Parts[i].replaceAll("\\D+", "").isEmpty() ? Integer.parseInt(v1Parts[i].replaceAll("\\D+", "")) : 0;
            int v2Part = i < v2Parts.length && !v2Parts[i].replaceAll("\\D+", "").isEmpty() ? Integer.parseInt(v2Parts[i].replaceAll("\\D+", "")) : 0;
            if (v1Part < v2Part)
                return 1;
            if (v1Part > v2Part)
                return -1;
        }

        // If versions are identical, compare trailing non-digit characters
        String v1Suffix = v1Parts[v1Parts.length - 1].replaceAll("\\d+", "");
        String v2Suffix = v2Parts[v2Parts.length - 1].replaceAll("\\d+", "");
        return v2Suffix.compareTo(v1Suffix);
    }
}
