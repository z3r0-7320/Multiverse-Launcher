package multiverse.utils;

import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.json.QuiltRelease;

public class ListSorter {
    public static int compare(CosmicReachVersion o1, CosmicReachVersion o2) {
        // Split version numbers into parts
        String[] v1Parts = o1.getVersion().split("\\.");
        String[] v2Parts = o2.getVersion().split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i].replaceAll("\\D+", "")) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i].replaceAll("\\D+", "")) : 0;
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

    public static int compare(QuiltRelease quiltRelease, QuiltRelease quiltRelease1) {
        // Split version numbers into parts
        String[] parts1 = quiltRelease.getVersionNumber().split("\\.");
        String[] parts2 = quiltRelease1.getVersionNumber().split("\\.");

        // Compare each part
        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            // Consider missing parts as 0
            int part1 = i < parts1.length && parts1[i].matches("\\d+") ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length && parts2[i].matches("\\d+") ? Integer.parseInt(parts2[i]) : 0;

            // Compare parts
            if (part1 > part2) {
                return -1;
            } else if (part1 < part2) {
                return 1;
            }
        }

        // If all parts are equal, the versions are equal
        return 0;
    }
}
