import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Stack;

public class Bookmark {
    private String name;

    private String path;

    private int length;

    private int offset;

    private int end;

    private Stack offsetStack;

    private int lineNumber = 0;

    private boolean isTategaki;

    private int plainTextSize;

    private int rubyTextSize;

    private static final int BOOKMARK_FORMAT_VERSION = 1;

    /**
     * @param name
     * @param path
     * @param length
     * @param offset
     * @param end
     * @param offsetStack
     * @param lineNumber
     * @param isTategaki
     * @param plainTextSize
     * @param rubyTextSize
     */
    private Bookmark(String name, String path, int length, int offset, int end,
            Stack offsetStack, int lineNumber, boolean isTategaki,
            int plainTextSize, int rubyTextSize) {
        super();
        this.name = name;
        this.path = path;
        this.length = length;
        this.offset = offset;
        this.end = end;
        this.offsetStack = new Stack();
        for (int i = 0; i < offsetStack.size(); ++i) {
            this.offsetStack.addElement(offsetStack.elementAt(i));
        }
        this.lineNumber = lineNumber;
        this.isTategaki = isTategaki;
        this.plainTextSize = plainTextSize;
        this.rubyTextSize = rubyTextSize;
    }

    public static Bookmark getInstance(FileInfo fileInfo) {

        return new Bookmark(fileInfo.getPath() + " " + (fileInfo.getOffsetStack().size() +1) 
                + "ページ " + fileInfo.getLineNumber()+"行目", fileInfo.getPath(), fileInfo
                .getLength(), fileInfo.getOffset(), fileInfo.getEnd(), fileInfo
                .getOffsetStack(), fileInfo.getLineNumber(), fileInfo
                .isTategaki(), fileInfo.getPlainTextSize(), fileInfo
                .getRubyTextSize()

        );

    }
    


    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isTategaki() {
        return isTategaki;
    }

    public void setTategaki(boolean isTategaki) {
        this.isTategaki = isTategaki;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Stack getOffsetStack() {
        return offsetStack;
    }

    public void setOffsetStack(Stack offsetStack) {
        this.offsetStack = offsetStack;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPlainTextSize() {
        return plainTextSize;
    }

    public void setPlainTextSize(int plainTextSize) {
        this.plainTextSize = plainTextSize;
    }

    public int getRubyTextSize() {
        return rubyTextSize;
    }

    public void setRubyTextSize(int rubyTextSize) {
        this.rubyTextSize = rubyTextSize;
    }

    public static byte[] toByteArray(Bookmark bookmark) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] b = null;
        try {
            dos.writeInt(BOOKMARK_FORMAT_VERSION);
            dos.writeUTF(bookmark.getName());
            dos.writeUTF(bookmark.getPath());
            dos.writeInt(bookmark.getLength());
            dos.writeInt(bookmark.getOffset());
            dos.writeInt(bookmark.getEnd());
            dos.writeInt(bookmark.getLineNumber());
            dos.writeBoolean(bookmark.isTategaki());
            dos.writeInt(bookmark.getPlainTextSize());
            dos.writeInt(bookmark.getRubyTextSize());

            if (bookmark.getOffsetStack() != null) {
                int size = bookmark.getOffsetStack().size();
                dos.writeInt(size);

                for (int i = 0; i < size; ++i) {
                    dos.writeInt(((Integer) bookmark.getOffsetStack()
                            .elementAt(i)).intValue());
                }
            } else {
                dos.writeInt(0);

            }

            b = baos.toByteArray();
        } finally {
            if(dos!=null) {
                dos.close();
            }
            if(baos!=null) {
                baos.close();
            }
        }

        return b;
    }

    public static Bookmark fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Bookmark bookmark = null;

        try {

            int version = dis.readInt();
            if (version != BOOKMARK_FORMAT_VERSION) {
                throw new IOException("バージョンが異なります");
            }

            String name = dis.readUTF();
            String path = dis.readUTF();
            int length = dis.readInt();
            int offset = dis.readInt();
            int end = dis.readInt();
            int lineNumber = dis.readInt();
            boolean isTategaki = dis.readBoolean();
            int plainFontSize = dis.readInt();
            int rubyFontSize = dis.readInt();

            int offsetSize = dis.readInt();
            Stack offsetStack = new Stack();
            for (int i = 0; i < offsetSize; ++i) {
                offsetStack.addElement(new Integer(dis.readInt()));
            }
            bookmark = new Bookmark(name, path, length, offset, end,
                    offsetStack, lineNumber, isTategaki, plainFontSize,
                    rubyFontSize);

        } finally {
            if(dis!=null) {
            dis.close();
            }
            if(bais!=null) {
                bais.close();
            }
        }

        return bookmark;

    }

}

