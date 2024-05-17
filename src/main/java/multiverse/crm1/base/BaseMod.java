package multiverse.crm1.base;

import multiverse.crm1.ext.Ext;
import multiverse.utils.URLUtil;

import java.util.List;

public abstract class BaseMod {
    protected String id;
    protected String name;
    protected String desc;
    protected List<String> authors;
    protected String version;
    protected String gameVersion;
    protected String url;
    protected Ext ext;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getVersion() {
        return version;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getUrl() {
        return url;
    }

    public abstract List<? extends BaseDependency> getDeps();

    public Ext getExt() {
        return ext;
    }

    @Override
    public String toString() {
        return version;
    }

    public boolean isModLoader() {
        if (ext == null || ext.getLoader() == null) {
            return URLUtil.getFileName(url).toLowerCase().endsWith(".jar");
        } else {
            return ext.getLoader().toLowerCase().contains("quilt") || ext.getLoader().toLowerCase().contains("fabric");
        }
    }

    public boolean isData() {
        if (ext == null || ext.getLoader() == null) {
            return URLUtil.getFileName(url).toLowerCase().endsWith(".zip");
        } else {
            return ext.getLoader().toLowerCase().contains("vanilla") || ext.getLoader().toLowerCase().contains("data");
        }
    }
}
