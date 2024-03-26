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

    /*public static final QuiltRelease none = new QuiltRelease("", "none", null);
    public static final QuiltRelease unknown = new QuiltRelease("", "Unknown Version", null);
    private final String name;
    @SerializedName("tag_name")
    private String tagName;
    @SerializedName("assets")
    private Asset[] assets;

    public QuiltRelease(String name, String tagName, Asset[] assets) {
        this.name = name;
        this.tagName = tagName;
        this.assets = assets;
    }

    public String getName() {
        return name;
    }

    public String getTagName() {
        return tagName;
    }

    public Asset[] getAssets() {
        return assets;
    }

    public void setAssets(Asset[] assets) {
        this.assets = assets;
    }

    @Override
    public String toString() {
        return tagName;
    }

    public static class Asset {
        @SerializedName("browser_download_url")
        private String browserDownloadUrl;

        public String getBrowserDownloadUrl() {
            return browserDownloadUrl;
        }
    }*/
}
