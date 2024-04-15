package multiverse.json;

public class Settings {
    private String lastProfil;
    private String apiKey;
    private String[] themes;
    private int minRam, maxRam;
    private boolean useGitHubVersions, useItchIoVersions, showExperimentalVersions, checkForUpdates;

    public Settings(String lastProfile, String apiKey, int minRam, int maxRam, String[] themes, boolean useGitHubVersions, boolean useItchIoVersions, boolean showExperimentalVersions, boolean checkForUpdates) {
        this.lastProfil = lastProfile;
        this.apiKey = apiKey;
        this.minRam = minRam;
        this.maxRam = maxRam;
        this.themes = themes;
        this.useGitHubVersions = useGitHubVersions;
        this.useItchIoVersions = useItchIoVersions;
        this.showExperimentalVersions = showExperimentalVersions;
        this.checkForUpdates = checkForUpdates;
    }

    public String getLastProfile() {
        return lastProfil;
    }

    public void setLastProfile(String lastProfile) {
        this.lastProfil = lastProfile;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getMaxRam() {
        return maxRam;
    }

    public void setMaxRam(int maxRam) {
        this.maxRam = maxRam;
    }

    public int getMinRam() {
        return minRam;
    }

    public void setMinRam(int minRam) {
        this.minRam = minRam;
    }

    public String[] getThemes() {
        return themes;
    }

    public void setThemes(String[] themes) {
        this.themes = themes;
    }

    public boolean useGitHubVersions() {
        return useGitHubVersions;
    }

    public void setUseGitHubVersions(boolean useGitHubVersions) {
        this.useGitHubVersions = useGitHubVersions;
    }

    public boolean useItchIoVersions() {
        return useItchIoVersions;
    }

    public void setUseItchIoVersions(boolean useItchIoVersions) {
        this.useItchIoVersions = useItchIoVersions;
    }

    public boolean showExperimentalVersions() {
        return showExperimentalVersions;
    }

    public void setShowExperimentalVersions(boolean showExperimentalVersions) {
        this.showExperimentalVersions = showExperimentalVersions;
    }

    public boolean checkForUpdates() {
        return checkForUpdates;
    }

    public void setCheckForUpdates(boolean selected) {
        this.checkForUpdates = selected;
    }
}
