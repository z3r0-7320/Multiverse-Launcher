package multiverse.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import multiverse.Statics;
import multiverse.json.QuiltRelease;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QuiltManager {
    public static final List<QuiltRelease> RELEASES = new ArrayList<>();

    public static boolean updateReleases() {
        RELEASES.clear();
        File[] directories = Statics.QUILT_DIRECTORY.listFiles();
        if (directories != null) {
            for (File file : directories) {
                if (new File(file, Statics.QUILT_LOADER_JAR_NAME).exists()) {
                    RELEASES.add(new QuiltRelease(file.getName(), true));
                }
            }
        }
        String response = Downloader.downloadAsString(Statics.QUILT_LOADER_VERSIONS);
        if (response == null) return true;
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        JsonObject cosmicQuilt = jsonObject.getAsJsonObject("org.codeberg.CRModders").getAsJsonObject("cosmic-quilt");
        Map<String, String> map = new Gson().fromJson(cosmicQuilt, new TypeToken<Map<String, String>>() {
        }.getType());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!entry.getValue().equalsIgnoreCase("error") && entry.getKey().matches("\\d+\\.\\d+\\.\\d+")) {
                boolean found = false;
                for (QuiltRelease release : RELEASES) {
                    if (release.getVersionNumber().equals(entry.getKey())) {
                        found = true;
                        break;
                    }
                }
                if (!found) RELEASES.add(new QuiltRelease(entry.getKey(), false));
            }
        }
        RELEASES.sort(ListSorter::compare);
        return false;
    }

    public static boolean downloadRelease(QuiltRelease release, Consumer<Double> consumer) {
        if (release.isLocal()) return true;
        boolean error = !Downloader.downloadFile(Statics.QUILT_LOADER_DOWNLOAD.formatted(release.getVersionNumber(), "cosmic-quilt-%s.jar".formatted(release.getVersionNumber())),
                new File(Statics.QUILT_DIRECTORY, release.getVersionNumber()), Statics.QUILT_LOADER_JAR_NAME, consumer);
        if (error) return false;
        String dependencies = Downloader.downloadAsString(Statics.QUILT_LOADER_DOWNLOAD.formatted(release.getVersionNumber(), "cosmic-quilt-%s.pom".formatted(release.getVersionNumber())));
        if (dependencies == null) return false;
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(dependencies)));
            NodeList dependencyList = document.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dependency = (Element) dependencyList.item(i);
                String groupId = dependency.getElementsByTagName("groupId").item(0).getTextContent();
                String artifactId = dependency.getElementsByTagName("artifactId").item(0).getTextContent();
                String version1 = dependency.getElementsByTagName("version").item(0).getTextContent();
                //String scope = dependency.getElementsByTagName("scope").item(0).getTextContent();

                if (!artifactId.equalsIgnoreCase("cosmicreach"))
                    if (!downloadDependencies(release, groupId, artifactId, version1, Statics.MAVEN_REPOSITORIES, consumer))
                        return false;
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            return false;
        }

        return true;
    }

    public static boolean downloadDependencies(QuiltRelease quiltVersion, String groupId, String artifactId, String version, String[] repositories, Consumer<Double> consumer) {
        if (groupId == null || artifactId == null || version == null) {
            return false;
        }
        boolean downloaded = false;
        for (String repository : repositories) {
            if (Downloader.downloadFile(repository + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar",
                    new File(Statics.QUILT_DIRECTORY, quiltVersion.getVersionNumber() + "/deps"), artifactId + "-" + version + ".jar", consumer)) {
                downloaded = true;
                break;
            }
        }
        return downloaded;
    }
}
