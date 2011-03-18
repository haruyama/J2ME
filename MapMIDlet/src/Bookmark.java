import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Bookmark {
    private String name;

    private int x;

    private int y;

    private int zoom;

    private static final int BOOKMARK_FORMAT_VERSION = 0;

    /**
     * @param name
     * @param x
     * @param y
     * @param zoom
     */
    public Bookmark(String name, int x, int y, int zoom) {
        super();
        this.name = name;
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    /**
     * @param x
     * @param y
     * @param zoom
     */
    public Bookmark(int x, int y, int zoom) {
        this("x=" + x + ", y=" + y + ", zoom=" + zoom, x, y, zoom);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public static byte[] toByteArray(Bookmark bookmark) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] b;
        try {
            dos.writeInt(BOOKMARK_FORMAT_VERSION);
            dos.writeUTF(bookmark.getName());
            dos.writeInt(bookmark.getX());
            dos.writeInt(bookmark.getY());
            dos.writeInt(bookmark.getZoom());

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

    public static Bookmark fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Bookmark bookmark;

        try {

            int version = dis.readInt();
            if (version > BOOKMARK_FORMAT_VERSION) {
                throw new IOException("バージョンが異なります");
            }

            String name = dis.readUTF();
            int x = dis.readInt();
            int y = dis.readInt();
            int zoom = dis.readInt();

            bookmark = new Bookmark(name, x, y, zoom);

        } finally {
            if (dis != null) {
                dis.close();
            }
            if (bais != null) {
                bais.close();
            }
        }

        return bookmark;

    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(name.replace('\n', '|').replace(',', '\\'));
        buffer.append(',');
        buffer.append(x);
        buffer.append(',');
        buffer.append(y);
        buffer.append(',');
        buffer.append(zoom);
        // buffer.append('\n');
        return buffer.toString();
    }

    public static Bookmark fromString(String str)
            throws IllegalArgumentException, NumberFormatException {

        int indexBegin = 0;
        int indexEnd;

        indexEnd = str.indexOf(',');
        if (indexEnd == -1) {
            throw new IllegalArgumentException("comma not found no.1: " + str);
        }

        String name = str.substring(0, indexEnd);

        name = name.replace('|', '\n').replace('\\', ',');

        indexBegin = indexEnd + 1;

        indexEnd = str.indexOf(',', indexBegin);
        if (indexEnd == -1) {
            throw new IllegalArgumentException("comma not found no.2: " + str
                    + ", " + name);
        }

        int x = Integer.parseInt(str.substring(indexBegin, indexEnd));

        indexBegin = indexEnd + 1;

        indexEnd = str.indexOf(',', indexBegin);
        if (indexEnd == -1) {
            throw new IllegalArgumentException("comma not found no.3: " + str);
        }

        int y = Integer.parseInt(str.substring(indexBegin, indexEnd));

        int zoom = Integer.parseInt(str.substring(indexEnd + 1));

        return new Bookmark(name, x, y, zoom);

    }

}
