import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class FileUtil {

    /**
     * ファイルアクセスが複数あるとうれしくないかもしれないので 一応ロックある。そのためのオブジェクト
     */
    private static final Object FILE_LOCK = new Object();

    /**
     * InputStream.skip(long n)の代わりをさせる部分で 一度に最大どれだけ読み飛ばすか
     */
    // private static final int SKIP_STEP = 8192;
    /**
     * ディレクトリパスを入力してそのディレクトリ中のパス名のEnumerationを 返すメソッド
     * 
     * @param directoryPath
     *            ディレクトリのパス
     * @return ディレクトリ中のパス名の Enumeration
     * @throws IOException
     *             入力パスに対応するものがなかったりディレクトリでなかった場合にthrowされる
     */
    public static Enumeration getFileNames(String directoryPath)
            throws IOException {
        return getFileNames(directoryPath, "*", true);

    }

    public static Enumeration getCacheFileNames(String directoryPath,
            String filter) throws IOException {
        return getFileNames(directoryPath, filter, true);
    }

    public static void delete(String filePath) throws IOException {
        FileConnection fc = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(filePath);
                if (fc.exists()) {
                    fc.delete();
                } else {
                    throw new IOException("File or Directory Not Found.");
                }

            } finally {
                if (fc != null) {
                    fc.close();
                }
            }
        }
    }

    private static Enumeration getFileNames(String directoryPath,
            String filger, boolean includeHidden) throws IOException {

        FileConnection fc = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(directoryPath);
                if (fc.exists()) {
                    if (fc.isDirectory()) {
                        return fc.list(filger, includeHidden);
                    }
                    throw new IOException(directoryPath + "is  not directory.");
                }
                throw new IOException("Directory Not Found.");

            } finally {
                if (fc != null) {
                    fc.close();
                }
            }
        }
    }

    public static void mkdir(String directoryPath) throws IOException {
        FileConnection fc = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(directoryPath);
                fc.mkdir();

            } finally {
                if (fc != null) {
                    fc.close();
                }
            }
        }

    }

    public static long getLastModified(String filePath) throws IOException {
        FileConnection fc = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(filePath);
                if (fc.exists()) {
                    return fc.lastModified();
                }
                throw new IOException("File not found");

            } finally {
                if (fc != null) {
                    fc.close();
                }
            }
        }

    }

    public static void saveFileContent(String filePath, byte[] content)
            throws IOException {
        FileConnection fc = null;
        OutputStream os = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(filePath);

                if (!fc.exists()) {
                    fc.create();
                }
                os = fc.openOutputStream();
                os.write(content);

            } finally {

                if (os != null) {
                    os.close();
                }

                if (fc != null) {
                    fc.close();
                }
            }
        }

    }

    public static byte[] getFileContent(String filePath) throws IOException {
        FileConnection fc = null;
        InputStream is = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(filePath);
                if (fc.exists()) {
                    if (!fc.isDirectory()) {
                        long length = fc.fileSize();
                        byte[] buffer = new byte[(int) length];
                        is = fc.openInputStream();

                        int ret = is.read(buffer);
                        if (ret != buffer.length) {
                            throw new IOException("read() is not blocked.");
                        }
                        return buffer;
                    }
                    throw new IOException(filePath + "is  directory.");

                }
                throw new IOException("File not Found");

            } finally {

                if (is != null) {
                    is.close();
                }

                if (fc != null) {
                    fc.close();
                }
            }
        }

    }

}
