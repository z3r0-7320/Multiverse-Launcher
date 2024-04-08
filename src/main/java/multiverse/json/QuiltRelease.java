package multiverse.json;

public class QuiltRelease {
    public static final QuiltRelease none = new QuiltRelease("none", true);
    public static final QuiltRelease unknown = new QuiltRelease("unknown", true);
    private String versionNumber;
    private boolean local;

    public QuiltRelease(String versionNumber, boolean local) {
        this.versionNumber = versionNumber;
        this.local = local;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    @Override
    public String toString() {
        return versionNumber;
    }
}
