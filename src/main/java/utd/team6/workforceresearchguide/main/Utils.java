package utd.team6.workforceresearchguide.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

//@author Michael Haertling
public class Utils {

    /**
     * Iterates through all files and subdirectories within the given path and
     * collects the document paths within the hierarchy.
     *
     * @param dirPath
     * @return
     */
    public static ArrayList<String> extractAllPaths(String dirPath) {
        ArrayList<String> paths = new ArrayList<>();
        extractAllPaths(dirPath, paths);
        return paths;
    }

    /**
     * A helper method for the public path extraction function.
     *
     * @param path
     * @param list
     */
    private static void extractAllPaths(String path, ArrayList<String> list) {
        File f = new File(path);
        if (f.isDirectory()) {
            String[] files = f.list();
            for (String file : files) {
                File tmp = new File(path + "/" + file);
                if (tmp.isDirectory()) {
                    extractAllPaths(tmp.getAbsolutePath(), list);
                } else {
                    list.add(tmp.getAbsolutePath());
                }
            }
        }
    }
    
    public static String readDocument(String path) throws IOException, TikaException{
        String file = "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\SE Project\\test1.pdf";
        
        Tika tika = new Tika();
        try(InputStream fis = new FileInputStream(path)){
            String out = tika.parseToString(fis);
            return out;
        }
    }

}