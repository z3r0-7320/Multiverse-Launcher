package multiverse.launcherupdater;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import multiverse.Statics;
import multiverse.utils.Downloader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Updater {
    public static void checkForUpdates(Node node) {
        if (node == null || Statics.VERSION == null) return;
        try {
            String json = Downloader.downloadAsString(Statics.GITHUB_LAUNCHER_UPDATE_URL);
            if (json == null) return;
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            String version = object.get("tag_name").getAsString();
            object.get("assets").getAsJsonArray().forEach((JsonElement element) -> {
                JsonObject asset = element.getAsJsonObject();
                if (asset.get("name").getAsString().endsWith(".jar")) {
                    if (!version.equals(Statics.VERSION)) {
                        Platform.runLater(() -> {
                            node.setVisible(true);
                            node.addEventFilter(ActionEvent.ACTION, (event) -> {
                                node.setDisable(true);
                                if (Downloader.downloadFile(asset.get("browser_download_url").getAsString(), Statics.BASE_DIRECTORY.getParentFile(), asset.get("name").getAsString())) {
                                    try {
                                        Files.copy(Updater.class.getClassLoader().getResourceAsStream("Deleter.class"), new File(Statics.BASE_DIRECTORY, "Deleter.class").toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        new ProcessBuilder("java", "-jar", new File(Statics.BASE_DIRECTORY.getParentFile(), asset.get("name").getAsString()).getAbsolutePath()).start();
                                        new ProcessBuilder("java", "-cp", Statics.BASE_DIRECTORY.getAbsolutePath(), "Deleter", new File(Updater.class.getProtectionDomain().getCodeSource().getLocation()
                                                .toURI()).getName()).start();
                                        System.exit(0);
                                    } catch (IOException | URISyntaxException ignored) {
                                    }
                                }
                            });
                        });
                    }
                }
            });
        } catch (Exception ignored) {
        }
    }
}