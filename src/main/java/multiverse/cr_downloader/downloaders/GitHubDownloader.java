package multiverse.cr_downloader.downloaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import multiverse.Statics;
import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.crversions.GitHubVersion;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.cr_downloader.interfaces.CosmicReachDownloader;
import multiverse.utils.Downloader;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import static multiverse.Statics.GSON;


public class GitHubDownloader implements CosmicReachDownloader {

    @Override
    public List<GitHubVersion> getVersions() throws CRDownloaderException {
        String json = Downloader.downloadAsString(Statics.GITHUB_GIT_TREE);
        if (json == null) throw new CRDownloaderException("Failed to download GitHub tree");
        JsonArray tree = JsonParser.parseString(json).getAsJsonObject().get("tree").getAsJsonArray();
        if (tree == null) throw new CRDownloaderException("Failed to parse GitHub tree");
        List<GitHubVersion> versions = GSON.fromJson(tree, new TypeToken<List<GitHubVersion>>() {
        }.getType());
        return versions.stream().filter(GitHubVersion::isValid).toList();
    }

    @Override
    public void downloadVersion(CosmicReachVersion version, Consumer<Double> consumer) throws CRDownloaderException {
        if (version instanceof GitHubVersion gitHubVersion) {
            String versionStr = gitHubVersion.getVersion();
            Downloader.downloadFile(gitHubVersion.getUrl(), new File(Statics.VERSIONS_DIRECTORY, versionStr), Statics.COSMIC_REACH_JAR_NAME, new String[]{"Accept:application/vnd.github.raw+json"}, consumer);
        } else throw new CRDownloaderException("Invalid version type");
    }
}
