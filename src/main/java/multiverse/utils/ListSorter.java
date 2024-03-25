package multiverse.utils;

import multiverse.json.Builds;
import multiverse.json.QuiltRelease;

public class ListSorter {
    public static int compare(Builds.Build o1, Builds.Build o2) {
        // Split version numbers into parts
        String[] parts1 = o1.getUserVersion().split("\\.");
        String[] parts2 = o2.getUserVersion().split("\\.");

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

    public static int compare(QuiltRelease quiltRelease, QuiltRelease quiltRelease1) {
        // Split version numbers into parts
        String[] parts1 = quiltRelease.getTagName().split("\\.");
        String[] parts2 = quiltRelease1.getTagName().split("\\.");

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
