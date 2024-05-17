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
            return null;
        }
    }

    public static String downloadAsString(URL url) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n");
                }
                in.close();
                return response.toString();
            }
        } catch (IOException ignored) {
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
        boolean r = true;
        try {
            dir.mkdirs();
            HttpsURLConnection httpsConn;
            httpsConn = (HttpsURLConnection) new URI(url).toURL().openConnection();
            if (headers != null) {
                for (String header : headers) {
                    String[] split = header.split(":", 2);
                    if (split.length == 2) httpsConn.setRequestProperty(split[0].trim(), split[1].trim());
                }
            }

            int responseCode = httpsConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpsConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(new File(dir, filename));
                int bytesRead;
                byte[] buffer = new byte[4096];
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
                outputStream.close();
                inputStream.close();
            } else {
                r = false;
            }
            httpsConn.disconnect();
        } catch (URISyntaxException e) {
            r = false;
        } catch (IOException e) {
            new File(dir, filename).delete();
            r = false;
        }
        return r;
    }

    public static byte[] downloadFileToByteArray(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }
}
