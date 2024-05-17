package multiverse.crm1.ext;

import java.util.List;

public class Ext {
    private String icon;
    private List<String> images;
    private String loader;
    private String source;
    private String changelog;
    private long published_at;
    private List<String[]> alt_download;
    private List<ExtAltVersion> alt_versions;

    public String getIcon() {
        return icon;
    }

    public List<String> getImages() {
        return images;
    }

    public String getLoader() {
        return loader;
    }

    public String getSource() {
        return source;
    }

    public String getChangelog() {
        return changelog;
    }

    public long getPublished_at() {
        return published_at;
    }

    public List<String[]> getAlt_download() {
        return alt_download;
    }

    public List<ExtAltVersion> getAlt_versions() {
        return alt_versions;
    }
}
