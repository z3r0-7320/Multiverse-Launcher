package multiverse.json;

import com.google.gson.annotations.SerializedName;

public class QuiltRelease {
    public static final QuiltRelease none = new QuiltRelease("", "none", null);
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
    }
}
