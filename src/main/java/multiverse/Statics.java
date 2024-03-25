package multiverse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

public final class Statics {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final File BASE_DIRECTORY = new File("./multiverse");
    public static final File VERSIONS_DIRECTORY = new File(BASE_DIRECTORY, "versions");
    public static final File PROFILES_DIRECTORY = new File(BASE_DIRECTORY, "profiles");
    public static final File QUILT_DIRECTORY = new File(BASE_DIRECTORY,"quilt");

    public static final String COSMIC_REACH_JAR_NAME = "cosmic-reach.jar";
    public static final String QUILT_LOADER_JAR_NAME = "cosmic-quilt.jar";
}
