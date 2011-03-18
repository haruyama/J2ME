import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Stack;

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
    private static final int SKIP_STEP = 8192;

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

        FileConnection fc = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(directoryPath);
                if (fc.exists()) {
                    if (fc.isDirectory()) {
                        return fc.list("*", true);
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

    public static FileInfo getFileContent(String filePath, int offset,
            Stack offsetStack) throws IOException {
        FileConnection fc = null;
        InputStream is = null;
        FileInfo info = null;

        synchronized (FILE_LOCK) {
            try {
                fc = (FileConnection) Connector.open(filePath);
                if (fc.exists()) {
                    if (!fc.isDirectory()) {
                        long length = fc.fileSize();
                        byte[] buffer;
                        boolean isOverflow = false;

                        if (length > offset + FileInfo.MAX_CONTENT_LENGTH) {
                            buffer = new byte[FileInfo.MAX_CONTENT_LENGTH];
                            isOverflow = true;
                        } else {
                            buffer = new byte[(int) length - offset];
                        }

                        is = fc.openInputStream();

                        int skip = 0;
                        byte[] tmp = new byte[SKIP_STEP];
                        while (offset > skip) {
                            // skip += dis.skip(offset-skip);
                            // skip += dis.skipBytes((int)(offset-skip));

                            if (offset - skip > SKIP_STEP) {
                                int ret = is.read(tmp);
                                skip += ret;
                            } else {
                                int ret = is.read(tmp, 0, offset - skip);
                                skip += ret;
                            }
                        }

                        int ret = is.read(buffer);
                        if(ret != buffer.length) {
                            throw new IOException("read() is not blocked.");
                        }

                        if (isOverflow) {
                            int i;
                            for (i = buffer.length - 1; i > 0; --i) {
                                if (buffer[i] == '\n') {
                                    if (buffer[i - 1] == '\r') {
                                        break;
                                    }

                                }
                            }
                            info = new FileInfo(new String(buffer, 0, i,
                                    FileInfo.FILE_ENCODING), filePath,
                                    (int) length, offset, offset + i,
                                    offsetStack);
                        } else {
                            info = new FileInfo(new String(buffer,
                                    FileInfo.FILE_ENCODING), filePath,
                                    (int) length, offset, (int) length,
                                    offsetStack);
                        }

                        return info;

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
