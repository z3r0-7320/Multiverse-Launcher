package multiverse.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.management.OperatingSystemMXBean;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import multiverse.Statics;
import multiverse.json.Profile;
import multiverse.json.Settings;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.Arrays;

import static multiverse.Statics.GSON;

public class SettingsManager {
    public static final Settings SETTINGS;
    public static final File FILE = new File(Statics.BASE_DIRECTORY, "settings.json");
    private static final ObjectProperty<Boolean> checkForUpdates = new SimpleObjectProperty<>();


    static {
        int ram = (int) (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() / 1048576);
        if (!FILE.exists()) {
            SETTINGS = new Settings("latest", null,
                    Math.min(ram / 4, 2048),
                    Math.min(ram / 2, 4096),
                    Statics.DARK_MODE,
                    true,
                    true,
                    false,
                    true
            );
        } else {
            try {
                String json = Files.readString(FILE.toPath());
                JsonObject object = JsonParser.parseString(json).getAsJsonObject();
                SETTINGS = GSON.fromJson(object, Settings.class);
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
                if (SETTINGS.getThemes() == null || SETTINGS.getThemes().length == 0)
                    SETTINGS.setThemes(Statics.DARK_MODE);
                if (object.get("useGitHubVersions") == null) {
                    SETTINGS.setUseGitHubVersions(true);
                }
                if (object.get("useItchIoVersions") == null) {
                    SETTINGS.setUseItchIoVersions(true);
                }
                if (object.get("showExperimentalVersions") == null) {
                    SETTINGS.setShowExperimentalVersions(false);
                }
                if (object.get("checkForUpdates") == null) {
                    SETTINGS.setCheckForUpdates(true);
                }
                checkForUpdates.set(SETTINGS.checkForUpdates());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean updateLastProfile(Profile lastProfile) {
        try {
            SETTINGS.setLastProfile(lastProfile != null ? lastProfile.getName() : null);
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

    public static boolean updateThemes(String... themes) {
        if (themes == null || Arrays.equals(themes, SETTINGS.getThemes())) return false;
        try {
            SETTINGS.setThemes(themes);
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

    public static String[] getThemes() {
        return SETTINGS.getThemes();
    }

    public static boolean useGitHubVersions() {
        return SETTINGS.useGitHubVersions();
    }

    public static boolean useItchIoVersions() {
        return SETTINGS.useItchIoVersions();
    }

    public static boolean showExperimentalVersions() {
        return SETTINGS.showExperimentalVersions();
    }

    public static boolean updateUseGitHubVersions(boolean selected) {
        if (selected == SETTINGS.useGitHubVersions()) return false;
        try {
            SETTINGS.setUseGitHubVersions(selected);
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean updateUseItchIoVersions(boolean selected) {
        if (selected == SETTINGS.useItchIoVersions()) return false;
        try {
            SETTINGS.setUseItchIoVersions(selected);
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean updateShowExperimentalVersions(boolean selected) {
        if (selected == SETTINGS.showExperimentalVersions()) return false;
        try {
            SETTINGS.setShowExperimentalVersions(selected);
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean checkForUpdates() {
        return SETTINGS.checkForUpdates();
    }

    public static boolean updateCheckForUpdates(boolean selected) {
        if (selected == SETTINGS.checkForUpdates()) return false;
        try {
            SETTINGS.setCheckForUpdates(selected);
            checkForUpdates.set(selected);
            Files.write(FILE.toPath(), GSON.toJson(SETTINGS).getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void addUpdateListener(ChangeListener<Boolean> listener) {
        checkForUpdates.addListener(listener);
    }
}
