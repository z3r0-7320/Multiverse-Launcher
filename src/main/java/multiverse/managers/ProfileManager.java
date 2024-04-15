package multiverse.managers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import multiverse.Statics;
import multiverse.json.Profile;
import multiverse.utils.DirectoryDeleter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static multiverse.Statics.GSON;
import static multiverse.utils.DirectoryDeleter.deleteDir;

public class ProfileManager {

    private static final ObservableList<Profile> profiles = FXCollections.observableArrayList();
    private static final ObjectProperty<Profile> currentProfile = new SimpleObjectProperty<>();

    static {
        getProfiles();
        Profile profile;
        if (profiles.isEmpty()) profile = ProfileManager.createProfile("Latest", "latest", false, "", null);
        else if ((profile = getProfile(SettingsManager.getLastProfile())) == null) profile = profiles.get(0);
        SettingsManager.updateLastProfile(profile);
        currentProfile.set(profile);
    }

    public static boolean updateCurrentProfile(Profile profile) {
        return updateCurrentProfile(profile, false);
    }

    private static boolean updateCurrentProfile(Profile profile, boolean force) {
        if (profile == null || !force && (Objects.equals(currentProfile.get(), profile) || !SettingsManager.updateLastProfile(profile)))
            return false;
        currentProfile.set(profile);
        return true;
    }

    public static Profile createProfile(String name, String version, boolean useQuilt, String quiltLoaderVersion, File icon) {
        File dir = new File(Statics.PROFILES_DIRECTORY, name);
        try {
            if (!dir.mkdirs()) return null;
            String iconName;
            if (icon != null) {
                if (icon.getParentFile().getCanonicalPath().equals(Statics.ICONS_DIRECTORY.getCanonicalPath()))
                    iconName = icon.getName();
                else iconName = copyAndResizeImageAndGetIconName(icon);
                if (iconName == null) return null;
            } else iconName = null;
            Profile profile = new Profile(name, version, new File(Statics.PROFILES_DIRECTORY, name).getPath(), useQuilt, quiltLoaderVersion, iconName);
            Files.write(new File(dir, "profile.json").toPath(), GSON.toJson(profile).getBytes());
            updateCurrentProfile(profile);
            profiles.add(profile);
            new File(dir, "mods").mkdir();
            new File(dir, "worlds").mkdir();
            return profile;
        } catch (IOException e) {
            DirectoryDeleter.deleteDir(dir);
        }
        return null;
    }

    public static Profile editProfile(Profile profile, String name, String version, boolean useQuilt, String quiltLoaderVersion, File icon) {
        if (profile == null) return null;
        File ProfileDir = new File(Statics.PROFILES_DIRECTORY, profile.getName());
        if (!profile.getName().equals(name) && !ProfileDir.renameTo(new File(Statics.PROFILES_DIRECTORY, name)))
            return null;
        String iconName;
        if (icon != null) {
            try {
                if (icon.getParentFile().getCanonicalPath().equals(Statics.ICONS_DIRECTORY.getCanonicalPath()))
                    iconName = icon.getName();
                else iconName = copyAndResizeImageAndGetIconName(icon);
                if (iconName == null) return null;
            } catch (IOException e) {
                iconName = null;
            }
        } else iconName = null;
        profile.setName(name);
        profile.setVersion(version);
        profile.setUseQuilt(useQuilt);
        profile.setQuiltLoaderVersion(quiltLoaderVersion);
        profile.setIconName(iconName);
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).equals(profile)) {
                profiles.set(i, profile);
                break;
            }
        }
        try {
            Files.write(new File(new File(Statics.PROFILES_DIRECTORY, name), "profile.json").toPath(), GSON.toJson(profile).getBytes());
            SettingsManager.updateLastProfile(profile);
            return profile;
        } catch (IOException ignored) {
        }
        return null;
    }

    private static String copyAndResizeImageAndGetIconName(File icon) throws IOException {
        String iconName;
        File newIconLocation = new File(Statics.ICONS_DIRECTORY, icon.getName());
        int i = 1;
        String[] split = new String[2];
        int index = icon.getName().lastIndexOf('.');
        if (index < 0) return null;
        split[0] = icon.getName().substring(0, index);
        split[1] = icon.getName().substring(index);
        while (newIconLocation.exists()) newIconLocation = new File(Statics.ICONS_DIRECTORY, split[0] + i++ + split[1]);

        BufferedImage originalImage = ImageIO.read(icon);
        int type = originalImage.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

        double scaleFactor = Math.min(1d, 256d / Math.max(originalImage.getHeight(), originalImage.getWidth()));

        int scaledWidth = (int) (originalImage.getWidth() * scaleFactor);
        int scaledHeight = (int) (originalImage.getHeight() * scaleFactor);
        Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

        BufferedImage resizedImage = new BufferedImage(scaledWidth, scaledHeight, type);
        resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);
        ImageIO.write(resizedImage, split[1].substring(1), newIconLocation);

        iconName = newIconLocation.getName();
        return iconName;
    }


    public static Profile getProfile(String name) {
        return profiles.stream().filter(profile -> profile.getName().equals(name)).findFirst().orElse(null);
    }

    public static List<Profile> getProfiles() {
        try {
            if (!profiles.isEmpty()) return profiles;
            ObservableList<Profile> profiles = FXCollections.observableArrayList();
            for (File file : Objects.requireNonNullElseGet(Statics.PROFILES_DIRECTORY.listFiles(), () -> new File[0])) {
                File profileJson = new File(file, "profile.json");
                if (profileJson.exists()) {
                    Profile profile = GSON.fromJson(Files.readString(new File(file, "profile.json").toPath()), Profile.class);
                    profiles.add(profile);
                }
            }
            ProfileManager.profiles.addAll(profiles);
            return profiles;
        } catch (IOException e) {
            return List.of();
        }
    }

    public static boolean deleteProfile(Profile profile) {
        File dir = new File(Statics.PROFILES_DIRECTORY, profile.getName());
        return deleteDir(dir) && profiles.remove(profile) && updateCurrentProfile(profiles.isEmpty() ? null : profiles.get(0));
    }

    public static ObservableList<Profile> getObservableProfiles() {
        return profiles;
    }

    public static ObjectProperty<Profile> currentProfileProperty() {
        return currentProfile;
    }

    public static Profile getCurrentProfile() {
        return currentProfile.get();
    }

    public static void setCurrentProfile(Profile profile) {
        currentProfile.set(profile);
    }

    public static ObservableList<Profile> profilesProperty() {
        return profiles;
    }


}
