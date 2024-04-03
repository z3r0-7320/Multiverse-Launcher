package multiverse;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import multiverse.managers.SettingsManager;

import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    public Button saveButton;
    public Button cancelButton;
    public TextField itchIoApiKey;
    public Label errorField;

    public void handleSave(ActionEvent actionEvent) {
        if (!itchIoApiKey.getText().equals(SettingsManager.SETTINGS.getApiKey()))
            SettingsManager.updateIchIoApiKey(itchIoApiKey.getText());
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
    }
}
