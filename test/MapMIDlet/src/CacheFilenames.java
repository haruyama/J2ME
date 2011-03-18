import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;


public class CacheFilenames {

    private Vector cacheFilenamesVector;

    private Vector filterVector;

    private final static int MAX_ZOOM_LEVEL = 16;

    private static CacheFilenames cacheFilenames = new CacheFilenames();

    public static CacheFilenames getInstance() {
        return cacheFilenames;
    }

    private CacheFilenames() {
        cacheFilenamesVector = new Vector();
        filterVector = new Vector();
    }

    public boolean removeElement(Object arg0) {

        return cacheFilenamesVector.removeElement(arg0);
    }

    public void addElement(String string) {
        cacheFilenamesVector.addElement(string);
    }

    private String createFilter(int x, int zoom) {
        if (zoom < 0 || zoom > MAX_ZOOM_LEVEL || x < 0) {
            throw new IllegalArgumentException("x=" + x + " zoom=" + zoom);
        }
        // if (x > 1000) {
        // x = x / 1000;
        // }else
        if (x > 100) {
            x = x / 100;
        } else if (x > 10) {
            x = x / 10;
        }

        return "x=" + x + "*&zoom=" + zoom;

    }

    private void loadCacheFilenames(String filter) {

        Enumeration fileEnumeration;
        try {
            fileEnumeration = FileUtil.getCacheFileNames(Settings.getInstance()
                    .getCacheDir(), filter);
            for (; fileEnumeration.hasMoreElements();) {

                String filename = (String) fileEnumeration.nextElement();
                cacheFilenamesVector.addElement(filename);

            }
//            Alert alert = new Alert("DebugMessage", "CacheFilenames init: "
//                    + filter + "size: " + cacheFilenamesVector.size(), null,
//                    null);
//            MapMIDlet.getDisplay().setCurrent(alert);

        } catch (IOException e) {

        }

    }

    public void clear() {
        filterVector.removeAllElements();
        cacheFilenamesVector.removeAllElements();
    }
    
    public boolean contains(String object, int x, int zoom) {
        String filter = createFilter(x, zoom);
        if (!filterVector.contains(filter)) {
            filterVector.addElement(filter);
            loadCacheFilenames(filter);
        }

        return cacheFilenamesVector.contains(object);
    }

}
