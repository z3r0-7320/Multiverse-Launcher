package multiverse.managers;

import com.sun.management.OperatingSystemMXBean;
import multiverse.Statics;
import multiverse.json.Profile;
import multiverse.json.Settings;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;

import static multiverse.Statics.GSON;

public class SettingsManager {
    public static final Settings SETTINGS;
    public static final File FILE = new File(Statics.BASE_DIRECTORY, "settings.json");

    static {
        int ram = (int) (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() / 1048576);
        if (!FILE.exists()) {
            SETTINGS = new Settings("latest", null,
                    Math.min(ram / 4, 2048),
                    Math.min(ram / 2, 4096)
            );
        } else {
            try {
                SETTINGS = GSON.fromJson(Files.readString(FILE.toPath()), Settings.class);
                if (SETTINGS.getMinRam() <= 0) {
                    SETTINGS.setMinRam(Math.min(ram / 4, 2048));
                }
                if (SETTINGS.getMaxRam() <= 0) {
                    SETTINGS.setMaxRam(Math.min(ram / 2, 4096));
                }
                if (SETTINGS.getMinRam() > SETTINGS.getMaxRam()) {
                    SETTINGS.setMinRam(Math.min(ram / 4, 2048));
                    SETTINGS.setMaxRam(Math.min(ram / 2, 4096));
                }
                if (SETTINGS.getLastProfile() == null) {
                    SETTINGS.setLastProfile("latest");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean updateLastProfile(Profile lastProfile) {
        if (lastProfile == null) return false;
        try {
            SETTINGS.setLastProfile(lastProfile.getName());
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean updateIchIoApiKey(String apiKey) {
        if (apiKey == null || apiKey.equals(SETTINGS.getApiKey())) return false;
        try {
            SETTINGS.setApiKey(apiKey);
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean updateMinRam(int minRam) {
        if (minRam == SETTINGS.getMinRam()) return false;
        try {
            SETTINGS.setMinRam(minRam);
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean updateMaxRam(int maxRam) {
        if (maxRam == 0 || maxRam == SETTINGS.getMaxRam()) return false;
        try {
            SETTINGS.setMaxRam(maxRam);
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getLastProfile() {
        return SETTINGS.getLastProfile();
    }

    public static String getApiKey() {
        return SETTINGS.getApiKey();
    }
}
