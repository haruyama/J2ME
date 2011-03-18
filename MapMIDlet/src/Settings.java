import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

public class Settings {
    
    private static final int SETTINGS_FORMAT_VERSION = 5; 
    /**
     * cacheExpireDays
     */
    
    private static final int SETTINGS_FORMAT_VERSION_5 = 5; 
    
    /**
     * displayScale
     */
    private static final int SETTINGS_FORMAT_VERSION_4 = 4;

    /**
     * bookmarkFilename;
     */

    private static final int SETTINGS_FORMAT_VERSION_3 = 3;

    /**
     * displayInfo, displayCenter
     */

    private static final int SETTINGS_FORMAT_VERSION_2 = 2;

    /**
     * keepImageDistance
     */
    private static final int SETTINGS_FORMAT_VERSION_1 = 1;

    /**
     * useCache, cacheDir, defaultZoomのみ
     */
    // private static final int SETTINGS_FORMAT_VERSION_0 = 0;
    private boolean useCache;

    private String cacheDir;

    private int defaultZoom;

    private int keepImageDistance;

    private boolean displayInfo;

    private boolean displayCenter;
    
    private boolean displayScale;

    private String bookmarkFilename;
    
    private int cacheExpireDays;

    private static Settings settings;

    private static final Object LOCK = new Object();

    public static Settings getInstance() {

        synchronized (LOCK) {
            if (settings != null) {
                return settings;
            }

            if (settings == null) {
                try {
                    settings = RecordStoreUtil.loadSettings();
                } catch (RecordStoreException e) {
                } catch (IOException e) {

                }

            }

            if (settings == null) {
                settings = new Settings();
            }
            return settings;
        }

    }

    public int getKeepImageDistance() {
        return keepImageDistance;
    }

    public void setKeepImageDistance(int keepImageDistance) {
        if (keepImageDistance < 0) {
            keepImageDistance = 0;
        }

        this.keepImageDistance = keepImageDistance;
    }

    private Settings() {
        useCache = false;
        cacheDir = "file:///";
        defaultZoom = 3;
        keepImageDistance = 2;
        displayInfo = false;
        displayCenter = false;
        displayScale = false;
        bookmarkFilename = "file:///";
        cacheExpireDays = 60;
    }

    /**
     * @param useCache
     * @param cacheDir
     * @param defaultZoom
     */
    private Settings(boolean useCache, String cacheDir, int defaultZoom,
            int keepImageDistance, boolean displayInfo, boolean displayCenter,
            String bookmarkExportFilename,
            boolean displayScale, 
            int cacheExpireDays) {
        super();
        this.useCache = useCache;
        this.cacheDir = cacheDir;
        this.defaultZoom = defaultZoom;
        this.keepImageDistance = keepImageDistance;
        this.displayInfo = displayInfo;
        this.displayCenter = displayCenter;
        this.bookmarkFilename = bookmarkExportFilename;
        this.displayScale = displayScale;
        this.cacheExpireDays = cacheExpireDays;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        if (!cacheDir.endsWith("/")) {
            cacheDir += "/";
        }

        this.cacheDir = cacheDir;
    }

    public int getDefaultZoom() {
        return defaultZoom;
    }

    public void setDefaultZoom(int defaultZoom) {
        if (defaultZoom < MapCanvas.GOOGLE_MAPS_MIN_ZOOM) {
            defaultZoom = MapCanvas.GOOGLE_MAPS_MIN_ZOOM;
        } else if (defaultZoom > MapCanvas.GOOGLE_MAPS_MAX_ZOOM) {
            defaultZoom = MapCanvas.GOOGLE_MAPS_MAX_ZOOM;
        }

        this.defaultZoom = defaultZoom;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public static byte[] toByteArray(Settings settings) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] b;
        try {
            dos.writeInt(SETTINGS_FORMAT_VERSION);

            dos.writeBoolean(settings.isUseCache());
            dos.writeUTF(settings.getCacheDir());
            dos.writeInt(settings.getDefaultZoom());

            dos.writeInt(settings.getKeepImageDistance());
            dos.writeBoolean(settings.isDisplayInfo());

            dos.writeBoolean(settings.isDisplayCenter());
            dos.writeUTF(settings.getBookmarkFilename());
            dos.writeBoolean(settings.isDisplayScale());
            dos.writeInt(settings.getCacheExpireDays());
            b = baos.toByteArray();
        } finally {
            if (dos != null) {
                dos.close();
            }
            if (baos != null) {
                baos.close();
            }

        }

        return b;
    }

    public static Settings fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Settings settings = null;

        try {

            int version = dis.readInt();
            if (version > SETTINGS_FORMAT_VERSION) {
                throw new IOException("バージョンが異なります");
            }
            boolean useCache = dis.readBoolean();
            String cacheDir = dis.readUTF();
            int defaultZoom = dis.readInt();

            int keepImageDIstance;
            if (version < SETTINGS_FORMAT_VERSION_1) {
                keepImageDIstance = 3;
            } else {
                keepImageDIstance = dis.readInt();
            }
            boolean displayInfo;
            boolean displayCenter;
            if (version < SETTINGS_FORMAT_VERSION_2) {
                displayInfo = false;
                displayCenter = false;

            } else {
                displayInfo = dis.readBoolean();
                displayCenter = dis.readBoolean();
            }
            String bookmarkExportFilename;
            if (version < SETTINGS_FORMAT_VERSION_3) {
                bookmarkExportFilename = "file:///";

            } else {
                bookmarkExportFilename = dis.readUTF();

            }
            boolean displayScale;
            if (version < SETTINGS_FORMAT_VERSION_4) {
                displayScale = false;
            } else {
                displayScale = dis.readBoolean();
            }
            int cacheExpireDays;
            if (version < SETTINGS_FORMAT_VERSION_5) {
                cacheExpireDays = 60;
            } else {
                cacheExpireDays = dis.readInt();
            }
            
            settings = new Settings(useCache, cacheDir, defaultZoom,
                    keepImageDIstance, displayInfo, displayCenter,
                    bookmarkExportFilename, displayScale,
                    cacheExpireDays);

        } finally {
            if (dis != null) {
                dis.close();
            }
            if (bais != null) {
                bais.close();
            }
        }

        return settings;

    }

    public boolean isDisplayCenter() {
        return displayCenter;
    }

    public void setDisplayCenter(boolean displayCenter) {
        this.displayCenter = displayCenter;
    }

    public boolean isDisplayInfo() {
        return displayInfo;
    }

    public void setDisplayInfo(boolean displayInfo) {
        this.displayInfo = displayInfo;
    }

    public String getBookmarkFilename() {
        return bookmarkFilename;
    }

    public void setBookmarkFilename(String bookmarkExportFilename) {
        this.bookmarkFilename = bookmarkExportFilename;
    }

    public boolean isDisplayScale() {
        return displayScale;
    }

    public void setDisplayScale(boolean displayScale) {
        this.displayScale = displayScale;
    }

    public int getCacheExpireDays() {
        return cacheExpireDays;
    }

    public void setCacheExpireDays(int cacheExpireDays) {
        this.cacheExpireDays = cacheExpireDays;
    }

}
