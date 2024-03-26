package multiverse;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import multiverse.json.Builds;
import multiverse.json.Profile;
import multiverse.managers.BuildManager;
import multiverse.managers.ProfileManager;
import multiverse.managers.SettingsManager;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static multiverse.Statics.GSON;

public class Launcher {

    public Button launchButton;
    public TextArea consoleTextArea;
    public Button addProfileButton;
    public ComboBox<Profile> profileComboBox;
    public Button deleteProfileButton;
    public Button editProfileButton;
    public Button openFolderButton;
    public TextArea changelogTextArea;
    private int lineCounter;

    public Launcher() {
        try {
            File dir = Statics.VERSIONS_DIRECTORY;
            dir.mkdir();
            Platform.runLater(() -> {
                if (ProfileManager.getProfiles().isEmpty()) {
                    SettingsManager.updateSettings(ProfileManager.createProfile("Latest", "latest", false, ""));
                }
                for (Profile profile : ProfileManager.getProfiles()) {
                    profileComboBox.getItems().add(profile);
                }
                Profile lastProfile = ProfileManager.getProfile(SettingsManager.getLastProfile());
                profileComboBox.setValue(lastProfile == null ? ProfileManager.getProfiles().get(0) : lastProfile);
                changelogTextArea.setText(String.join("\n\n", GSON.fromJson(getChangeLog(), String[].class)));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getChangeLog() {
        try {
            URL url = new URL("https://workers-playground-dawn-pond-be0d.cosmicreachdl.workers.dev/changelog");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine).append("\n");
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
            return "[\"Failed to fetch changelog\"]";
        }
    }

    private static boolean unzip(String zipFilePath, File destDir) {
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.getName().endsWith(".jar")) {
                    File newFile = new File(destDir, Statics.COSMIC_REACH_JAR_NAME);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    break;
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @FXML
    protected void onLaunchButtonClick() {
        Profile profile = profileComboBox.getValue();
        if (profile == null || BuildManager.builds.isEmpty()) return;
        consoleTextArea.setText("Launching Cosmic Reach...\n");
        String version = profile.getVersion();
        Builds.Build build;
        if (version.equals("latest")) {
            build = BuildManager.builds.get(0);
            version = build.getUserVersion();
        } else {
            Builds.Build found = null;
            for (Builds.Build b : BuildManager.builds) {
                if (b.getUserVersion().equals(version)) {
                    found = b;
                    break;
                }
            }
            build = found;
        }
        String finalVersion = version;
        new Thread(() -> {
            Platform.runLater(() -> disableUI(true));
            File launchTarget = new File(Statics.VERSIONS_DIRECTORY, finalVersion + "/" + Statics.COSMIC_REACH_JAR_NAME);
            if (launchTarget.getParentFile().mkdirs() || !launchTarget.exists()) {
                if (build != null && build.getId() != 0) {
                    File zip = new File(Statics.VERSIONS_DIRECTORY, finalVersion + "/cosmic_reach.zip");
                    File dir = new File(Statics.VERSIONS_DIRECTORY, finalVersion);
                    try {
                        Platform.runLater(() -> consoleTextArea.appendText("Starting download of version " + finalVersion + "\n"));
                        if (download(new URL("https://workers-playground-dawn-pond-be0d.cosmicreachdl.workers.dev/download/" + build.getId()), dir.getPath()))
                            if (unzip(zip.getPath(), dir))
                                zip.delete();
                            else Platform.runLater(() -> consoleTextArea.appendText("Failed to unzip\n"));
                        else Platform.runLater(() -> consoleTextArea.appendText("Failed to download\n"));

                    } catch (MalformedURLException ignored) {
                    }
                } else
                    Platform.runLater(() -> consoleTextArea.setText("Failed to find version " + finalVersion + "\n"));
            }

            if (launchTarget.exists() && ((profile.useQuilt() && new File(Statics.QUILT_DIRECTORY, profile.getQuiltLoaderVersion() + "/" + Statics.QUILT_LOADER_JAR_NAME).exists()) || !profile.useQuilt()))
                try {
                    ProcessBuilder processBuilder = profile.useQuilt() ?
                            new ProcessBuilder("java", "-Dloader.gameJarPath=" + Statics.VERSIONS_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + finalVersion + "/" + Statics.COSMIC_REACH_JAR_NAME,
                                    "-Dloader.modsDir=" + Statics.PROFILES_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + profile.getName() + "/quilt-mods",
                                    "-Dloader.skipMcProvider=true",
                                    //"-Dloader.addMods=" + Statics.QUILT_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + profile.getQuiltLoaderVersion() + "/cosmic-quilt.jar",
                                    "-classpath", Statics.QUILT_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + profile.getQuiltLoaderVersion() + "/cosmic-quilt.jar" + (System.getProperty("os.name").toLowerCase().startsWith("win") ? ";" : ":") + Statics.QUILT_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + profile.getQuiltLoaderVersion() + "/deps/*",
                                    "org.quiltmc.loader.impl.launch.knot.KnotClient") :
                            new ProcessBuilder("java", "-jar", launchTarget.getAbsolutePath());

                    processBuilder.directory(new File(Statics.PROFILES_DIRECTORY, profile.getName()));

                    Process process = processBuilder.start();

                    launchButton.setDisable(true);
                    lineCounter = 0;
                    writeToWindow(process.getInputStream());
                    writeToWindow(process.getErrorStream());
                    Platform.runLater(() -> process.onExit().thenRun(() -> disableUI(false)));
                } catch (IOException e) {
                    Platform.runLater(() -> disableUI(false));
                }
            else Platform.runLater(() -> consoleTextArea.setText("Failed to find version " + finalVersion + "\n"));
        }).start();
    }

    private void writeToWindow(final InputStream src) {
        new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                src.transferTo(new OutputStream() {
                    @Override
                    public void write(int b) {
                        String c = String.valueOf((char) b);
                        sb.append(c);
                        if (c.equals("\n")) {
                            if (lineCounter > 500) {
                                sb.delete(0, sb.indexOf("\n") + 1);
                            } else {
                                lineCounter++;
                            }
                            final String text = sb.toString();
                            Platform.runLater(() -> {
                                consoleTextArea.setText(text);
                                consoleTextArea.setScrollTop(Double.MAX_VALUE);
                            });
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean download(URL url, String path) {
        boolean r = true;
        try {
            HttpsURLConnection httpConn;
            httpConn = (HttpsURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = path + File.separator + "cosmic_reach.zip";
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
            } else {
                r = false;
            }
            httpConn.disconnect();
        } catch (IOException e) {
            r = false;
        }
        return r;
    }

    public void onProfileAddButtonClick(ActionEvent actionEvent) throws IOException {
        disableUI(true);
        ProgramState.updateStatus(ProgramState.ProgramStateEnum.CREATE_PROFILE);
        Stage popupStage = createPopupStage("Add Profile");
        popupStage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        popupStage.setOnHiding(event -> Platform.runLater(this::refreshUI));
        popupStage.setOnCloseRequest(event -> Platform.runLater(this::refreshUI));
        popupStage.show();
    }

    private Stage createPopupStage(String title) throws IOException {
        Stage popupStage = new Stage();
        popupStage.setTitle(title);
        FXMLLoader fxmlLoader = new FXMLLoader(MultiverseLauncher.class.getResource("add_profil.fxml"));
        popupStage.setScene(new Scene(fxmlLoader.load()));
        popupStage.setResizable(false);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        return popupStage;
    }

    private void disableUI(boolean disable) {
        profileComboBox.setDisable(disable);
        addProfileButton.setDisable(disable);
        editProfileButton.setDisable(disable);
        deleteProfileButton.setDisable(disable);
        openFolderButton.setDisable(disable);
        launchButton.setDisable(disable);
    }

    private void refreshUI() {
        profileComboBox.getItems().clear();
        for (Profile profile : ProfileManager.getProfiles()) {
            profileComboBox.getItems().add(profile);
        }
        profileComboBox.setValue(ProfileManager.getProfile(SettingsManager.getLastProfile()));
        disableUI(false);
    }

    public void selectProfile(ActionEvent actionEvent) {
        SettingsManager.updateSettings(profileComboBox.getValue());
    }

    public void onProfileDeleteButtonClick(ActionEvent actionEvent) {
        Profile profile = profileComboBox.getValue();
        if (profile != null && ProfileManager.deleteProfile(profile.getName())) {
            profileComboBox.getItems().remove(profile);
            if (profileComboBox.getItems().isEmpty()) {
                profileComboBox.setValue(null);
                SettingsManager.updateSettings(null);
            } else {
                profileComboBox.setValue(ProfileManager.getProfiles().get(0));
                SettingsManager.updateSettings(ProfileManager.getProfiles().get(0));
            }

        }
    }

    public void onProfileEditButtonClick(ActionEvent actionEvent) throws IOException {
        disableUI(true);
        ProgramState.updateStatus(ProgramState.ProgramStateEnum.EDIT_PROFILE);
        Stage popupStage = createPopupStage("Edit Profile");
        popupStage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        popupStage.setOnHiding(event -> Platform.runLater(this::refreshUI));
        popupStage.setOnCloseRequest(event -> Platform.runLater(this::refreshUI));
        popupStage.show();
    }

    public void onOpenFolderButtonClick(ActionEvent actionEvent) {
        SwingUtilities.invokeLater(() -> {
            File profile = new File(Statics.PROFILES_DIRECTORY, profileComboBox.getValue().getName());
            if (!profile.isDirectory()) {
                throw new RuntimeException("`" + profile + "` is not a directory!");
            } else {
                try {
                    Desktop.getDesktop().open(profile);
                } catch (Exception var2) {
                    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
                        try {
                            Runtime.getRuntime().exec("explorer.exe \"" + profile.getAbsolutePath() + "\"");
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        });
    }
}