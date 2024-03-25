package multiverse.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Builds {
    private List<Build> builds;

    public List<Build> getBuilds() {
        return builds;
    }

    public static class Build {
        public static final Build latest = new Build("latest", Integer.MIN_VALUE);
        public static final Build unknown = new Build("Unknown Version", Integer.MIN_VALUE);

        @SerializedName("user_version")
        private String userVersion;
        private int id;

        public Build(String userVersion, int id) {
            this.userVersion = userVersion;
            this.id = id;
        }

        public String getUserVersion() {
            return userVersion;
        }

        public void setId(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return userVersion;
        }
    }
}
