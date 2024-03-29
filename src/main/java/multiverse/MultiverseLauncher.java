package multiverse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MultiverseLauncher extends Application {

    static {
        Statics.PROFILES_DIRECTORY.mkdirs();
        Statics.VERSIONS_DIRECTORY.mkdirs();
        Statics.QUILT_DIRECTORY.mkdirs();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setMinWidth(1024);
        stage.setMinHeight(576);
        FXMLLoader fxmlLoader = new FXMLLoader(MultiverseLauncher.class.getResource("launcher.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Multiverse Launcher");
        stage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        stage.setScene(scene);
        stage.show();
    }
}