package multiverse.utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionComparator {
    private static final String regex = "([(\\[])([0-9a-zA-Z_\\-.,]*)([)\\]])";
    private static final Pattern pattern = Pattern.compile(regex);

    public static <T> int compare(T o1, T o2) {
        String[] v1Parts = o1.toString().split("\\.");
        String[] v2Parts = o2.toString().split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            String v1Part = i < v1Parts.length ? v1Parts[i] : "";
            String v2Part = i < v2Parts.length ? v2Parts[i] : "";

            String[] v1SubParts = v1Part.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            String[] v2SubParts = v2Part.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

            int subLength = Math.max(v1SubParts.length, v2SubParts.length);
            for (int j = 0; j < subLength; j++) {
                String v1SubPart = j < v1SubParts.length ? v1SubParts[j] : "";
                String v2SubPart = j < v2SubParts.length ? v2SubParts[j] : "";

                if (v1SubPart.matches("\\d+") && v2SubPart.matches("\\d+")) {
                    int comparison = Integer.compare(Integer.parseInt(v1SubPart), Integer.parseInt(v2SubPart));
                    if (comparison != 0) {
                        return -comparison; // reverse order
                    }
                } else {
                    int comparison = v1SubPart.compareTo(v2SubPart);
                    if (comparison != 0) {
                        return -comparison; // reverse order
                    }
                }
            }
        }
        return 0;
    }

    public static int modVersionCompatibleToGameVersion(String versionRange, String profileVersion) {
        if (versionRange == null || profileVersion == null) return -1;
        if (versionRange.equals("(,)")) return 1;
        Matcher matcher = pattern.matcher(versionRange);
        if (!matcher.matches()) return -1;
        boolean closedStart = matcher.group(1).equals("[");
        String[] rangeParts = removeLeadingAndTrailingCommas(matcher.group(2)).split(",");
        if (rangeParts.length == 1 && rangeParts[0].isEmpty()) return -1;
        boolean closedEnd = matcher.group(3).equals("]");
        int compareStart = compare(rangeParts[0], profileVersion);
        if (compareStart < 0)
            if (!closedStart)
                return 1;
        int compareEnd = compare(rangeParts[rangeParts.length - 1], profileVersion);
        if (compareEnd > 0)
            if (!closedEnd)
                return 1;
        if (rangeParts.length < 3)
            if (compareStart >= 0 && compareEnd <= 0)
                return 2;
            else if (Arrays.asList(rangeParts).contains(profileVersion))
                return 2;
        return 0;
    }

    private static String removeLeadingAndTrailingCommas(String version) {
        int start = 0, end = version.length() - 1;
        while (start < version.length() && version.charAt(start) == ',') start++;
        while (end > start && version.charAt(end) == ',') end--;
        return version.substring(start, end + 1);
    }
}
