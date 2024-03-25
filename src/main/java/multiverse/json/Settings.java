package multiverse.json;

public class Settings {
    private String lastProfil;

    public Settings(String lastProfile) {
        this.lastProfil = lastProfile;
    }

    public String getLastProfile() {
        return lastProfil;
    }

    public void setLastProfile(String lastProfile) {
        this.lastProfil = lastProfile;
    }
}
