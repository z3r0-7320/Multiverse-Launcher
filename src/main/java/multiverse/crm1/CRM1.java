package multiverse.crm1;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import multiverse.Statics;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.crm1.base.BaseDependency;
import multiverse.crm1.base.BaseMod;
import multiverse.crm1.base.BaseRepository;
import multiverse.crm1.v2.V2Repository;
import multiverse.managers.SettingsManager;
import multiverse.utils.Downloader;
import multiverse.utils.VersionComparator;
import org.hjson.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CRM1 {
    private static final Map<String, BaseRepository> repos = new HashMap<>();

    public static void update() throws CRDownloaderException {
        repos.clear();
        for (String url : SettingsManager.getModRepos()) {
            addRepository(url, 0);
        }
    }

    private static void addRepository(String url, int depth) throws CRDownloaderException {
        if (depth > 5) return;
        try {
            if (url.isEmpty()) return;
            URL u = new URI(url).toURL();
            String hjson = Downloader.downloadAsString(u);
            if (hjson == null) throw new CRDownloaderException("Could not download repo.hjson");
            String json = JsonValue.readHjson(hjson).toString();
            JsonObject repo = JsonParser.parseString(json).getAsJsonObject();
            int version = repo.get("specVersion").getAsInt();
            BaseRepository repository = Statics.GSON.fromJson(json, V2Repository.class);
            if (repos.containsKey(repository.getRootId())) return;
            repos.put(repository.getRootId(), repository);
            if (version > 1 && repository.getDeps() != null)
                for (String dep : repository.getDeps())
                    addRepository(dep, depth + 1);
        } catch (MalformedURLException | URISyntaxException ignored) {
        }
    }

    public static List<? extends BaseMod> getMods() {
        return repos.values().stream().map(BaseRepository::getMods).flatMap(Collection::stream).filter(mod -> mod.getName() != null && mod.getUrl() != null).toList();
    }

    public static void downloadMod(File downloadDir, BaseMod mod, Consumer<Double> consumer) throws CRDownloaderException {
        System.out.println("Downloading " + mod.getId() + " " + mod.getVersion());
        if (mod.isModLoader()) {
            if (!Downloader.downloadFile(mod.getUrl(), downloadDir, mod.getId() + "-" + mod.getVersion() + ".jar", consumer))
                throw new CRDownloaderException("Could not download " + mod.getId());
        } else if (mod.isData()) {
            try {
                byte[] buffer = Downloader.downloadFileToByteArray(mod.getUrl());
                File assets = new File(downloadDir, "assets");
                try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(buffer))) {
                    ZipEntry zipEntry = zis.getNextEntry();
                    while (zipEntry != null) {
                        String fileName = zipEntry.getName();
                        if (fileName.startsWith("mods/assets/")) {
                            fileName = fileName.substring(12);
                        } else if (fileName.startsWith("assets/")) {
                            fileName = fileName.substring(7);
                        }
                        File newFile = new File(assets, fileName);
                        if (zipEntry.isDirectory()) {
                            if (!newFile.isDirectory() && !newFile.mkdirs()) {
                                throw new IOException("Failed to create directory " + newFile);
                            }
                        } else {
                            File parent = newFile.getParentFile();
                            if (!parent.isDirectory() && !parent.mkdirs()) {
                                throw new IOException("Failed to create directory " + parent);
                            }
                            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                byte[] buf = new byte[1024];
                                int length;
                                while ((length = zis.read(buf)) > 0) {
                                    fos.write(buf, 0, length);
                                }
                            }
                        }
                        zipEntry = zis.getNextEntry();
                    }
                }
            } catch (IOException e) {
                throw new CRDownloaderException("Could not download and extract " + mod.getId(), e);
            }
        }
        for (BaseDependency dependency : mod.getDeps()) {
            if (repos.containsKey(dependency.getSource()))
                repos.get(dependency.getSource()).getMods().stream().filter(m -> m.getId().equals(dependency.getId())).findFirst().ifPresent(m -> {
                    try {
                        if (!modAlreadyDownloaded(downloadDir, m, false)) {
                            if (VersionComparator.modVersionCompatibleToGameVersion(dependency.getVersion(), m.getVersion()) == 2)
                                downloadMod(downloadDir, m, consumer);
                            else if (m.getExt() != null && m.getExt().getAlt_versions() != null && !m.getExt().getAlt_versions().isEmpty())
                                m.getExt().getAlt_versions().stream().filter(m1 -> VersionComparator.modVersionCompatibleToGameVersion(dependency.getVersion(), m1.getVersion()) == 2).findFirst().ifPresent(v -> {
                                    try {
                                        downloadMod(downloadDir, v, consumer);
                                    } catch (CRDownloaderException ignored) {
                                    }
                                });
                        }
                    } catch (CRDownloaderException ignored) {
                    }
                });
        }
    }

    public static boolean modAlreadyDownloaded(File downloadDir, BaseMod mod, boolean deleteIfFound) {
        return modAlreadyDownloaded(downloadDir, mod.getId(), deleteIfFound);
    }

    public static boolean modAlreadyDownloaded(File downloadDir, String modId, boolean deleteIfFound) {
        for (File file : Objects.requireNonNullElseGet(downloadDir.listFiles(), () -> new File[0])) {
            if (file.getName().startsWith(modId + "-") && file.getName().endsWith(".jar"))
                return !deleteIfFound || file.delete();
        }
        return false;
    }
}
