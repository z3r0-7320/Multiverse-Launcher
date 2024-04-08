package multiverse.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Explorer {
    public static void openExplorer(File file) {
        SwingUtilities.invokeLater(() -> {
            if (!file.isDirectory()) {
                throw new RuntimeException("`" + file + "` is not a directory!");
            } else {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Exception var2) {
                    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
                        try {
                            Runtime.getRuntime().exec("explorer.exe \"" + file.getAbsolutePath() + "\"");
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        });
    }
}
