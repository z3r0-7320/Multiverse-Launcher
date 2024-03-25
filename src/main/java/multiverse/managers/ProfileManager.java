package multiverse.managers;

import multiverse.Statics;
import multiverse.json.Profile;
import multiverse.utils.DirectoryDeleter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static multiverse.Statics.GSON;
import static multiverse.utils.DirectoryDeleter.deleteDir;

public class ProfileManager {

    private static List<Profile> profiles;

    public static Profile createProfile(String name, String version, boolean useQuilt, String quiltLoaderVersion) {
        if (profiles == null) getProfiles();
        File dir = new File(Statics.PROFILES_DIRECTORY, name);
        try {
            if (!dir.mkdirs()) return null;
            Profile profile = new Profile(name, version, new File(Statics.PROFILES_DIRECTORY, name).getPath(), useQuilt, quiltLoaderVersion);
            Files.write(new File(dir, "profile.json").toPath(), GSON.toJson(profile).getBytes());
            profiles.add(profile);
            SettingsManager.updateSettings(profile);
            new File(dir, "mods").mkdir();
            new File(dir, "worlds").mkdir();
            new File(dir, "quilt-mods").mkdir();
            return profile;
        } catch (IOException e) {
            DirectoryDeleter.deleteDir(dir);
            e.printStackTrace();
        }
        return null;
    }

    public static Profile editProfile(Profile profile, String name, String version, boolean useQuilt, String quiltLoaderVersion) {
        if (profile == null) return null;
        File ProfileDir = new File(Statics.PROFILES_DIRECTORY, profile.getName());
        if (!profile.getName().equals(name) && !ProfileDir.renameTo(new File(Statics.PROFILES_DIRECTORY, name))) return null;
        profile.setName(name);
        profile.setVersion(version);
        profile.setUseQuilt(useQuilt);
        profile.setQuiltLoaderVersion(quiltLoaderVersion);
        try {
            Files.write(new File(new File(Statics.PROFILES_DIRECTORY, name), "profile.json").toPath(), GSON.toJson(profile).getBytes());
            SettingsManager.updateSettings(profile);
            return profile;
        } catch (IOException e) {
        }
        return null;
    }


    public static Profile getProfile(String name) {
        return (profiles == null ? getProfiles() : profiles).stream().filter(profile -> profile.getName().equals(name)).findFirst().orElse(null);
    }

    public static List<Profile> getProfiles() {
        try {
            if (profiles != null) return profiles;
            List<Profile> profiles = new ArrayList<>();
            for (File file : Objects.requireNonNullElseGet(Statics.PROFILES_DIRECTORY.listFiles(), () -> new File[0])) {
                Profile profile = GSON.fromJson(Files.readString(new File(file, "profile.json").toPath()), Profile.class);
                if (file.isDirectory()) profiles.add(profile);
            }
            return (ProfileManager.profiles = profiles);
        } catch (IOException e) {
            return List.of();
        }
    }

    public static boolean deleteProfile(String name) {
        File dir = new File(Statics.PROFILES_DIRECTORY, name);
        return deleteDir(dir) && profiles.remove(getProfile(name));
    }
}
