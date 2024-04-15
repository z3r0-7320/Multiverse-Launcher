package multiverse.cr_downloader.crversions;

import com.google.gson.annotations.SerializedName;

public class ItchIoVersion extends CosmicReachVersion {
    @SerializedName("user_version")
    private String userVersion;
    private int id;

    public int getId() {
        return id;
    }

    @Override
    public String getVersion() {
        return userVersion;
    }
}
