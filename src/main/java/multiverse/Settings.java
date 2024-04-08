package multiverse;

import com.sun.management.OperatingSystemMXBean;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import multiverse.managers.SettingsManager;
import multiverse.utils.Explorer;

import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    public Button saveButton;
    public Button cancelButton;
    public TextField itchIoApiKey;
    public Label errorField;
    public Slider maxRamSlider;
    public Slider minRamSlider;

    public void handleSave(ActionEvent actionEvent) {
        SettingsManager.updateIchIoApiKey(itchIoApiKey.getText());
        SettingsManager.updateMinRam((int) minRamSlider.getValue());
        SettingsManager.updateMaxRam((int) maxRamSlider.getValue());
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SettingsManager.getApiKey() != null) {
            itchIoApiKey.setText(SettingsManager.getApiKey());
        }
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
}
