package multiverse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import multiverse.utils.NullOnFailTypeAdapterFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.function.Supplier;

public class Statics {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory(new NullOnFailTypeAdapterFactory()).create();

    public static final File BASE_DIRECTORY = ((Supplier<File>) () -> {
        try {
            return new File(new File(Statics.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile(), "/multiverse");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }).get();
    public static final File VERSIONS_DIRECTORY = new File(BASE_DIRECTORY, "versions");
    public static final File PROFILES_DIRECTORY = new File(BASE_DIRECTORY, "profiles");
    public static final File ICONS_DIRECTORY = new File(BASE_DIRECTORY, "icons");
    public static final File THEMES_DIRECTORY = new File(BASE_DIRECTORY, "themes");
    public static final File QUILT_DIRECTORY = new File(BASE_DIRECTORY, "quilt");
    public static final String COSMIC_REACH_JAR_NAME = "cosmic-reach.jar";
    public static final String QUILT_LOADER_JAR_NAME = "cosmic-quilt.jar";
    public static final String QUILT_LOADER_VERSIONS = "https://jitpack.io/api/builds/org.codeberg.CRModders/cosmic-quilt";
    public static final String QUILT_LOADER_DOWNLOAD = "https://jitpack.io/org/codeberg/CRModders/cosmic-quilt/%s/%s";
    public static final String[] MAVEN_REPOSITORIES = {
            "https://maven.quiltmc.org/repository/release/",
            "https://maven.fabricmc.net/",
            "https://repo.spongepowered.org/maven/",
            "https://jitpack.io/",
    };

    public static final String[] DARK_MODE = {
            "base.css",
            "dark.css"
    };

    public static final String[] LIGHT_MODE = {
            "base.css",
            "light.css"
    };

    public static final String[] HIGH_CONTRAST = {
            "base.css",
            "highcontrast.css"
    };

    public static final String GITHUB_USER = "CRModders";
    public static final String GITHUB_REPO = "CosmicArchive";
    public static final String GITHUB_BRANCH = "main";
    public static final String GITHUB_API_URL = "https://api.github.com";
    public static final String GITHUB_GIT_TREE = GITHUB_API_URL + "/repos/%s/%s/git/trees/%s?recursive=1".formatted(GITHUB_USER, GITHUB_REPO, GITHUB_BRANCH);
    public static final String ITCH_IO_WORKER_URL = "https://workers-playground-dawn-pond-be0d.cosmicreachdl.workers.dev";
    public static final String ITCH_IO_WORKER_BUILDS_URL = ITCH_IO_WORKER_URL + "/builds";
    public static final String ITCH_IO_WORKER_DOWNLOAD_URL = ITCH_IO_WORKER_URL + "/download/%d";
    public static final String ITCH_IO_API_URL = "https://api.itch.io";
    public static final String ITCH_IO_BUILDS_URL = ITCH_IO_API_URL + "/uploads/9891067/builds?api_key=%s";
    public static final String ITCH_IO_DOWNLOAD_URL = ITCH_IO_API_URL + "/builds/%d/download/archive/default?api_key=%s";
    public static final String VERSION = Statics.class.getPackage().getImplementationVersion();
    public static final String JAVA_EXECUTABLE = ProcessHandle.current().info().command().orElseThrow();
    public static final String GITHUB_LAUNCHER_UPDATE_URL = GITHUB_API_URL + "/repos/z3r0-7320/Multiverse-Launcher/releases/latest";
}
