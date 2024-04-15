package multiverse.json;

public class QuiltRelease {
    public static final QuiltRelease none = new QuiltRelease("none", true);
    public static final QuiltRelease unknown = new QuiltRelease("unknown", true);
    private final String versionNumber;
    private final boolean local;

    public QuiltRelease(String versionNumber, boolean local) {
        this.versionNumber = versionNumber;
        this.local = local;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public boolean isLocal() {
        return local;
    }

    @Override
    public String toString() {
        return versionNumber;
    }
}
