package multiverse.utils;

public class URLUtil {
    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf('/') + 1, getLastIndex(url));
    }

    private static int getLastIndex(String url) {
        int lastIndex = url.indexOf('?');
        return lastIndex == -1 ? url.length() : lastIndex;
    }
}
