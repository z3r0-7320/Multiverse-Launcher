package multiverse.utils;

import java.io.File;
import java.nio.file.Files;

public class DirectoryDeleter {
    public static boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        return file.delete();
    }
}
