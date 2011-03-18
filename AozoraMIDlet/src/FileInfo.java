import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Stack;

import javax.microedition.lcdui.Font;

public class FileInfo {
    private String content;

    private String path;

    private int length;

    private int offset;

    private int end;

    private Stack offsetStack;

    private int lineNumber = 0;

    private boolean isTategaki;

    private int plainTextSize;

    private int rubyTextSize;

//    public static final int MAX_CONTENT_LENGTH = 65536;
    public static final int MAX_CONTENT_LENGTH = 32786;

    public static final String FILE_ENCODING = "Shift_JIS";

    private static final int FILE_INFO_FORMAT_VERSION = 2;

    /**
     * content, path, length, offset, end, offsetStack, lineNumber, isTategaki
     * で構成されていた。
     */
    private static final int FILE_INFO_FORMAT_VERSION_0 = 0;

    /**
     * plainTextFontSizeとrubyTextFontSizeが加わった
     */
//    private static final int FILE_INFO_FORMAT_VERSION_1 = 1;
    
    /**
     * MAX_CONTENT_LENGTH 65536 -> 32786;
     */
//  private static final int FILE_INFO_FORMAT_VERSION_2 = 2;
    
    
    

    /**
     * @param content
     * @param directory
     * @param path
     * @param length
     * @param currentPosition
     */
    public FileInfo(String content, String path, int length, int offset,
            int end, Stack offsetStack) {
        this(content, path, length, offset, end, offsetStack, 0, 
                Settings.getInstance().isTategaki(),
                Settings.getInstance().getPlainTextSize(), Settings
                        .getInstance().getRubyTextSize());
    }

    private FileInfo(String content, String path, int length, int offset,
            int end, Stack offsetStack, int lineNumber, boolean isTategaki,
            int plainFontsize, int rubyFontSize) {

        this.content = content;
        this.path = path;
        this.length = length;
        this.offset = offset;
        this.end = end;
        if (offsetStack != null) {
            this.offsetStack = offsetStack;
        } else {
            this.offsetStack = new Stack();
        }
        this.lineNumber = lineNumber;
        this.isTategaki = isTategaki;
        this.plainTextSize = plainFontsize;
        this.rubyTextSize = rubyFontSize;
    }
//
//    public void clean() {
//        content = null;
//        offsetStack = null;
//        System.gc();
//        
//        
//    }
    
    public boolean hasNext() {

        if (end < length) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasPrev() {

        if (offset > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("FileInfo: path =" + path + "\n");
        buffer.append("length =" + length + "\n");
        buffer.append("offset =" + offset + "\n");
        buffer.append("end =" + end + "\n");
        buffer.append("lineNumber=" + lineNumber + "\n");
        buffer.append("isTategaki=" + isTategaki + "\n");
        for (int i = 0; i < offsetStack.size(); ++i) {
            buffer.append("offsetStack" + i + " : " + offsetStack.elementAt(i)
                    + "\n");
        }
        buffer.append("plainTextFontSize=" + plainTextSize + "\n");
        buffer.append("rubyTextFontSize=" + rubyTextSize + "\n");
        return buffer.toString();

    }

    /**
     * @return Returns the offsetStack.
     */
    public Stack getOffsetStack() {
        return offsetStack;
    }

    /**
     * @param offsetStack
     *            The offsetStack to set.
     */
    public void setOffsetStack(Stack offsetStack) {
        this.offsetStack = offsetStack;
    }

    /**
     * @return Returns the end.
     */
    public int getEnd() {
        return end;
    }

    /**
     * @return Returns the currentPosition.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return Returns the content.
     */
    public String getContent() {
        return content;
    }

    /**
     * @return Returns the length.
     */
    public int getLength() {
        return length;
    }

    /**
     * @return Returns the name.
     */
    public String getPath() {
        return path;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public boolean isTategaki() {
        return isTategaki;
    }

    public void setTategaki(boolean isTategaki) {
        this.isTategaki = isTategaki;
    }

    public Font getPlainTextFont() {

        if (plainTextSize == Settings.FONT_SIZE_NONE) {
            plainTextSize = Font.SIZE_MEDIUM;
        }

        return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, plainTextSize);

    }

    public Font getRubyTextFont() {

        if (rubyTextSize == Settings.FONT_SIZE_NONE) {
            return null;
        }

        return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, rubyTextSize);

    }
    public static byte[] toByteArray(FileInfo fileInfo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] b = null;
        try {
            dos.writeInt(FILE_INFO_FORMAT_VERSION);
            dos.writeUTF(fileInfo.getPath());
            // dos.writeInt(fileInfo.getLength());
            dos.writeInt(fileInfo.getOffset());

            dos.writeInt(fileInfo.getLineNumber());
            dos.writeBoolean(fileInfo.isTategaki());
            // dos.writeInt(fileInfo.getEnd());
            if (fileInfo.getOffsetStack() != null) {
                int size = fileInfo.getOffsetStack().size();
                dos.writeInt(size);

                for (int i = 0; i < size; ++i) {
                    dos.writeInt(((Integer) fileInfo.getOffsetStack()
                            .elementAt(i)).intValue());
                }
            } else {
                dos.writeInt(0);

            }

            dos.writeInt(fileInfo.getPlainTextSize());
            dos.writeInt(fileInfo.getRubyTextSize());
            
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

    public static FileInfo fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        FileInfo fileInfo = null;

        try {

            int version = dis.readInt();
            if (version != FILE_INFO_FORMAT_VERSION) {
                throw new IOException("バージョンが異なります");
            }

            String path = dis.readUTF();
            // int length = dis.readInt();
            int offset = dis.readInt();
            // int end = dis.readInt();
            int lineNumber = dis.readInt();
            boolean isTategaki = dis.readBoolean();

            int offsetSize = dis.readInt();
            Stack offsetStack = new Stack();
            for (int i = 0; i < offsetSize; ++i) {
                offsetStack.addElement(new Integer(dis.readInt()));
            }

            int plainFontSize;
            int rubyFontSize;
            if (version > FILE_INFO_FORMAT_VERSION_0) {
                plainFontSize = dis.readInt();
                rubyFontSize = dis.readInt();
            } else {
                plainFontSize = Settings.getInstance().getPlainTextSize();
                rubyFontSize = Settings.getInstance().getRubyTextSize();

            }

            fileInfo = FileUtil.getFileContent(path, offset, offsetStack);
            fileInfo.setLineNumber(lineNumber);
            fileInfo.setTategaki(isTategaki);
            fileInfo.setPlainTextSize(plainFontSize);
            fileInfo.setRubyTextSize(rubyFontSize);
            

        } finally {
            if(dis!=null) {
                dis.close();
            }
            if(bais!=null) {
                bais.close();
            }
        }

        return fileInfo;

    }

    public int getPlainTextSize() {
        return plainTextSize;
    }

    public void setPlainTextSize(int plainFontSize) {
        this.plainTextSize = plainFontSize;
    }

    public int getRubyTextSize() {
        return rubyTextSize;
    }

    public void setRubyTextSize(int rubyFontSize) {
        
        this.rubyTextSize = rubyFontSize;
    }

}
