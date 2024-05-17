package multiverse.crm1.v2;

import multiverse.crm1.base.BaseMod;

import java.util.List;

public class V2Mod extends BaseMod {
    private List<V2Dependency> deps;

    @Override
    public List<V2Dependency> getDeps() {
        return deps;
    }
}
