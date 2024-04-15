package multiverse.utils;

import javafx.scene.Scene;
import javafx.stage.Window;
import multiverse.MultiverseLauncher;
import multiverse.Statics;
import multiverse.managers.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class ThemeSwitcher {
    private static String[] themes;

    static {
        loadThemes(SettingsManager.getThemes());
    }

    public static boolean switchTheme(String... themes) {
        boolean b = loadThemes(themes);
        if (b) applyStyles();
        return b;
    }

    private static boolean loadThemes(String... themes) throws RuntimeException {
        String[] t = new String[themes.length];
        for (int i = 0; i < themes.length; i++) {
            URL url = MultiverseLauncher.class.getResource(themes[i]);
            if (url != null) t[i] = url.toExternalForm();
            else {
                if (!Statics.THEMES_DIRECTORY.exists()) return false;
                File themeFile = new File(Statics.THEMES_DIRECTORY, themes[i]);
                if (!themeFile.isFile()) return false;
                try {
                    t[i] = Files.readString(themeFile.toPath());
                } catch (IOException ignored) {
                    return false;
                }
            }
        }
        ThemeSwitcher.themes = t;
        return true;
    }

    private static void applyStyles() {
        for (Window window : Window.getWindows()) {
            setTheme(window.getScene());
        }
    }

    public static void setTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        scene.getStylesheets().addAll(themes);
    }
}
