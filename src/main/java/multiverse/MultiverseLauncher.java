package multiverse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import multiverse.utils.ThemeSwitcher;
import multiverse.utils.VersionComparator;

import java.io.File;
import java.io.IOException;

public class MultiverseLauncher extends Application {

    static {
        Statics.PROFILES_DIRECTORY.mkdirs();
        Statics.VERSIONS_DIRECTORY.mkdirs();
        Statics.ICONS_DIRECTORY.mkdirs();
        Statics.QUILT_DIRECTORY.mkdirs();
        new File(Statics.BASE_DIRECTORY, "Deleter.class").delete();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MultiverseLauncher.class.getResource("launcher.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ThemeSwitcher.setTheme(scene);
        stage.setTitle("Multiverse Launcher");
        stage.getIcons().add(new Image(MultiverseLauncher.class.getResourceAsStream("icon.png")));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        stage.setMinHeight(scene.getHeight());
        stage.setMinWidth(scene.getWidth());
    }
}