package multiverse.crm1.base;

import java.util.List;

public abstract class BaseRepository {
    protected int specVersion;
    protected String rootId;
    protected long lastUpdated;
    protected List<String> deps;

    public int getSpecVersion() {
        return specVersion;
    }

    public String getRootId() {
        return rootId;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public List<String> getDeps() {
        return deps;
    }


    public abstract List<? extends BaseMod> getMods();
}
