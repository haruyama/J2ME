import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Image;

public class ImageInfo {

    private int mapX;

    private int mapY;

    private int mapZoom;

    private int relativeX;

    private int relativeY;

//    private long lastModified;

    private Image image = null;

    private boolean isCache;

    private static int serverNumber = 3;

    private static int getServerNumber() {
        ++serverNumber;
        if (serverNumber > 3) {
            serverNumber = 0;
        }
        return serverNumber;
    }

    /**
     * @return Returns the imageByte.
     */
    public Image getImage() {
        return image;
    }

    /**
     * @param bytes
     *            The imageByte to set.
     */
    public void setImage(byte[] bytes) {
        if (Settings.getInstance().isUseCache()) {
            try {
                FileUtil.saveFileContent(Settings.getInstance().getCacheDir()
                        + getImagePath(), bytes);
//                lastModified = System.currentTimeMillis();

                isCache = false;
                CacheFilenames.getInstance().addElement(getImagePath());
            } catch (IOException e) {
                Alert alert = new Alert("キャッシュの保存に失敗", "キャッシュの保存に失敗しました\n"
                        + "IOExeptionが発生しています。空き容量が足りないのかもしれません", null, null);
                MapMIDlet.getDisplay().setCurrent(alert);
            }

        }

        System.gc();
        this.image = Image.createImage(bytes, 0, bytes.length);
        System.gc();
    }

    /**
     * @return Returns the mapX.
     */
    public int getMapX() {
        return mapX;
    }

    /**
     * @param mapX
     *            The mapX to set.
     */
    public void setMapX(int mapX) {
        this.mapX = mapX;
    }

    /**
     * @return Returns the mapY.
     */
    public int getMapY() {
        return mapY;
    }

    /**
     * @param mapY
     *            The mapY to set.
     */
    public void setMapY(int mapY) {
        this.mapY = mapY;
    }

    /**
     * @return Returns the mapZoom.
     */
    public int getMapZoom() {
        return mapZoom;
    }

    /**
     * @param mapZoom
     *            The mapZoom to set.
     */
    public void setMapZoom(int mapZoom) {
        this.mapZoom = mapZoom;
    }

    /**
     * @return Returns the relativeX.
     */
    public int getRelativeX() {
        return relativeX;
    }

    /**
     * @param relativeX
     *            The relativeX to set.
     */
    public void setRelativeX(int relativeX) {
        this.relativeX = relativeX;
    }

    /**
     * @return Returns the relativeY.
     */
    public int getRelativeY() {
        return relativeY;
    }

    /**
     * @param relativeY
     *            The relativeY to set.
     */
    public void setRelativeY(int relativeY) {
        this.relativeY = relativeY;
    }

    /**
     * @param mapX
     * @param mapY
     * @param mapZoom
     * @param relativeX
     * @param relativeY
     */
    public ImageInfo(int mapX, int mapY, int mapZoom, int relativeX,
            int relativeY) {
        super();
        this.mapX = mapX;
        this.mapY = mapY;
        this.mapZoom = mapZoom;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
    }

    public ImageInfo(int mapX, int mapY, int mapZoom, int relativeX,
            int relativeY, Image image
//            , long lastModified
            ) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.mapZoom = mapZoom;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.isCache = true;
        this.image = image;
//        this.lastModified = lastModified;
    }

    public String getImagePath() {
        return "x=" + mapX + "&y=" + mapY + "&zoom=" + mapZoom;
    }

    public static String getImagePath(int mapX, int mapY, int mapZoom) {
        return "x=" + mapX + "&y=" + mapY + "&zoom=" + mapZoom;

    }

    public String getImageUrl() {

        String url = "http://mt" + getServerNumber() +

        ".google.com/mt?" + "n=404&" + getImagePath();
        if (mapZoom > 8) {
            url += "&v=t2.2";

        } else {
            url += "&v=w2.6";
        }
        return url;
    }

    public static int[] getXYZoom(String filename) 
    throws IllegalArgumentException{
        int[] xyz = new int[3];
        if (!filename.startsWith("x=")) {
            throw new IllegalArgumentException(
                    "filename do not have a  correct form");
        }

        int indexY = filename.indexOf("&y=");
        if (indexY == -1) {
            throw new IllegalArgumentException(
                    "filename do not have a  correct form");
        }
        xyz[0] = Integer.parseInt(filename.substring(2, indexY));

        int indexZoom = filename.indexOf("&zoom=");
        if (indexZoom == -1) {
            throw new IllegalArgumentException(
                    "filename do not have a  correct form");
        }

        xyz[1] = Integer.parseInt(filename.substring(indexY + 3,
                indexZoom));

        xyz[2] = Integer.parseInt(filename.substring(indexZoom + 6));

        return xyz;
        
    }
    
    
    public static ImageInfo getInstance(String dirname, String filename,
            int relativeX, int relativeY) throws IllegalArgumentException,
            IOException {

        // String debugMessage = dirname + filename + "\n";

        // filename は x=116400&y=51625&zoom=0 という形式
        try {
            /*
            if (!filename.startsWith("x=")) {
                throw new IllegalArgumentException(
                        "filename do not have a  correct form");
            }

            int indexY = filename.indexOf("&y=");
            if (indexY == -1) {
                throw new IllegalArgumentException(
                        "filename do not have a  correct form");
            }
            int mapX = Integer.parseInt(filename.substring(2, indexY));

            int indexZoom = filename.indexOf("&zoom=");
            if (indexZoom == -1) {
                throw new IllegalArgumentException(
                        "filename do not have a  correct form");
            }

            int mapY = Integer.parseInt(filename.substring(indexY + 3,
                    indexZoom));

            int mapZoom = Integer.parseInt(filename.substring(indexZoom + 6));
*/
            int[] xyz = getXYZoom(filename);
//            long lastModified = FileUtil.getLastModified(dirname + filename);
            byte[] content = FileUtil.getFileContent(dirname + filename);

            // debugMessage += "\n" + content.length;

            // debugMessage += "\n" + Runtime.getRuntime().freeMemory();

            System.gc();
            Image image = Image.createImage(content, 0, content.length);
            System.gc();
            // debugMessage += "\nImage created";

            return new ImageInfo(xyz[0], xyz[1], xyz[2], relativeX, relativeY,
                    image);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "filename do not have a  correct form");
            // } catch (Throwable t) {
            // Alert alert = new Alert(t.toString(), debugMessage, null, null);
            // alert.setTimeout(Alert.FOREVER);
            // MapMIDlet.getDisplay().setCurrent(alert);
            // throw new IllegalArgumentException(
            // "dummy");
        }

    }

//    public long getLastModified() {
//        return lastModified;
//    }
//
//    public void setLastModified(long lastModified) {
//        this.lastModified = lastModified;
//    }

    public boolean isCache() {
        return isCache;
    }
}
