package multiverse.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import multiverse.Statics;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.json.QuiltRelease;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
        RELEASES.sort(VersionComparator::compare);
        return false;
    }

    public static void downloadRelease(QuiltRelease release, Consumer<Double> consumer) throws CRDownloaderException {
        if (release.isLocal()) return;
        File file = new File(Statics.QUILT_DIRECTORY, release.getVersionNumber());
        try {
            if (!Downloader.downloadFile(Statics.QUILT_LOADER_DOWNLOAD.formatted(release.getVersionNumber(), "cosmic-quilt-%s.jar".formatted(release.getVersionNumber())),
                    file, Statics.QUILT_LOADER_JAR_NAME, consumer))
                throw new CRDownloaderException("Failed to download Quilt Loader");
            String pom = Downloader.downloadAsString(Statics.QUILT_LOADER_DOWNLOAD.formatted(release.getVersionNumber(), "cosmic-quilt-%s.pom".formatted(release.getVersionNumber())));
            if (pom == null) throw new CRDownloaderException("Failed to download dependencies");
            Map<String, Dependency> dependencies1 = new HashMap<>();
            getDependencies(dependencies1, pom, null, 0);
            for (Dependency dependency : dependencies1.values())
                downloadDependencies(release, dependency, Statics.MAVEN_REPOSITORIES, consumer);
        } catch (CRDownloaderException e) {
            DirectoryDeleter.deleteDir(file);
            throw e;
        }
    }

    private static void getDependencies(Map<String, Dependency> dependencies, String pom, String search, int depth) throws CRDownloaderException {
        if (pom == null || depth > 9) return;
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(pom)));
            NodeList dependencyList = document.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dependency = (Element) dependencyList.item(i);
                String groupId = dependency.getElementsByTagName("groupId").item(0).getTextContent();
                String artifactId = dependency.getElementsByTagName("artifactId").item(0).getTextContent();
                if (search != null && !artifactId.equalsIgnoreCase(search)) continue;
                Node optionalNode = dependency.getElementsByTagName("optional").item(0);
                if (optionalNode != null && optionalNode.getTextContent().equalsIgnoreCase("true")) continue;
                if (dependency.getElementsByTagName("version").item(0) != null) {
                    String version = dependency.getElementsByTagName("version").item(0).getTextContent();
                    if (version.equals("${project.version}")) { // replace with parent version
                        NodeList parentList = document.getElementsByTagName("parent");
                        if (parentList != null && parentList.getLength() > 0) {
                            Element parent = (Element) parentList.item(0);
                            version = parent.getElementsByTagName("version").item(0).getTextContent();
                        }
                    }
                    if (version.matches("\\$\\{.*}")) continue;
                    Node scope = dependency.getElementsByTagName("scope").item(0);
                    if ((scope == null || (!scope.getTextContent().equalsIgnoreCase("test") || artifactId.equals("slf4j-api"))) && (!dependencies.containsKey(artifactId) || VersionComparator.compare(dependencies.get(artifactId).getVersion(), version) == 1) && !artifactId.equalsIgnoreCase("cosmicreach")) {
                        dependencies.put(artifactId, new Dependency(groupId, artifactId, version));
                        for (String repository : Statics.MAVEN_REPOSITORIES) {
                            String nextPom = Downloader.downloadAsString(repository + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");
                            if (nextPom != null) {
                                getDependencies(dependencies, nextPom, null, depth + 1);
                                break;
                            }
                        }
                    }
                } else {
                    NodeList parentList = document.getElementsByTagName("parent");
                    if (parentList.getLength() > 0) {
                        Element parent = (Element) parentList.item(0);
                        String parentGroupId = parent.getElementsByTagName("groupId").item(0).getTextContent();
                        String parentArtifactId = parent.getElementsByTagName("artifactId").item(0).getTextContent();
                        String parentVersion = parent.getElementsByTagName("version").item(0).getTextContent();
                        for (String repository : Statics.MAVEN_REPOSITORIES) {
                            String parentPom = Downloader.downloadAsString(repository + parentGroupId.replace('.', '/') + "/" + parentArtifactId + "/" + parentVersion + "/" + parentArtifactId + "-" + parentVersion + ".pom");
                            if (parentPom != null) {
                                getDependencies(dependencies, parentPom, artifactId, depth + 1);
                            }
                        }

                    }
                }
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new CRDownloaderException("Failed to parse POM", e);
        }
    }

    private static void downloadDependencies(QuiltRelease quiltVersion, Dependency dependency, String[] repositories, Consumer<Double> consumer) throws CRDownloaderException {
        if (dependency == null) {
            return;
        }
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();
        boolean downloaded = false;
        for (String repository : repositories) {
            if (Downloader.downloadFile(repository + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar",
                    new File(Statics.QUILT_DIRECTORY, quiltVersion.getVersionNumber() + "/deps"), artifactId + "-" + version + ".jar", consumer)) {
                downloaded = true;
                break;
            }
        }
        if (!downloaded) System.out.println("Failed to download dependency: " + artifactId + "-" + version);
    }

    private static class Dependency {
        private final String groupId;
        private final String artifactId;
        private final String version;

        public Dependency(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }
    }
}