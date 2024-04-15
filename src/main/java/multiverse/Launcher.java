package multiverse;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import multiverse.cr_downloader.DownloadManager;
import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.json.Profile;
import multiverse.launcherupdater.Updater;
import multiverse.managers.ProfileManager;
import multiverse.managers.SettingsManager;
import multiverse.utils.Downloader;
import multiverse.utils.Explorer;
import multiverse.utils.LegacyUpdater;
import multiverse.utils.ThemeSwitcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;

import static multiverse.Statics.GSON;

public class Launcher implements Initializable {

    public Button launchButton;
    public TextArea consoleTextArea;
    public Button addProfileButton;
    public ComboBox<Profile> profileComboBox;
    public Button deleteProfileButton;
    public Button editProfileButton;
    public Button openFolderButton;
    public TextArea changelogTextArea;
    public Button settingsButton;
    public FlowPane profilePane;
    public BorderPane borderPane;
    public Button updateButton;
    private int lineCounter;

    private static String getChangeLog() {
        String changelog = Downloader.downloadAsString("https://workers-playground-dawn-pond-be0d.cosmicreachdl.workers.dev/changelog");
        return changelog != null ? changelog : "[\"Failed to fetch changelog\"]";
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    for (Profile profile : ProfileManager.getProfiles()) {
                        profileComboBox.getItems().add(profile);
                        addProfile(profile);
                    }
                    profileComboBox.setValue(ProfileManager.getCurrentProfile());
                });
                ProfileManager.getObservableProfiles().addListener((ListChangeListener<? super Profile>) observable -> Platform.runLater(() -> {
                    profileComboBox.getItems().clear();
                    profileComboBox.setValue(null);
                    profilePane.getChildren().clear();
                    for (Profile profile : ProfileManager.getProfiles()) {
                        profileComboBox.getItems().add(profile);
                        addProfile(profile);
                    }
                    profileComboBox.setValue(ProfileManager.getCurrentProfile());
                    selectProfile(ProfileManager.getCurrentProfile());
                }));
                ProfileManager.currentProfileProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                    profileComboBox.setValue(newValue);
                    selectProfile(newValue);
                }));
                LegacyUpdater.update();
                if (SettingsManager.checkForUpdates()) Updater.checkForUpdates(updateButton);
                SettingsManager.addUpdateListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                    if (newValue) Updater.checkForUpdates(updateButton);
                    else updateButton.setVisible(false);
                }));
                String changelog = getChangeLog();
                Platform.runLater(() -> changelogTextArea.setText(String.join("\n\n", GSON.fromJson(changelog, String[].class))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @FXML
    protected void onLaunchButtonClick() {
        disableUI(true);
        Profile profile = profileComboBox.getValue();
        if (profile == null) return;
        consoleTextArea.setText("Launching Cosmic Reach...\n");
        try {
            DownloadManager.update();
        } catch (CRDownloaderException ignored) {
            consoleTextArea.appendText("Failed to update some versions\n");
        }
        try {
            CosmicReachVersion build = DownloadManager.getVersion(profile.getVersion());
            new Thread(() -> {
                String version = build.getVersion();
                try {
                    DownloadManager.downloadVersion(build, null);
                } catch (CRDownloaderException e) {
                    Platform.runLater(() -> consoleTextArea.setText("Failed to download version " + version + "\n"));
                    return;
                }
                File launchTarget = new File(Statics.VERSIONS_DIRECTORY, build.getVersion() + "/" + Statics.COSMIC_REACH_JAR_NAME);
                if (launchTarget.exists() && ((profile.useQuilt() && new File(Statics.QUILT_DIRECTORY, profile.getQuiltLoaderVersion() + "/" + Statics.QUILT_LOADER_JAR_NAME).exists()) || !profile.useQuilt()))
                    try {
                        ProcessBuilder processBuilder = profile.useQuilt() ?
                                new ProcessBuilder(Statics.JAVA_EXECUTABLE,
                                        "-Xms" + SettingsManager.SETTINGS.getMinRam() + "m",
                                        "-Xmx" + SettingsManager.SETTINGS.getMaxRam() + "m",
                                        "-Dloader.gameJarPath=" + Statics.VERSIONS_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + version + "/" + Statics.COSMIC_REACH_JAR_NAME,
                                        "-Dloader.skipMcProvider=true",
                                        "-Dcosmicquilt.colorizeLogs=false",
                                        "-Dcosmicquilt.cacheLogs=false",
                                        "-classpath", Statics.QUILT_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + profile.getQuiltLoaderVersion() + "/cosmic-quilt.jar" + (System.getProperty("os.name").toLowerCase().startsWith("win") ? ";" : ":") + Statics.QUILT_DIRECTORY.getAbsolutePath().replace('\\', '/') + "/" + profile.getQuiltLoaderVersion() + "/deps/*",
                                        "org.quiltmc.loader.impl.launch.knot.KnotClient") :
                                new ProcessBuilder(Statics.JAVA_EXECUTABLE, "-jar", "-Xms" + SettingsManager.SETTINGS.getMinRam() + "m", "-Xmx" + SettingsManager.SETTINGS.getMaxRam() + "m", launchTarget.getAbsolutePath());
                        processBuilder.directory(new File(Statics.PROFILES_DIRECTORY, profile.getName()));
                        Process process = processBuilder.start();
                        lineCounter = 0;
                        writeToWindow(process.getInputStream());
                        writeToWindow(process.getErrorStream());
                        Platform.runLater(() -> process.onExit().thenRun(() -> disableUI(false)));
                    } catch (IOException e) {
                        Platform.runLater(() -> disableUI(false));
                    }
                else Platform.runLater(() -> {
                    consoleTextArea.setText("Failed to find version " + version);
                    disableUI(false);
                });
            }).start();
        } catch (CRDownloaderException e) {
            consoleTextArea.setText("Failed to find version " + profile.getVersion());
            disableUI(false);
        }
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

    public void onProfileAddButtonClick(ActionEvent actionEvent) throws IOException {
        disableUI(true);
        ProgramState.updateStatus(ProgramState.ProgramStateEnum.CREATE_PROFILE);
        Stage popupStage = createPopupStage("Add Profile", "add_profil.fxml");
        popupStage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        popupStage.setOnHiding(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.setOnCloseRequest(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.sizeToScene();
        popupStage.show();
    }

    private Stage createPopupStage(String title, String name) throws IOException {
        Stage popupStage = new Stage();
        popupStage.setTitle(title);
        FXMLLoader fxmlLoader = new FXMLLoader(MultiverseLauncher.class.getResource(name));
        Scene popupScene = new Scene(fxmlLoader.load());
        popupStage.setScene(popupScene);
        ThemeSwitcher.setTheme(popupScene);
        popupStage.setResizable(false);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        return popupStage;
    }

    private void disableUI(boolean disable) {
        profileComboBox.setDisable(disable);
        profilePane.setDisable(disable);
        addProfileButton.setDisable(disable);
        editProfileButton.setDisable(disable);
        deleteProfileButton.setDisable(disable);
        openFolderButton.setDisable(disable);
        launchButton.setDisable(disable);
        settingsButton.setDisable(disable);
    }

    public void selectProfile(ActionEvent actionEvent) {
        ProfileManager.updateCurrentProfile(profileComboBox.getValue());
        selectProfile(profileComboBox.getValue());
    }

    public void onProfileDeleteButtonClick(ActionEvent actionEvent) throws IOException {
        disableUI(true);
        ProgramState.updateStatus(ProgramState.ProgramStateEnum.EDIT_PROFILE);
        Stage popupStage = new Stage();
        popupStage.setTitle("Delete Profile");
        FXMLLoader fxmlLoader = new FXMLLoader(MultiverseLauncher.class.getResource("delete.fxml"));
        fxmlLoader.setController(new Delete(() -> Platform.runLater(() -> {
            Profile profile = profileComboBox.getValue();
            if (profile != null) {
                ProfileManager.deleteProfile(profile);
            }
        }), null));
        Scene popupScene = new Scene(fxmlLoader.load());
        popupStage.setScene(popupScene);
        ThemeSwitcher.setTheme(popupScene);
        popupStage.setResizable(false);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        popupStage.setOnHiding(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.setOnCloseRequest(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.sizeToScene();
        popupStage.show();
    }

    public void onProfileEditButtonClick(ActionEvent actionEvent) throws IOException {
        disableUI(true);
        ProgramState.updateStatus(ProgramState.ProgramStateEnum.EDIT_PROFILE);
        Stage popupStage = createPopupStage("Edit Profile", "add_profil.fxml");
        popupStage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        popupStage.setOnHiding(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.setOnCloseRequest(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.sizeToScene();
        popupStage.show();
    }

    public void onOpenFolderButtonClick(ActionEvent actionEvent) {
        Explorer.openExplorer(new File(Statics.PROFILES_DIRECTORY, profileComboBox.getValue().getName()));
    }

    public void onSettingsButtonClick(ActionEvent actionEvent) throws IOException {
        disableUI(true);
        ProgramState.updateStatus(ProgramState.ProgramStateEnum.EDIT_PROFILE);
        Stage popupStage = createPopupStage("Settings", "settings.fxml");
        popupStage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        popupStage.setOnHiding(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.setOnCloseRequest(event -> Platform.runLater(() -> disableUI(false)));
        popupStage.sizeToScene();
        popupStage.show();
    }


    public void addProfile(Profile profile) {
        VBox profileBox = new VBox();
        profileBox.setAlignment(Pos.CENTER);

        Image originalImage = profile.getIconName() == null || profile.getIconName().isBlank() || !new File(Statics.ICONS_DIRECTORY, profile.getIconName()).exists() ?
                new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")) :
                scale(new Image(new File(Statics.ICONS_DIRECTORY, profile.getIconName()).toURI().toString()), 256);

        ImageView profileImage = new ImageView(originalImage);
        profileImage.setPreserveRatio(true);
        profileImage.setFitHeight(100);
        profileImage.setFitWidth(100);

        StackPane imagePane = new StackPane();
        imagePane.setMinSize(100, 100);
        imagePane.setMaxSize(100, 100);
        imagePane.getChildren().add(profileImage);
        StackPane.setAlignment(profileImage, Pos.CENTER);

        profileBox.getChildren().add(imagePane);

        Label profileName = new Label(profile.getName());
        profileName.setMaxWidth(100);
        profileName.setAlignment(Pos.CENTER);
        profileBox.getChildren().add(profileName);

        profileBox.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                selectProfile(profileBox, profile);
            } else if (e.getClickCount() == 2) {
                startProfile(profile);
            }
        });

        profilePane.getChildren().add(profileBox);
    }

    public Image scale(Image source, int targetSize) {
        double scaleFactor = Math.max(targetSize / source.getWidth(), targetSize / source.getHeight());

        int targetWidth = (int) (source.getWidth() * scaleFactor);
        int targetHeight = (int) (source.getHeight() * scaleFactor);

        PixelReader reader = source.getPixelReader();
        WritableImage output = new WritableImage(targetWidth, targetHeight);
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int srcX = (int) (x / (double) targetWidth * source.getWidth());
                int srcY = (int) (y / (double) targetHeight * source.getHeight());
                writer.setColor(x, y, reader.getColor(srcX, srcY));
            }
        }

        return output;
    }

    public void selectProfile(Profile profile) {
        if (profile == null) return;
        ObservableList<Node> profiles = profilePane.getChildren();
        for (Node node : profiles) {
            VBox profileBox = (VBox) node;
            if (profileBox.getChildren().get(1) instanceof Label label && label.getText().equals(profile.getName())) {
                selectProfile(profileBox, profile);
                return;
            }
        }
    }

    public void selectProfile(VBox profileBox, Profile profile) {
        for (Node node : profilePane.getChildren()) {
            node.getStyleClass().remove("profile-selected");
        }
        profileComboBox.setValue(profile);
        profileBox.getStyleClass().add("profile-selected");
    }

    public void startProfile(Profile profile) {
        onLaunchButtonClick();
    }
}