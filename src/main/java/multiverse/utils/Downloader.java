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
            URL obj = new URI(url).toURL();
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }
        } catch (IOException | URISyntaxException ignored) {
        }
        return null;
    }

    public static boolean downloadFile(String url, File dir, String filename, Consumer<Double> consumer) {
        boolean r = true;
        try {
            dir.mkdirs();
            HttpsURLConnection httpsConn;
            httpsConn = (HttpsURLConnection) new URI(url).toURL().openConnection();
            int responseCode = httpsConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpsConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(new File(dir, filename));
                int bytesRead;
                byte[] buffer = new byte[4096];
                long totalBytesRead = 0;
                long fileSize = httpsConn.getContentLength();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    double percent = (double) totalBytesRead / fileSize;
                    if (consumer != null) consumer.accept(percent);
                }
                outputStream.close();
                inputStream.close();
            } else {
                r = false;
            }
            httpsConn.disconnect();
        } catch (IOException | URISyntaxException e) {
            r = false;
        }
        return r;
    }
}
