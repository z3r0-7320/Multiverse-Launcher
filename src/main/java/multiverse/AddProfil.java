package multiverse;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import multiverse.cr_downloader.DownloadManager;
import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.json.Profile;
import multiverse.json.QuiltRelease;
import multiverse.managers.ProfileManager;
import multiverse.managers.SettingsManager;
import multiverse.utils.QuiltManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AddProfil implements Initializable {
    public TextField profileNameField;
    public ComboBox<CosmicReachVersion> versionComboBox;
    public ComboBox<QuiltRelease> modSupportComboBox;
    public Button saveButton;
    public Button cancelButton;
    public Label errorField;
    public ProgressBar progressBar;


    public boolean isEdit;
    public File icon;
    public Profile profile;
    public ImageView profileImageView;
    public Button chooseImageButton;
    public Button removeImageButton;

    private static boolean unzip(String zipFilePath, File destDir) {
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        return false;
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        return false;
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
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

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isEdit = ProgramState.getCurrentStatus() == ProgramState.ProgramStateEnum.EDIT_PROFILE;
        profile = isEdit ? ProfileManager.getProfile(SettingsManager.getLastProfile()) : null;
        if (profile == null || profile.getIconName() == null || profile.getIconName().isBlank())
            setDefaultImage();
        else {
            icon = new File(Statics.ICONS_DIRECTORY, profile.getIconName());
            profileImageView.setImage(new Image(icon.toURI().toString()));
        }
        new Thread(() -> {
            setupProfileNameField(profile);
            setupComboBoxes(profile);
            Platform.runLater(() -> {
                versionComboBox.setDisable(false);
                modSupportComboBox.setDisable(false);
            });
        }).start();
    }

    private void setupProfileNameField(Profile profile) {
        Platform.runLater(() -> {
            profileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.matches("[a-zA-Z0-9]*")) {
                    profileNameField.setText(oldValue);
                }
            });
            if (isEdit && profile != null)
                profileNameField.setText(profile.getName());
        });
    }

    private void setupComboBoxes(Profile profile) {
        boolean networkError = QuiltManager.updateReleases();
        try {
            DownloadManager.update();
        } catch (CRDownloaderException e) {
            showError(e.getMessage());
        }

        Platform.runLater(() -> {
            versionComboBox.getItems().add(DownloadManager.getLatestVersionPlaceholder());
            versionComboBox.getItems().addAll(DownloadManager.getVersions());
            versionComboBox.setValue(getVersionComboBoxValue(profile));

            modSupportComboBox.getItems().add(QuiltRelease.none);
            modSupportComboBox.getItems().addAll(QuiltManager.RELEASES);
            modSupportComboBox.setValue(getModSupportComboBoxValue(profile));

            if (networkError)
                errorField.setText("Some versions could not be loaded");
        });
    }

    private CosmicReachVersion getVersionComboBoxValue(Profile profile) {
        return isEdit && profile != null ? versionComboBox.getItems().stream().filter(build -> build.getVersion().equals(profile.getVersion())).findFirst().orElse(DownloadManager.getLatestVersionPlaceholder()) : DownloadManager.getLatestVersionPlaceholder();
    }

    private QuiltRelease getModSupportComboBoxValue(Profile profile) {
        return isEdit && profile != null && profile.useQuilt() ? modSupportComboBox.getItems().stream().filter(build -> build.getVersionNumber().equals(profile.getQuiltLoaderVersion())).findFirst().orElse(QuiltRelease.unknown) : QuiltRelease.none;
    }

    public void handleSave(ActionEvent actionEvent) {
        String profileName = profileNameField.getText();
        if (profileName.isEmpty()) {
            errorField.setText("Profile name cannot be empty");
            return;
        }

        CosmicReachVersion version = versionComboBox.getSelectionModel().getSelectedItem();
        File profileDir = new File(Statics.PROFILES_DIRECTORY, profileName);

        if (profileDir.exists() && !isEdit) {
            errorField.setText("Profile already exists");
            return;
        }

        QuiltRelease quiltRelease = modSupportComboBox.getSelectionModel().getSelectedItem();
        boolean quilt = !quiltRelease.equals(QuiltRelease.none);

        new Thread(() -> {
            boolean error = false;
            disableUI(true);
            progressBar.setVisible(true);
            try {
                DownloadManager.downloadVersion(version, d -> Platform.runLater(() -> progressBar.setProgress(d)));
            } catch (CRDownloaderException e) {
                error = showError(e.getMessage());
            }
            if (!error && quilt) {
                try {
                    QuiltManager.downloadRelease(quiltRelease, d -> Platform.runLater(() -> progressBar.setProgress(d)));
                } catch (CRDownloaderException e) {
                    error = showError(e.getMessage());
                }
            }
            if (error) ;
            else if (isEdit) {
                if ((!profileName.equals(profile.getName()) ||
                     (!version.getVersion().equals(profile.getVersion())) ||
                     (!quiltRelease.equals(QuiltRelease.unknown) && !quiltRelease.getVersionNumber().equals(profile.getQuiltLoaderVersion())) ||
                     !Objects.equals(profile.getIconName() != null ? new File(Statics.ICONS_DIRECTORY, profile.getIconName()) : null, icon))) {
                    if (ProfileManager.editProfile(profile, profileName, version.getVersion(), quilt, quiltRelease.getVersionNumber(), icon) != null) {
                        closeWindow();
                    } else showError("Failed to edit profile");
                } else closeWindow();
            } else if (!profileDir.exists())
                if (ProfileManager.createProfile(profileName, version.getVersion(), quilt, quiltRelease.getVersionNumber(), icon) != null)
                    closeWindow();
                else showError("Failed to create profile");
            else showError("Profile already exists");
            disableUI(false);
        }).start();
    }

    private void disableUI(boolean disable) {
        Platform.runLater(() -> {
            saveButton.setDisable(disable);
            cancelButton.setDisable(disable);
            versionComboBox.setDisable(disable);
            modSupportComboBox.setDisable(disable);
            chooseImageButton.setDisable(disable);
            removeImageButton.setDisable(disable);
            profileNameField.setDisable(disable);
        });
    }

    private boolean showError(String message) {
        Platform.runLater(() -> errorField.setText(message));
        return true;
    }

    private void closeWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        });
    }

    public void handleCancel(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
    }

    public void handleChooseImage(ActionEvent actionEvent) {
        disableUI(true);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setInitialDirectory(Statics.ICONS_DIRECTORY);
        icon = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (icon != null) {
            profileImageView.setImage(new Image(icon.toURI().toString()));
        }
        disableUI(false);
    }

    public void setDefaultImage() {
        icon = null;
        profileImageView.setImage(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
    }
}
