package multiverse.managers;

import multiverse.Statics;
import multiverse.json.Builds;
import multiverse.utils.ListSorter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static multiverse.Statics.COSMIC_REACH_JAR_NAME;
import static multiverse.Statics.GSON;

public class BuildManager {
    public static List<Builds.Build> builds;

    static {
        updateBuilds();
    }

    public static boolean updateBuilds() {
        boolean networkError = false;
        File[] directories = Statics.VERSIONS_DIRECTORY.listFiles();
        List<Builds.Build> builds = new ArrayList<>();
        if (directories != null) {
            for (File file : directories) {
                if (new File(file, COSMIC_REACH_JAR_NAME).exists()) {
                    builds.add(new Builds.Build(file.getName(), 0));
                }
            }
        }
        try {
            URL obj = new URL(SettingsManager.getApiKey() == null || SettingsManager.getApiKey().isBlank() ? "https://workers-playground-dawn-pond-be0d.cosmicreachdl.workers.dev/builds" : ("https://api.itch.io/uploads/9891067/builds?api_key=" + SettingsManager.getApiKey()));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                for (Builds.Build build : GSON.fromJson(response.toString(), Builds.class).getBuilds()) {
                    boolean found = false;
                    for (Builds.Build localBuild : builds) {
                        if (localBuild.getUserVersion().equals(build.getUserVersion())) {
                            localBuild.setId(build.getId());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        builds.add(build);
                    }
                }
            }
        } catch (IOException ignored) {
            networkError = true;
        }

        builds.sort(ListSorter::compare);
        BuildManager.builds = builds;
        return networkError;
    }
}
