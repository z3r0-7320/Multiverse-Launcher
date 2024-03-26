package multiverse.json;

import java.util.Objects;

public class Profile {
    private String name;
    private String version;
    private String gameDir;
    private boolean useQuilt;
    private String quiltLoaderVersion;

    public Profile(String name, String version, String gameDir, boolean useQuilt, String quiltLoaderVersion) {
        this.name = name;
        this.version = version;
        this.gameDir = gameDir;
        this.useQuilt = useQuilt;
        this.quiltLoaderVersion = quiltLoaderVersion;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGameDir() {
        return gameDir;
    }

    public void setGameDir(String gameDir) {
        this.gameDir = gameDir;
    }

    public boolean useQuilt() {
        return useQuilt;
    }

    public String getQuiltLoaderVersion() {
        return quiltLoaderVersion;
    }

    public void setQuiltLoaderVersion(String quiltLoaderVersion) {
        this.quiltLoaderVersion = quiltLoaderVersion;
    }

    public void setUseQuilt(boolean useQuilt) {
        this.useQuilt = useQuilt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Profile) obj;
        return Objects.equals(this.name, that.name) &&
               Objects.equals(this.version, that.version) &&
               Objects.equals(this.gameDir, that.gameDir) &&
               this.useQuilt == that.useQuilt &&
               Objects.equals(this.quiltLoaderVersion, that.quiltLoaderVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, gameDir, useQuilt, quiltLoaderVersion);
    }

}
