package multiverse.cr_downloader.downloaders;

import multiverse.Statics;
import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.crversions.LocalVersion;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.cr_downloader.interfaces.CosmicReachDownloader;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class LocalDownloader implements CosmicReachDownloader {
    private static final Field versionField;

    static {
        try {
            versionField = LocalVersion.class.getDeclaredField("version");
            versionField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LocalVersion> getVersions() {
        return Arrays.stream(Objects.requireNonNullElseGet(Statics.VERSIONS_DIRECTORY.listFiles(File::isDirectory), () -> new File[0])).filter(file -> new File(file, Statics.COSMIC_REACH_JAR_NAME).isFile()).map(file -> {
            LocalVersion version = new LocalVersion();
            try {
                versionField.set(version, file.getName());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return version;
        }).toList();
    }

    @Override
    public void downloadVersion(CosmicReachVersion version, Consumer<Double> consumer) throws CRDownloaderException {
        if (version instanceof LocalVersion localVersion) {
            if (!new File(new File(Statics.VERSIONS_DIRECTORY, localVersion.getVersion()), Statics.COSMIC_REACH_JAR_NAME).isFile())
                throw new CRDownloaderException("Version does not exist");
        } else throw new CRDownloaderException("Invalid version type");
    }
}
