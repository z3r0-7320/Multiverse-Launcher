package multiverse.cr_downloader.crversions;

public class GitHubVersion extends CosmicReachVersion {
    private String path;
    private String type;
    private String sha;
    private int size;
    private String url;

    public static boolean isValid(GitHubVersion cosmicReachVersion) {
        return cosmicReachVersion.getPath().endsWith(".jar");
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public String getSha() {
        return sha;
    }

    public int getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getVersion() {
        return path.substring(path.lastIndexOf('-') + 1, path.lastIndexOf('.'));
    }
}
