import java.io.IOException;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class RecordStoreUtil {

    /**
     * レコードストアへの複数アクセスをなくすためのロックオブジェクト (たぶんいらない)
     */
    private static final Object LOCK = new Object();

    /**
     * 最後に閲覧したファイル情報を格納するRecordStore名
     */
    private static final String LAST_FILE_INFO = "LastFileInfo";

    /**
     * 設定を格納するRecordStore名
     */
    private static final String SETTINGS = "Settings";

    private static final String BOOKMARKS = "Bookmarks";

    private static final String NUMBER_OF_BOOKMARKS = "NumOfBookmarks";

    public static void saveBookmarks(Bookmarks bookmarks)
            throws RecordStoreException, RecordStoreFullException,
            RecordStoreNotFoundException, IOException {
        RecordStore recordStore = null;
        RecordStore recordStore2 = null;
        synchronized (LOCK) {
            // 既存のレコードストアがあったら消す
            try {
                RecordStore.deleteRecordStore(NUMBER_OF_BOOKMARKS);
            } catch (Exception e) {

            }
            try {

                RecordStore.deleteRecordStore(BOOKMARKS);
            } catch (Exception e) {

            }
            try {

                recordStore2 = RecordStore.openRecordStore(NUMBER_OF_BOOKMARKS,
                        true);

                byte[] b = Integer.toString(bookmarks.size()).getBytes();
                recordStore2.addRecord(b, 0, b.length);



                recordStore = RecordStore.openRecordStore(BOOKMARKS, true);
                for (int i = 0; i < bookmarks.size(); ++i) {
                    b = Bookmark.toByteArray((Bookmark) bookmarks
                            .elementAt(i));

                    recordStore.addRecord(b, 0, b.length);
                }
            } finally {
                if (recordStore != null) {
                    recordStore.closeRecordStore();
                }

                if (recordStore2 != null) {
                    recordStore2.closeRecordStore();
                }

            }

        }

    }

    public static Bookmarks loadBookmarks() throws RecordStoreException,
            RecordStoreFullException, RecordStoreNotFoundException, IOException {
        RecordStore recordStore = null;
        RecordStore recordStore2 = null;
        Bookmarks bookmarks = Bookmarks.getInstance();
        int size = 0;
        synchronized (LOCK) {

            try {
                recordStore2 = RecordStore.openRecordStore(NUMBER_OF_BOOKMARKS,
                        false);

                size = Integer.parseInt(new String(recordStore2.getRecord(1))) + 1;

            } catch (Exception e) {

            }

            try {

                recordStore = RecordStore.openRecordStore(BOOKMARKS, false);
                if (size == 0) {
                    size = recordStore.getNumRecords() + 1;
                }

                for (int i = 1; i < size; ++i) {
                    Bookmark bookmark = Bookmark.fromByteArray(recordStore
                            .getRecord(i));

                    bookmarks.addElement(bookmark);

                }

            } finally {
                if (recordStore != null) {
                    recordStore.closeRecordStore();
                }
                if (recordStore2 != null) {
                    recordStore2.closeRecordStore();
                }
            }

        }
        return bookmarks;

    }

    private static void saveByteArray(String recordStoreName, byte[] b)
            throws RecordStoreException, RecordStoreFullException,
            RecordStoreNotFoundException {
        RecordStore recordStore = null;
        synchronized (LOCK) {

            // 既存のレコードストアがあったら消す
            try {
                RecordStore.deleteRecordStore(recordStoreName);
            } catch (Exception e) {

            }

            try {
                recordStore = RecordStore
                        .openRecordStore(recordStoreName, true);
                recordStore.addRecord(b, 0, b.length);

            } finally {
                if (recordStore != null) {
                    recordStore.closeRecordStore();
                }
            }
        }

    }

    private static byte[] loadByteArray(String recordStoreName)
            throws RecordStoreException, RecordStoreFullException,
            RecordStoreNotFoundException {
        RecordStore recordStore = null;
        byte[] b = null;
        synchronized (LOCK) {
            try {
                recordStore = RecordStore.openRecordStore(recordStoreName,
                        false);

                b = recordStore.getRecord(1);
            } finally {
                if (recordStore != null) {
                    recordStore.closeRecordStore();
                }
            }

        }
        return b;

    }

    public static void saveCurrentFileInfo(FileInfo fileInfo)
            throws RecordStoreException, RecordStoreFullException,
            RecordStoreNotFoundException {

        byte[] b;
        try {
            b = FileInfo.toByteArray(fileInfo);
        } catch (IOException e) {
            throw new RecordStoreException(e.toString());
        }

        saveByteArray(LAST_FILE_INFO, b);
    }

    public static FileInfo loadCurrentFileInfo() throws IOException,
            RecordStoreException, RecordStoreFullException,
            RecordStoreNotFoundException {

        byte[] b = loadByteArray(LAST_FILE_INFO);

        return FileInfo.fromByteArray(b);
    }

    public static void saveSettings(Settings settings)
            throws RecordStoreException, RecordStoreFullException,
            RecordStoreNotFoundException {

        byte[] b;
        try {
            b = Settings.toByteArray(settings);
        } catch (IOException e) {
            throw new RecordStoreException(e.toString());
        }

        saveByteArray(SETTINGS, b);
    }

    public static Settings loadSettings() throws IOException,
            RecordStoreException, RecordStoreFullException,
            RecordStoreNotFoundException {

        byte[] b = loadByteArray(SETTINGS);

        return Settings.fromByteArray(b);
    }

}
