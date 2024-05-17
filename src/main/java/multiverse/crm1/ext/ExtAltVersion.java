package multiverse.crm1.ext;

import multiverse.crm1.base.BaseDependency;
import multiverse.crm1.base.BaseMod;

import java.util.List;

public class ExtAltVersion extends BaseMod {

    private List<BaseDependency> deps;

    @Override
    public List<BaseDependency> getDeps() {
        return deps;
    }
}
