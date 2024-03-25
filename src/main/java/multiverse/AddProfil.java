package multiverse;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import multiverse.json.Builds;
import multiverse.json.Profile;
import multiverse.json.QuiltRelease;
import multiverse.managers.BuildManager;
import multiverse.managers.ProfileManager;
import multiverse.managers.SettingsManager;
import multiverse.utils.DirectoryDeleter;
import multiverse.utils.ListSorter;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static multiverse.Statics.GSON;
import static multiverse.managers.BuildManager.builds;

public class AddProfil {
    public TextField profileNameField;
    public ComboBox<Builds.Build> versionComboBox;
    public ComboBox<QuiltRelease> modSupportComboBox;
    public Button saveButton;
    public Button cancelButton;
    public Label errorField;
    public ProgressBar progressBar;

    public List<QuiltRelease> quiltReleases;

    public boolean isEdit;
    public Profile profile;

    public AddProfil() {
        quiltReleases = getGitHubReleases();
        isEdit = ProgramState.getCurrentStatus() == ProgramState.ProgramStateEnum.EDIT_PROFILE;
        profile = isEdit ? ProfileManager.getProfile(SettingsManager.getLastProfile()) : null;

        setupProfileNameField(profile);
        setupComboBoxes(profile);
    }

    private void setupProfileNameField(Profile profile) {
        Platform.runLater(() -> {
            profileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.matches("[a-zA-Z0-9]*")) {
                    profileNameField.setText(oldValue);
                }
            });
            if (isEdit && profile != null)
                profileNameField.setText(profile.getName());
        });
    }

    private void setupComboBoxes(Profile profile) {
        boolean networkError = BuildManager.updateBuilds();
        Platform.runLater(() -> {
            versionComboBox.getItems().add(Builds.Build.latest);
            versionComboBox.getItems().addAll(builds);
            versionComboBox.setValue(getVersionComboBoxValue(profile));

            modSupportComboBox.getItems().add(QuiltRelease.none);
            modSupportComboBox.getItems().addAll(quiltReleases);
            modSupportComboBox.setValue(getModSupportComboBoxValue(profile));

            if (networkError)
                errorField.setText("Could not fetch online game versions, only local versions are available.");
        });
    }

    private Builds.Build getVersionComboBoxValue(Profile profile) {
        return isEdit && profile != null ? versionComboBox.getItems().stream().filter(build -> build.getUserVersion().equals(profile.getVersion())).findFirst().orElse(Builds.Build.unknown) : Builds.Build.latest;
    }

    private QuiltRelease getModSupportComboBoxValue(Profile profile) {
        return isEdit && profile != null && profile.useQuilt() ? modSupportComboBox.getItems().stream().filter(build -> build.getTagName().equals(profile.getQuiltLoaderVersion())).findFirst().orElse(QuiltRelease.unknown) : QuiltRelease.none;
    }

    public static List<QuiltRelease> getGitHubReleases() {
        boolean networkError = false;
        File[] directories = Statics.QUILT_DIRECTORY.listFiles();
        List<QuiltRelease> releases = new ArrayList<>();
        if (directories != null) {
            for (File file : directories) {
                if (new File(file, Statics.QUILT_LOADER_JAR_NAME).exists()) {
                    releases.add(new QuiltRelease("", file.getName(), null));
                }
            }
        }

        try {
            URL obj = new URL("https://api.github.com/repos/z3r0-7320/Cosmic-Quilt-Loader/releases");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                for (QuiltRelease build : GSON.fromJson(response.toString(), QuiltRelease[].class)) {
                    boolean found = false;
                    for (QuiltRelease localBuild : releases) {
                        if (localBuild.getTagName().equals(build.getTagName())) {
                            localBuild.setAssets(build.getAssets());
                            found = true;
                            break;
                        }
                    } if (!found) {
                        releases.add(build);
                    }
                }
            }
        } catch (IOException ignored) {
            networkError = true;
        }
        releases.sort(ListSorter::compare);
        return releases;
    }

    private static boolean unzip(String zipFilePath, File destDir) {
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        return false;
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        return false;
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public void handleSave(ActionEvent actionEvent) {
        String profileName = profileNameField.getText();
        if (profileName.isEmpty()) {
            errorField.setText("Profile name cannot be empty");
            return;
        }

        // int selectedBuildIndex = versionComboBox.getSelectionModel().getSelectedIndex();
        //Builds.Build build = builds.get(selectedBuildIndex - 1 == -1 ? 0 : selectedBuildIndex - 1);
        Builds.Build build = versionComboBox.getSelectionModel().getSelectedItem();
        File dir = new File(Statics.VERSIONS_DIRECTORY, (build.equals(Builds.Build.latest) ? builds.isEmpty() ? "0.0.0" : builds.get(0).getUserVersion() : build.getUserVersion()));
        File jar = new File(dir, Statics.COSMIC_REACH_JAR_NAME);
        File profileDir = new File(Statics.PROFILES_DIRECTORY, profileName);

        if (profileDir.exists() && !isEdit) {
            errorField.setText("Profile already exists");
            return;
        }


        QuiltRelease quiltRelease = modSupportComboBox.getSelectionModel().getSelectedItem();
        boolean quilt = !quiltRelease.equals(QuiltRelease.none);

        new Thread(() -> {
            boolean error = false;
            disableUI(true);
            if (!builds.isEmpty() && build.getId() != 0 && (dir.mkdirs() || !jar.exists())) {
                try {
                    if (download(new URL("https://workers-playground-dawn-pond-be0d.cosmicreachdl.workers.dev/download/" + (build.equals(Builds.Build.latest) ? builds.get(0).getId() : build.getId())), dir.getPath(), "cosmic_reach.zip"))
                        if (unzip(new File(dir, "cosmic_reach.zip").getPath(), dir)) {
                            File cosmicReachJar = null;
                            List<File> otherFiles = new ArrayList<>();
                            for (File file : Objects.requireNonNullElseGet(dir.listFiles(), () -> new File[0])) {
                                if (file.getName().endsWith(".jar")) {
                                    cosmicReachJar = file;
                                } else otherFiles.add(file);
                            }
                            if (cosmicReachJar == null || !cosmicReachJar.renameTo(new File(dir, Statics.COSMIC_REACH_JAR_NAME))) {
                                error = showError("Failed to rename jar");
                            }
                            new File(dir, "cosmic_reach.zip").delete();
                            for (File file : otherFiles)
                                file.delete();
                        } else error = showError("Failed to unzip");
                    else error = showError("Failed to download");
                } catch (MalformedURLException ignored) {
                }
            }
            if (error) DirectoryDeleter.deleteDir(dir);
            boolean b = !quilt;
            if (!error && quilt) {
                File quiltFolder = new File(Statics.QUILT_DIRECTORY, quiltRelease.getTagName());
                File quiltJar = new File(quiltFolder, Statics.QUILT_LOADER_JAR_NAME);
                if (quiltRelease.getAssets() != null && (quiltFolder.mkdirs() || !quiltJar.exists())) {
                    try {
                        if (download(new URL(quiltRelease.getAssets()[0].getBrowserDownloadUrl()), quiltFolder.getPath(), "CosmicQuiltLoader.zip"))
                            if (unzip(new File(quiltFolder, "CosmicQuiltLoader.zip").getPath(), quiltFolder))
                                if (downloadDependencies(quiltFolder))
                                    if (findJarInFolderAndRename(quiltFolder, Statics.QUILT_LOADER_JAR_NAME)) {
                                        new File(quiltFolder, "CosmicQuiltLoader.zip").delete();
                                        new File(quiltFolder, "dependencies.txt").delete();
                                        b = true;
                                    } else error = showError("Failed to rename Quilt Loader");
                                else error = showError("Failed to download dependencies");
                            else error = showError("Failed to unzip quilt");
                        else error = showError("Failed to download quilt");
                    } catch (MalformedURLException ignored) {
                    }
                } else b = true;
                if (error) DirectoryDeleter.deleteDir(quiltFolder);
            }
            if ((jar.exists() || builds.isEmpty()) && b)//&& !profileDir.exists() || isEdit
                if (isEdit) {
                    if ((!profileName.equals(profile.getName()) || (!build.equals(Builds.Build.unknown) && !build.getUserVersion().equals(profile.getVersion())) || (!quiltRelease.equals(QuiltRelease.unknown) && !quiltRelease.getTagName().equals(profile.getQuiltLoaderVersion())))) {
                        if (ProfileManager.editProfile(profile, profileName, build.getUserVersion(), quilt, quiltRelease.getTagName()) != null)
                            closeWindow();
                        else showError("Failed to edit profile");
                    } else closeWindow();
                } else if (!profileDir.exists())
                    if (ProfileManager.createProfile(profileName, build.getUserVersion(), quilt, quiltRelease.getTagName()) != null)
                        closeWindow();
                    else showError("Failed to create profile");
                else showError("Profile already exists");
            else showError("Something went wrong...");
            disableUI(false);
        }).start();
    }

    public boolean findJarInFolderAndRename(File dir, String newName) {
        if (dir == null || !dir.isDirectory()) return false;
        for (File file : Objects.requireNonNullElseGet(dir.listFiles(), () -> new File[0])) {
            if (file.getName().endsWith(".jar")) {
                return file.renameTo(new File(dir, newName));//file.renameTo(new File(dir, newName));
            }
        }
        return false;
    }

    private void disableUI(boolean disable) {
        Platform.runLater(() -> {
            saveButton.setDisable(disable);
            profileNameField.setDisable(disable);
        });
    }

    private boolean showError(String message) {
        Platform.runLater(() -> errorField.setText(message));
        return true;
    }

    private void closeWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        });
    }

    public void handleCancel(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private boolean download(URL url, String path, String name) {
        boolean r = true;
        progressBar.setVisible(true);
        try {
            HttpsURLConnection httpsConn;
            httpsConn = (HttpsURLConnection) url.openConnection();
            int responseCode = httpsConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpsConn.getInputStream();
                String saveFilePath = path + File.separator + name;
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);
                int bytesRead;
                byte[] buffer = new byte[4096];
                long totalBytesRead = 0;
                long fileSize = httpsConn.getContentLength();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    double percent = (double) totalBytesRead / fileSize;
                    Platform.runLater(() -> progressBar.setProgress(percent));
                }
                outputStream.close();
                inputStream.close();
            } else {
                r = false;
            }
            httpsConn.disconnect();
        } catch (IOException e) {
            r = false;
        }
        return r;
    }

    private boolean downloadDependencies(File quiltDir) {
        if (!quiltDir.isDirectory()) return false;
        File dependencies = new File(quiltDir, "dependencies.txt");
        if (!dependencies.exists()) return false;
        try (Stream<String> lines = Files.lines(dependencies.toPath(), StandardCharsets.UTF_8)) {
            return lines.allMatch(line -> {
                File depsDir = new File(quiltDir, "deps");
                depsDir.mkdirs();
                try {
                    return download(new URL(line), depsDir.getPath(), line.substring(line.lastIndexOf("/") + 1));
                } catch (MalformedURLException ignored) {
                    return false;
                }
            });
        } catch (IOException ignored) {
        }
        return false;
    }
}
