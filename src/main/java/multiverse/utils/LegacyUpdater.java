package multiverse.utils;

import multiverse.Statics;
import multiverse.json.Profile;
import multiverse.managers.ProfileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LegacyUpdater {
    public static void update() {
        moveMods();
    }

    public static void moveMods() {
        for (Profile profile : ProfileManager.getProfiles()) {
            File modsFolder = new File(Statics.PROFILES_DIRECTORY, profile.getName() + "/quilt-mods");
            if (modsFolder.exists() && modsFolder.isDirectory()) {
                File[] toCopy = modsFolder.listFiles();
                boolean canDelete = true;
                if (toCopy.length > 0) {
                    File newModsFolder = new File(Statics.PROFILES_DIRECTORY, profile.getName() + "/mods");
                    newModsFolder.mkdirs();
                    for (File file : toCopy) {
                        try {
                            Files.move(file.toPath(), new File(newModsFolder, file.getName()).toPath());
                        } catch (IOException e) {
                            canDelete = false;
                        }
                    }
                }
                if (canDelete) DirectoryDeleter.deleteDir(modsFolder);
            }
        }
    }
}
