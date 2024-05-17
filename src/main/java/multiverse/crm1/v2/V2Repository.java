package multiverse.crm1.v2;

import multiverse.crm1.base.BaseRepository;

import java.util.List;

public class V2Repository extends BaseRepository {
    private List<V2Mod> mods;

    @Override
    public List<V2Mod> getMods() {
        return this.mods;
    }
}
