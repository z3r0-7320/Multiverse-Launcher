package multiverse.crm1;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import multiverse.Statics;
import multiverse.cr_downloader.DownloadManager;
import multiverse.cr_downloader.exceptions.CRDownloaderException;
import multiverse.crm1.base.BaseMod;
import multiverse.json.Profile;
import multiverse.managers.ProfileManager;
import multiverse.utils.ImageUtil;
import multiverse.utils.VersionComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static multiverse.crm1.CRM1.getMods;
import static multiverse.utils.NodeUtils.roundedNode;

public class ModView {
    private static final int entriesPerPage = 20;
    private static final PauseTransition pause = new PauseTransition(Duration.seconds(2));
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final ObjectProperty<Boolean> update = new SimpleObjectProperty<>(false);
    private static final ObjectProperty<Boolean> updateButtons = new SimpleObjectProperty<>(false);
    private final ScrollPane root;
    private final VBox modEntryContainer;
    private final HBox navigationButtons;
    private final TextField searchBar;
    private final Button reload;
    private final CheckBox modLoader;
    private final CheckBox vanilla;
    private final ModEntry[] modEntries = new ModEntry[entriesPerPage];
    private Label text;

    public ModView(ScrollPane root, VBox modEntries, HBox navigationButtons, TextField searchBar, Button reload, CheckBox modLoader, CheckBox vanilla) throws CRDownloaderException {
        this.root = root;
        this.modEntryContainer = modEntries;
        this.navigationButtons = navigationButtons;
        this.searchBar = searchBar;
        this.reload = reload;
        this.modLoader = modLoader;
        this.vanilla = vanilla;
        executorService.submit(() -> {
            try {
                DownloadManager.update();
                CRM1.update();
            } catch (CRDownloaderException ignored) {
            }
            Platform.runLater(() -> {
                createModEntries();
                text = new Label();
                modEntryContainer.getChildren().add(text);
                update(1, true);
            });
        });
        addListener();
    }

    public static void setUpdate(boolean shouldUpdate) {
        update.set(shouldUpdate);
    }

    public static void setUpdateButtons(boolean shouldUpdate) {
        updateButtons.set(shouldUpdate);
    }

    private void addListener() {
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event -> {
                if (newValue.isEmpty() || newValue.length() > 2) {
                    update(1, true);
                }
            });
            pause.playFromStart();
        });
        reload.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                CRM1.update();
                searchBar.setText("");
                update(1, true);
            } catch (CRDownloaderException ignored) {
            }
        });
        modLoader.selectedProperty().addListener((observable, oldValue, newValue) -> update(1, true));
        vanilla.selectedProperty().addListener((observable, oldValue, newValue) -> update(1, true));
        update.set(false);
        update.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                update(1, true);
                update.set(false);
            }
        });
        updateButtons.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    for (ModEntry modEntry : modEntries)
                        updateInstallationButton(modEntry, modEntry.getMod());
                });
                updateButtons.set(false);
            }
        });
        ProfileManager.getObservableProfiles().addListener((ListChangeListener<? super Profile>) observable -> Platform.runLater(() -> updateEntries(true)));
        ProfileManager.currentProfileProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> updateEntries(true)));
    }

    @SuppressWarnings("unchecked")
    private void update(int page, boolean reloadNavigationButtons) {
        List<? extends BaseMod> mods = getMods().stream().filter(mod -> (searchBar.getText().isEmpty() || (Objects.requireNonNullElseGet(mod.getName(), String::new).toLowerCase().contains(searchBar.getText().toLowerCase()) || mod.getId().contains(searchBar.getText()) || Objects.requireNonNullElseGet(mod.getDesc(), String::new).toLowerCase().contains(searchBar.getText()))) && (((modLoader.isSelected() && mod.isModLoader()) || (vanilla.isSelected() && mod.isData())))).toList();
        if (reloadNavigationButtons) setNavigationButtons(mods.size());
        List<BaseMod> pageMods = (List<BaseMod>) mods.stream().skip((long) (page - 1) * entriesPerPage).limit(entriesPerPage).toList();
        if (pageMods.isEmpty()) setMessage("No mods found");
        else fill(pageMods);
    }

    private void setMessage(String message) {
        for (ModEntry modEntry : modEntries) {
            hideModEntry(modEntry);
        }
        text.setText(message);
        text.setManaged(true);
        Platform.runLater(() -> text.setVisible(true));
    }

    public void updateEntries(boolean updateInstallButton) {
        for (ModEntry modEntry : modEntries) {
            if (modEntry.getMod() == null) continue;
            modEntry.getVersionComboBox().setValue(modEntry.getMod());
            modEntry.getVersionComboBox().setValue(null);
            modEntry.getVersionComboBox().getItems().clear();
            List<BaseMod> versions = new ArrayList<>();
            versions.add(modEntry.getMod());
            //versions.sort(VersionComparator::compare);
            if (modEntry.getMod().getExt().getAlt_versions() != null)
                versions.addAll(modEntry.getMod().getExt().getAlt_versions());
            modEntry.getVersionComboBox().getItems().addAll(versions);
            if (updateInstallButton) updateInstallationButton(modEntry, modEntry.getMod());
        }
    }

    private void setNavigationButtons(int modsSize) {
        navigationButtons.getChildren().clear();
        int pages = (int) Math.ceil((double) modsSize / entriesPerPage);
        for (int i = 1; i <= pages; i++) {
            Button pageButton = new Button(String.valueOf(i));
            pageButton.setFocusTraversable(false);
            if (i == 1) pageButton.setDisable(true);
            pageButton.addEventFilter(ActionEvent.ACTION, event -> {
                navigationButtons.getChildren().forEach(button -> button.setDisable(false));
                ((Button) event.getTarget()).setDisable(true);
                update(Integer.parseInt(((Button) event.getTarget()).getText()), false);
                root.setVvalue(root.vminProperty().get());
            });
            navigationButtons.getChildren().add(pageButton);
        }
    }

    private void createModEntries() {
        for (int i = 0; i < entriesPerPage; i++) {
            modEntries[i] = new ModEntry(executorService);
        }
        modEntryContainer.getChildren().addAll(modEntries);
    }

    private void fill(List<BaseMod> mods) {
        executorService.submit(() -> {
            modEntryContainer.setVisible(false);
            for (int i = 0; i < entriesPerPage; i++) {
                ModEntry modEntry = modEntries[i];
                if (modEntry == null) return;
                if (i >= mods.size()) {
                    hideModEntry(modEntry);
                    continue;
                }
                BaseMod mod = mods.get(i);
                modEntry.setMod(mod);
                Platform.runLater(() -> {
                    modEntry.getTitleTextField().setText(mod.getName());
                    if (!mod.getAuthors().isEmpty())
                        modEntry.getTitleAuthorTextField().setText(" by %s".formatted(String.join(", ", mod.getAuthors())));
                    modEntry.getDescriptionTextArea().setText(mod.getDesc());
                    modEntry.getType().setText(mod.isModLoader() ? "Quilt/Fabric" : "Data");
                    List<BaseMod> versions = new ArrayList<>();
                    versions.add(mod);
                    if (mod.getExt().getAlt_versions() != null) versions.addAll(mod.getExt().getAlt_versions());
                    versions.sort(VersionComparator::compare);
                    modEntry.getVersionComboBox().getItems().clear();
                    modEntry.getVersionComboBox().getItems().addAll(versions);
                    modEntry.getVersionComboBox().setValue(mod);
                    modEntry.getVersionComboBox().setValue(null);
                    updateInstallationButton(modEntry, mod);
                });
                ImageUtil.cropToAspectRatio(modEntry.getImageView(), ImageUtil.scale(ImageUtil.loadImage(mod.getExt().getImages() != null && !mod.getExt().getImages().isEmpty() ? mod.getExt().getImages().get(0) : null, mod.getExt().getIcon()), 231), 16, 9);
                modEntry.setManaged(true);
                Platform.runLater(() -> modEntry.setVisible(true)); // needed to prevent flickering
            }
            text.setVisible(false);
            text.setManaged(false);
            Platform.runLater(() -> modEntryContainer.setVisible(true));
        });
    }

    private void hideModEntry(ModEntry modEntry) {
        modEntry.setMod(null);
        modEntry.setVisible(false);
        modEntry.setManaged(false);
    }

    private void updateInstallationButton(ModEntry modEntry, BaseMod mod) {
        if (ProfileManager.getCurrentProfile() == null)
            modEntry.setButtonState(ModEntry.ButtonState.NO_PROFILE_SELECTED);
        else if (!ProfileManager.getCurrentProfile().useQuilt())
            modEntry.setButtonState(ModEntry.ButtonState.NO_QUILT);
        else if (CRM1.modAlreadyDownloaded(new File(Statics.PROFILES_DIRECTORY, ProfileManager.getCurrentProfile().getName() + "/mods"), mod, false))
            modEntry.setButtonState(ModEntry.ButtonState.UNINSTALL);
        else modEntry.setButtonState(ModEntry.ButtonState.INSTALL);
    }

    static class ModEntry extends HBox {
        private final ImageView imageView;
        private final Label titleTextField;
        private final Label titleAuthorTextField;
        private final TextArea descriptionTextArea;
        private final Label type;
        private final ComboBox<BaseMod> versionComboBox;
        private final Button installButton;
        private BaseMod mod;
        private ButtonState buttonState;

        public ModEntry(ExecutorService executorService) {
            setVisible(false);
            setManaged(false);
            setAlignment(Pos.CENTER);
            setSpacing(0);
            setMinHeight(150);
            setMaxHeight(150);
            setPadding(new Insets(10, 10, 10, 10));

            imageView = new ImageView();
            imageView.setFitHeight(130);
            imageView.setPreserveRatio(true);

            getChildren().add(roundedNode(imageView, 231.1, 130));

            VBox middle = new VBox();
            middle.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(middle, Priority.ALWAYS);
            HBox.setMargin(middle, new Insets(10, 0, 10, 0));

            HBox titleBox = new HBox();
            titleBox.setAlignment(Pos.CENTER_LEFT);
            titleTextField = new Label();
            titleTextField.setPadding(new Insets(0, 0, 0, 8));
            titleAuthorTextField = new Label();
            type = new Label();
            HBox.setMargin(type, new Insets(0, 0, 0, 7));
            type.setPadding(new Insets(0, 6, 0, 6));
            type.setStyle("-fx-border-width: 3px; -fx-border-insets: -3; -fx-background-color: -root-bg-color; -fx-border-color: -root-bg-color; -fx-border-radius: 10;");
            titleBox.getChildren().addAll(titleTextField, titleAuthorTextField, type);
            middle.getChildren().add(titleBox);

            descriptionTextArea = new TextArea();
            descriptionTextArea.setWrapText(true);
            descriptionTextArea.setEditable(false);
            VBox.setVgrow(descriptionTextArea, Priority.ALWAYS);
            middle.getChildren().add(descriptionTextArea);
            getChildren().add(middle);

            versionComboBox = new ComboBox<>();
            HBox.setMargin(versionComboBox, new Insets(0, 10, 0, 0));
            versionComboBox.getStyleClass().add("mod-version-combobox");
            versionComboBox.setPromptText("Select Version");
            versionComboBox.setMinWidth(150);
            versionComboBox.setMaxWidth(150);
            versionComboBox.setButtonCell(new ListCell<>() {
                @Override
                public void updateItem(BaseMod item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Version");
                    } else {
                        setText(item.toString());
                    }
                }
            });
            versionComboBox.setCellFactory(new Callback<>() {
                @Override
                public javafx.scene.control.ListCell<BaseMod> call(ListView<BaseMod> baseModListView) {
                    return new ListCell<>() {
                        @Override
                        public void updateItem(BaseMod item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                setStyle("");
                            } else {
                                setText(item.getVersion());
                                String color;
                                try {
                                    color = switch (VersionComparator.modVersionCompatibleToGameVersion(item.getGameVersion(), ProfileManager.getCurrentProfile() != null ? ProfileManager.getCurrentProfile().getVersion().equals("latest") ? DownloadManager.getLatestVersion().getVersion() : ProfileManager.getCurrentProfile().getVersion() : null)) {
                                        case 2 -> "#97D700";
                                        case 1 -> "#FFEB3B";
                                        default -> "#D32F2F";
                                    };
                                } catch (CRDownloaderException e) {
                                    color = "#D32F2F";
                                }
                                setStyle("-fx-text-fill:%s;".formatted(color));
                            }
                        }
                    };
                }
            });
            installButton = new Button();
            setFocusTraversable(false);
            HBox.setMargin(installButton, new Insets(0, 10, 0, 0));
            installButton.addEventFilter(ActionEvent.ACTION, event -> executorService.submit(() -> {
                try {
                    if (ProfileManager.getCurrentProfile() != null)
                        if (versionComboBox.getValue() != null && buttonState == ButtonState.INSTALL) {
                            Platform.runLater(() -> setButtonState(ButtonState.DOWNLOADING));
                            CRM1.downloadMod(new File(Statics.PROFILES_DIRECTORY, ProfileManager.getCurrentProfile().getName() + "/mods"), versionComboBox.getValue(), null);
                            setUpdateButtons(true);
                        } else if (buttonState == ButtonState.UNINSTALL)
                            if (CRM1.modAlreadyDownloaded(new File(Statics.PROFILES_DIRECTORY, ProfileManager.getCurrentProfile().getName() + "/mods"), mod, true))
                                Platform.runLater(() -> setButtonState(ButtonState.INSTALL));
                } catch (CRDownloaderException ignored) {
                    Platform.runLater(() -> setButtonState(ButtonState.FAILED));
                }
            }));
            getChildren().addAll(versionComboBox, installButton);
            getStyleClass().add("mod-entry");


            imageView.setFocusTraversable(false);
            titleTextField.setFocusTraversable(false);
            titleAuthorTextField.setFocusTraversable(false);
            descriptionTextArea.setFocusTraversable(false);
            type.setFocusTraversable(false);
            versionComboBox.setFocusTraversable(false);
            installButton.setFocusTraversable(false);
            setFocusTraversable(false);
        }

        public BaseMod getMod() {
            return mod;
        }

        public void setMod(BaseMod mod) {
            this.mod = mod;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public Label getTitleTextField() {
            return titleTextField;
        }

        public Label getTitleAuthorTextField() {
            return titleAuthorTextField;
        }

        public TextArea getDescriptionTextArea() {
            return descriptionTextArea;
        }

        public Label getType() {
            return type;
        }

        public ComboBox<BaseMod> getVersionComboBox() {
            return versionComboBox;
        }

        public Button getInstallButton() {
            return installButton;
        }

        public void setButtonState(ButtonState buttonState) {
            buttonState.run(versionComboBox, installButton);
            this.buttonState = buttonState;
        }

        public enum ButtonState {
            INSTALL("Install", false, false),
            DOWNLOADING("Downloading...", true, true),
            FAILED("Failed", false, false),
            UNINSTALL("Uninstall", true, false),
            NO_QUILT("No Quilt", true, true),
            NO_PROFILE_SELECTED("No Profile", true, true);
            private final String buttonText;
            private final boolean disableVersionSelector;
            private final boolean disableInstallButton;

            ButtonState(String buttonText, boolean disableVersionSelector, boolean disableInstallButton) {
                this.buttonText = buttonText;
                this.disableVersionSelector = disableVersionSelector;
                this.disableInstallButton = disableInstallButton;
            }

            public void run(ComboBox<BaseMod> versionComboBox, Button installButton) {
                versionComboBox.setDisable(disableVersionSelector);
                installButton.setDisable(disableInstallButton);
                installButton.setText(buttonText);
                installButton.setMinWidth(Control.USE_PREF_SIZE);
            }

            @Override
            public String toString() {
                return buttonText;
            }
        }

        static class ListCell<T> extends javafx.scene.control.ListCell<T> {
            public ListCell() {
                super();
            }

            public void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
            }
        }
    }
}
