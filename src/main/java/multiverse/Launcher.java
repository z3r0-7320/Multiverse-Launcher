package multiverse;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
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
    public VBox modEntries;
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
                //Platform.runLater(this::addMod);
                String changelog = getChangeLog();
                Platform.runLater(() -> changelogTextArea.setText(String.join("\n\n", GSON.fromJson(changelog, String[].class))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void addMod(){
        for (int i = 0; i < 10; i++) {
            // Create HBox for each mod entry
            HBox modEntry = new HBox();
            modEntry.setAlignment(Pos.CENTER);
            modEntry.setSpacing(10);
            modEntry.setPrefHeight(150);

            // Add image
            ImageView imageView = cropTo16x9(new Image(MultiverseLauncher.class.getResourceAsStream("img.png"))); // Replace "yourImage.png" with actual image path
            imageView.setFitHeight(130);
            imageView.setPreserveRatio(true);

            HBox.setMargin(imageView, new Insets(0, 0, 0, 10));
            modEntry.getChildren().add(roundedNode(imageView));

            // Add description ScrollPane
            TextArea descriptionTextArea = new TextArea("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.   \n" +
                                                        "\n" +
                                                        "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.");
            descriptionTextArea.setWrapText(true);
            descriptionTextArea.setMaxHeight(130);
            descriptionTextArea.setEditable(false);
            HBox.setHgrow(descriptionTextArea, Priority.ALWAYS);
            modEntry.getChildren().add(descriptionTextArea);

            // Add version selector
            ComboBox<String> versionComboBox = new ComboBox<>();
            versionComboBox.getItems().addAll("Version 1", "Version 2", "Version 3"); // Add your version options here
            versionComboBox.setValue("Select Version");
            modEntry.getChildren().add(versionComboBox);

            // Add install button
            Button installButton = new Button("Install");
            HBox.setMargin(installButton, new Insets(0, 10, 0, 0));
            modEntry.getChildren().add(installButton);

            modEntry.getStyleClass().add("mod-entry");

            modEntries.getChildren().add(modEntry);
        }
    }

    private Node roundedNode(Node inputNode) {
        final Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.setWidth(inputNode.getLayoutBounds().getWidth());
        clip.setHeight(inputNode.getLayoutBounds().getHeight());
        inputNode.setClip(clip);

        return inputNode;
    }

    public static ImageView cropTo16x9(Image image) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        double targetWidth;
        double targetHeight;
        double targetX;
        double targetY;

        // Calculate the dimensions and position for the cropped image
        if ((double)16/9 * imageHeight <= imageWidth) {
            targetHeight = imageHeight;
            targetWidth = (double)16/9 * imageHeight;
            targetX = (imageWidth - targetWidth) / 2;
            targetY = 0;
        } else {
            targetWidth = imageWidth;
            targetHeight = (double)9/16 * imageWidth;
            targetX = 0;
            targetY = (imageHeight - targetHeight) / 2;
        }

        // Create a new ImageView with the cropped image
        ImageView imageView = new ImageView(image);
        imageView.setViewport(new Rectangle2D(targetX, targetY, targetWidth, targetHeight));

        return imageView;
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
        roundedNode(profileImage);

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