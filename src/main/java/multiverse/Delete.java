package multiverse;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Delete {
    public Button confirmButton;
    public Button cancelButton;

    Runnable confirmAction;
    Runnable cancelAction;

    public Delete(Runnable confirmAction, Runnable cancelAction) {
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
    }

    public void handleConfirmation(ActionEvent actionEvent) {
        if (confirmAction != null) confirmAction.run();
        ((Stage) confirmButton.getScene().getWindow()).close();
    }

    public void handleCancel(ActionEvent actionEvent) {
        if (cancelAction != null) cancelAction.run();
        ((Stage) confirmButton.getScene().getWindow()).close();
    }
}
