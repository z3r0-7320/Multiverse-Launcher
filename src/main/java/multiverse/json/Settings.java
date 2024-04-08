package multiverse.json;

public class Settings {
    private String lastProfil;
    private String apiKey;
    private int minRam, maxRam;

    public Settings(String lastProfile, String apiKey, int minRam, int maxRam) {
        this.lastProfil = lastProfile;
        this.apiKey = apiKey;
        this.minRam = minRam;
        this.maxRam = maxRam;
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
}
