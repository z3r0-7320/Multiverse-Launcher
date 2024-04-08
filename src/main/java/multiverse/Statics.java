package multiverse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.net.URISyntaxException;

public final class Statics {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final File BASE_DIRECTORY;
    static {
        try {
            BASE_DIRECTORY = new File(new File(Statics.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile(), "/multiverse");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static final File VERSIONS_DIRECTORY = new File(BASE_DIRECTORY, "versions");
    public static final File PROFILES_DIRECTORY = new File(BASE_DIRECTORY, "profiles");
    public static final File ICONS_DIRECTORY = new File(BASE_DIRECTORY, "icons");
    public static final File QUILT_DIRECTORY = new File(BASE_DIRECTORY, "quilt");
    public static final String COSMIC_REACH_JAR_NAME = "cosmic-reach.jar";
    public static final String QUILT_LOADER_JAR_NAME = "cosmic-quilt.jar";
    public static final String QUILT_LOADER_VERSIONS = "https://jitpack.io/api/builds/org.codeberg.CRModders/cosmic-quilt";
    public static final String QUILT_LOADER_DOWNLOAD = "https://jitpack.io/org/codeberg/CRModders/cosmic-quilt/%s/%s";
    public static final String[] MAVEN_REPOSITORIES = {
            "https://maven.quiltmc.org/repository/release/",
            "https://maven.fabricmc.net/",
            "https://repo.spongepowered.org/maven/",
    };
}
