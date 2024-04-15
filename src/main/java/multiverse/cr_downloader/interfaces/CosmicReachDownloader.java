package multiverse.cr_downloader.interfaces;

import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.exceptions.CRDownloaderException;

import java.util.List;
import java.util.function.Consumer;

public interface CosmicReachDownloader {
    List<? extends CosmicReachVersion> getVersions() throws CRDownloaderException;

    void downloadVersion(CosmicReachVersion version, Consumer<Double> consumer) throws CRDownloaderException;
}
