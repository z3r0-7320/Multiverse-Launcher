package multiverse.cr_downloader;


import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.crversions.GitHubVersion;
import multiverse.cr_downloader.crversions.ItchIoVersion;
import multiverse.cr_downloader.crversions.LocalVersion;
import multiverse.cr_downloader.downloaders.GitHubDownloader;
import multiverse.cr_downloader.downloaders.ItchIoDownloader;
import multiverse.cr_downloader.downloaders.LocalDownloader;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.managers.SettingsManager;
import multiverse.utils.ListSorter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DownloadManager {
    private static final GitHubDownloader gitHubDownloader = new GitHubDownloader();
    private static final ItchIoDownloader itchIoDownloader = new ItchIoDownloader();
    private static final LocalDownloader localDownloader = new LocalDownloader();
    private static final List<CosmicReachVersion> versions = new ArrayList<>();
    private static final CosmicReachVersion latestVersionPlaceholder = new CosmicReachVersion() {
        @Override
        public String getVersion() {
            return "latest";
        }
    };

    public static void update() throws CRDownloaderException {
        versions.clear();
        boolean error = false;
        addVersions(localDownloader.getVersions());
        if (SettingsManager.useGitHubVersions())
            try {
                addVersions(gitHubDownloader.getVersions());
            } catch (CRDownloaderException e) {
                error = true;
            }
        if (SettingsManager.useItchIoVersions())
            try {
                addVersions(itchIoDownloader.getVersions());
            } catch (CRDownloaderException e) {
                error = true;
            }
        versions.sort(ListSorter::compare);
        if (error) throw new CRDownloaderException("Some versions could not be loaded");
    }

    public static void downloadVersion(CosmicReachVersion version, Consumer<Double> consumer) throws CRDownloaderException {
        if (version == null) throw new CRDownloaderException("Version is null");
        if (version.equals(latestVersionPlaceholder))
            version = getLatestVersion();
        if (version instanceof LocalVersion localVersion) localDownloader.downloadVersion(localVersion, consumer);
        else if (version instanceof GitHubVersion gitHubVersion)
            gitHubDownloader.downloadVersion(gitHubVersion, consumer);
        else if (version instanceof ItchIoVersion itchIoVersion)
            itchIoDownloader.downloadVersion(itchIoVersion, consumer);
        else throw new CRDownloaderException("Invalid version type");
    }

    public static CosmicReachVersion getLatestVersion() throws CRDownloaderException {
        if (!versions.isEmpty()) {
            CosmicReachVersion tempVersion = null;
            for (CosmicReachVersion cosmicReachVersion : versions) {
                if (!cosmicReachVersion.isExperimental()) {
                    tempVersion = cosmicReachVersion;
                    break;
                }
            }
            if (tempVersion == null) tempVersion = versions.get(0);
            return tempVersion;
        } else throw new CRDownloaderException("No versions available");
    }

    public static CosmicReachVersion getVersion(String version) throws CRDownloaderException {
        if (version.equals("latest")) return getLatestVersion();
        for (CosmicReachVersion cosmicReachVersion : versions) {
            if (cosmicReachVersion.getVersion().equals(version)) return cosmicReachVersion;
        }
        throw new CRDownloaderException("Version not found: " + version);
    }

    private static void addVersions(List<? extends CosmicReachVersion> versions) {
        for (CosmicReachVersion version : versions) {
            boolean exists = false;
            for (CosmicReachVersion existingVersion : DownloadManager.versions) {
                if (existingVersion.getVersion().equals(version.getVersion())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) DownloadManager.versions.add(version);
        }
    }

    public static List<CosmicReachVersion> getVersions() {
        return getVersions(SettingsManager.showExperimentalVersions());
    }

    public static List<CosmicReachVersion> getVersions(boolean experimental) {
        return experimental ? versions : versions.stream().filter(version -> !version.isExperimental()).toList();
    }

    public static CosmicReachVersion getLatestVersionPlaceholder() {
        return latestVersionPlaceholder;
    }
}
