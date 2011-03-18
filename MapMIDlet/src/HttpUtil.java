import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class HttpUtil {

    private static final Object LOCK = new Object();

    //
    // public static long getLastModified(String url) throws IOException {
    // HttpConnection connection = null;
    // synchronized (LOCK) {
    // try {
    // connection = (HttpConnection) Connector.open(url,
    // Connector.READ, true);
    // connection.setRequestMethod(HttpConnection.HEAD);
    // connection.setRequestProperty("Connection", "keep-alive");
    // connection.setRequestProperty("Keep-Alive", "600");
    //
    // int rc = connection.getResponseCode();
    // if (rc != HttpConnection.HTTP_OK) {
    // return 0L;
    // }
    // return connection.getLastModified();
    // } finally {
    //
    // if (connection != null) {
    // connection.close();
    // }
    //
    // }
    //
    // }
    //
    // }

    public static byte[] getBytesViaHttp(String url) throws IOException {
        InputStream is = null;
        HttpConnection connection = null;
        byte[] buffer;
        synchronized (LOCK) {
            try {
                connection = (HttpConnection) Connector.open(url,
                        Connector.READ, true);

                connection.setRequestMethod(HttpConnection.GET);
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("Keep-Alive", "600");

                int rc = connection.getResponseCode();
                if (rc != HttpConnection.HTTP_OK) {
                    return null;
                }

                is = connection.openInputStream();
                long len = connection.getLength();

                if (len != -1) {
                    buffer = new byte[(int) len];
                    int offset = 0;
                    while (offset < len) {
                        offset += is.read(buffer, offset, (int) len - offset);
                    }

                } else {
                    // 富豪的に
                    Vector vector = new Vector();
                    while (true) {
                        int ret = is.read();
                        if (ret != -1) {
                            vector.addElement(new Byte((byte) ret));
                        } else {
                            buffer = new byte[vector.size()];
                            for (int i = 0; i < buffer.length; ++i) {
                                buffer[i] = ((Byte) vector.elementAt(i))
                                        .byteValue();
                            }
                            break;
                        }
                    }
                }
                return buffer;
            } finally {

                if (is != null) {
                    is.close();
                }

                if (connection != null) {
                    connection.close();
                }

            }

        }

    }
}
