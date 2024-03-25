package multiverse.managers;

import multiverse.Statics;
import multiverse.json.Profile;
import multiverse.json.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static multiverse.Statics.GSON;

public class SettingsManager {
    public static final Settings SETTINGS;
    public static final File FILE = new File(Statics.BASE_DIRECTORY, "settings.json");

    static {
        if (!FILE.exists()) {
            SETTINGS = new Settings("latest");
        } else {
            try {
                SETTINGS = GSON.fromJson(Files.readString(FILE.toPath()), Settings.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean updateSettings(Profile lastProfile) {
        if (lastProfile == null) return false;
        try {
            SETTINGS.setLastProfile(lastProfile.getName());
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getLastProfile() {
        return SETTINGS.getLastProfile();
    }
}
