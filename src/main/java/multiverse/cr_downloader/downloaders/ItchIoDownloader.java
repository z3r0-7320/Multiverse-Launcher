package multiverse.cr_downloader.downloaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import multiverse.Statics;
import multiverse.cr_downloader.crversions.CosmicReachVersion;
import multiverse.cr_downloader.crversions.ItchIoVersion;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.cr_downloader.interfaces.CosmicReachDownloader;
import multiverse.managers.SettingsManager;
import multiverse.utils.Downloader;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static multiverse.Statics.GSON;


public class ItchIoDownloader implements CosmicReachDownloader {
    @Override
    public List<ItchIoVersion> getVersions() throws CRDownloaderException {
        String json = Downloader.downloadAsString(getBuildsUrl());
        if (json == null) throw new CRDownloaderException("Failed to download Itch.io builds");
        JsonArray builds = JsonParser.parseString(json).getAsJsonObject().get("builds").getAsJsonArray();
        if (builds == null) throw new CRDownloaderException("Failed to parse Itch.io builds");
        return GSON.fromJson(builds, new TypeToken<List<ItchIoVersion>>() {
        }.getType());
    }

    public void downloadVersion(CosmicReachVersion version, Consumer<Double> consumer) throws CRDownloaderException {
        if (version instanceof ItchIoVersion itchIoVersion) {
            File downloadFolder = new File(Statics.VERSIONS_DIRECTORY, version.getVersion());
            try {
                downloadFolder.mkdirs();
                byte[] fileData = Downloader.downloadFileToByteArray(getDownloadUrl(itchIoVersion.getId()), consumer);
                extract(fileData, downloadFolder.getAbsolutePath());
            } catch (IOException e) {
                throw new CRDownloaderException("Failed to download Itch.io build", e);
            }
        } else {
            throw new CRDownloaderException("Invalid version type");
        }
    }

    public void extract(byte[] zipData, String outputPath) throws CRDownloaderException {
        try {
            ZipFile.Builder zipFileBuilder = new ZipFile.Builder().setByteArray(zipData);
            try (ZipFile zipFile = zipFileBuilder.get()) {
                Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
                boolean jarFound = false;
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".jar")) {
                        try (InputStream is = zipFile.getInputStream(entry)) {
                            Files.copy(is, Paths.get(outputPath, Statics.COSMIC_REACH_JAR_NAME));
                            jarFound = true;
                            break;
                        }
                    }
                }
                if (!jarFound) {
                    throw new CRDownloaderException("No .jar file found in the zip archive");
                }
            }
        } catch (IOException e) {
            throw new CRDownloaderException("Failed to extract Itch.io build", e);
        }
    }

    private String getBuildsUrl() {
        String apiKey = getApiKey();
        return apiKey == null ? Statics.ITCH_IO_WORKER_BUILDS_URL : Statics.ITCH_IO_BUILDS_URL.formatted(apiKey);
    }

    private String getDownloadUrl(int id) {
        String apiKey = getApiKey();
        return apiKey == null ? Statics.ITCH_IO_WORKER_DOWNLOAD_URL.formatted(id) : Statics.ITCH_IO_DOWNLOAD_URL.formatted(id, apiKey);
    }

    private String getApiKey() {
        return SettingsManager.getApiKey() == null || SettingsManager.getApiKey().isBlank() ? null : SettingsManager.getApiKey();
    }
}
