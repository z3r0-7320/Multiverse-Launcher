package multiverse.cr_downloader.crversions;

public abstract class CosmicReachVersion {
    public abstract String getVersion();

    public boolean isExperimental() {
        return !getVersion().matches("\\d+\\.\\d+\\.\\d+");
    }

    @Override
    public String toString() {
        return getVersion();
    }
}
