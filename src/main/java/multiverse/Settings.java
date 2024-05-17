package multiverse;

import com.sun.management.OperatingSystemMXBean;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import multiverse.cr_downloader.DownloadManager;
import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.crversions.LocalVersion;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.managers.SettingsManager;
import multiverse.utils.Explorer;
import multiverse.utils.ThemeSwitcher;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Settings implements Initializable {
    public Button saveButton;
    public Button cancelButton;
    public TextField itchIoApiKey;
    public Label errorField;
    public Slider maxRamSlider;
    public Slider minRamSlider;
    public CheckBox useGitHubVersions;
    public CheckBox useItchIoVersions;
    public CheckBox showExperimentalVersions;
    public CheckBox checkForUpdates;
    public TextArea modRepos;
    String[] themes = null;

    public void handleSave(ActionEvent actionEvent) {
        SettingsManager.updateIchIoApiKey(itchIoApiKey.getText());
        SettingsManager.updateMinRam((int) minRamSlider.getValue());
        SettingsManager.updateMaxRam((int) maxRamSlider.getValue());
        SettingsManager.updateThemes(themes);
        SettingsManager.updateUseGitHubVersions(useGitHubVersions.isSelected());
        SettingsManager.updateUseItchIoVersions(useItchIoVersions.isSelected());
        SettingsManager.updateShowExperimentalVersions(showExperimentalVersions.isSelected());
        SettingsManager.updateCheckForUpdates(checkForUpdates.isSelected());
        SettingsManager.updateModRepos(modRepos.getText().split("\n"));
        Platform.runLater(() -> {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        });
    }

    public void handleCancel(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            if (themes != null) ThemeSwitcher.switchTheme(SettingsManager.getThemes());
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SettingsManager.getApiKey() != null)
            itchIoApiKey.setText(SettingsManager.getApiKey());
        useGitHubVersions.setSelected(SettingsManager.useGitHubVersions());
        useItchIoVersions.setSelected(SettingsManager.useItchIoVersions());
        showExperimentalVersions.setSelected(SettingsManager.showExperimentalVersions());
        checkForUpdates.setSelected(SettingsManager.checkForUpdates());

        minRamSlider.setMin(1024);
        minRamSlider.setMax((int) (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() / 1048576));
        minRamSlider.setValue(SettingsManager.SETTINGS.getMinRam());
        minRamSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((double) newValue > maxRamSlider.getValue()) {
                maxRamSlider.setValue(newValue.intValue());
            }
        });
        maxRamSlider.setMin(1024);
        maxRamSlider.setMax((int) (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() / 1048576));
        maxRamSlider.setValue(SettingsManager.SETTINGS.getMaxRam());
        maxRamSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((double) newValue < minRamSlider.getValue()) {
                minRamSlider.setValue(newValue.intValue());
            }
        });
        modRepos.setText(String.join("\n", SettingsManager.getModRepos()));
    }

    public void manageCR(ActionEvent actionEvent) {
        Explorer.openExplorer(Statics.VERSIONS_DIRECTORY);
    }

    public void manageCQ(ActionEvent actionEvent) {
        Explorer.openExplorer(Statics.QUILT_DIRECTORY);
    }

    public void manageIcons(ActionEvent actionEvent) {
        Explorer.openExplorer(Statics.ICONS_DIRECTORY);
    }

    public void setDarkTheme(ActionEvent actionEvent) {
        ThemeSwitcher.switchTheme(themes = Statics.DARK_MODE);
    }

    public void setLightTheme(ActionEvent actionEvent) {
        ThemeSwitcher.switchTheme(themes = Statics.LIGHT_MODE);
    }

    public void setHighContrast(ActionEvent actionEvent) {
        ThemeSwitcher.switchTheme(themes = Statics.HIGH_CONTRAST);
    }

    public void extractIcons(ActionEvent actionEvent) {
        try {
            DownloadManager.update();
        } catch (CRDownloaderException ignored) {
        }
        try {
            for (CosmicReachVersion version : DownloadManager.getVersions(true)) {
                if (version instanceof LocalVersion localVersion) {
                    try (ZipFile zipFile = new ZipFile(new File(Statics.VERSIONS_DIRECTORY, localVersion.getVersion() + "/" + Statics.COSMIC_REACH_JAR_NAME))) {
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            if (entry.getName().startsWith("textures/blocks/") && !entry.isDirectory()) {
                                File destFile = new File(Statics.ICONS_DIRECTORY, new File(entry.getName()).getName());
                                if (destFile.exists()) {
                                    continue;
                                }
                                destFile.getParentFile().mkdirs();
                                try (InputStream in = zipFile.getInputStream(entry)) {
                                    Files.copy(in, destFile.toPath());
                                }
                            }
                        }
                    }
                    return;
                }
            }
            errorField.setText("No installed version found, please install a version first.");
        } catch (Exception e) {
            errorField.setText("Failed to extract icons");
        }
    }
}
