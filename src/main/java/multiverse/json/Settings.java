package multiverse.json;

public class Settings {
    private String lastProfil;
    private String apiKey;

    public Settings(String lastProfile, String apiKey) {
        this.lastProfil = lastProfile;
        this.apiKey = apiKey;
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
}
