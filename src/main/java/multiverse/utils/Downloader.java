package multiverse.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

public class Downloader {

    public static String downloadAsString(String url) {
        try {
            return downloadAsString(new URI(url).toURL());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String downloadAsString(URL url) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine).append("\n");
                    }
                    return response.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean downloadFile(String url, File dir, String filename) {
        return downloadFile(url, dir, filename, null, null);
    }

    public static boolean downloadFile(String url, File dir, String filename, Consumer<Double> consumer) {
        return downloadFile(url, dir, filename, null, consumer);
    }

    public static boolean downloadFile(String url, File dir, String filename, String[] headers, Consumer<Double> consumer) {
        try {
            dir.mkdirs();
            URL fileUrl = new URI(url).toURL();
            return downloadFileInternal(fileUrl, new File(dir, filename), headers, consumer);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] downloadFileToByteArray(String fileUrl) throws IOException {
        return downloadFileToByteArray(fileUrl, null);
    }

    public static byte[] downloadFileToByteArray(String fileUrl, Consumer<Double> consumer) throws IOException {
        URL url = new URL(fileUrl);
        HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
        int responseCode = httpsConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download file: HTTP response code " + responseCode);
        }

        int fileSize = httpsConn.getContentLength();
        try (InputStream in = httpsConn.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            double lastPercent = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                if (fileSize > 0 && consumer != null) {
                    double percent = (double) totalBytesRead / fileSize;
                    if (percent - lastPercent > 0.01) {
                        consumer.accept(percent);
                        lastPercent = percent;
                    }
                }
            }
            return out.toByteArray();
        } finally {
            httpsConn.disconnect();
        }
    }

    private static boolean downloadFileInternal(URL url, File outputFile, String[] headers, Consumer<Double> consumer) throws IOException {
        HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
        if (headers != null) {
            for (String header : headers) {
                String[] split = header.split(":", 2);
                if (split.length == 2) httpsConn.setRequestProperty(split[0].trim(), split[1].trim());
            }
        }

        int responseCode = httpsConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = httpsConn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                long fileSize = httpsConn.getContentLength();
                double lastPercent = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    double percent = (double) totalBytesRead / fileSize;
                    if (consumer != null && percent - lastPercent > 0.01) {
                        consumer.accept(percent);
                        lastPercent = percent;
                    }
                }
            } catch (IOException e) {
                outputFile.delete();
                throw e;
            } finally {
                httpsConn.disconnect();
            }
            return true;
        } else {
            httpsConn.disconnect();
            return false;
        }
    }
}
