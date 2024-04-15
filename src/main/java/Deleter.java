import java.io.File;
import java.net.URISyntaxException;

public class Deleter {
    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        if (args.length == 0) return;
        File jar = new File(new File(Deleter.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), args[0]);
        if (jar.getName().endsWith(".jar"))
            for (int i = 0; i < 10; i++) {
                if (!jar.exists() || jar.delete()) break;
                Thread.sleep(1000);
            }
    }
}
