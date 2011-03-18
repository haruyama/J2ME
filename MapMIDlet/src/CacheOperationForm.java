import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.RecordStoreException;

public class CacheOperationForm extends Form implements CommandListener,
        ItemCommandListener {

    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private final Command CMD_DELETE_ALL = new Command("全削除", Command.ITEM, 1);

    private final Command CMD_DELETE = new Command("削除", Command.ITEM, 1);

    private final Command CMD_OK = new Command("OK", Command.OK, 1);

    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);

    private static CacheOperationForm cacheOperationForm = new CacheOperationForm();

    private StringItem cacheDirItem;

    private StringItem deleteAllCacheButton;

    private StringItem deleteCacheButton;

    private Settings settings;

    private Displayable previousDisplayable;

    private TextField cacheExpireDaysField;

    private Vector filenameVector;

    private Vector allFilenameVector;

    private Gauge deletingGauge;

    private boolean doDelateAll = false;

    private boolean isProcessing = false;

    public static CacheOperationForm getInstance(Displayable displayable) {

        if (displayable != null) {
            cacheOperationForm.previousDisplayable = displayable;
        }
        cacheOperationForm.settings = Settings.getInstance();
        cacheOperationForm.init();
        return cacheOperationForm;
    }

    private void init() {
        cacheExpireDaysField.setString(Integer.toString(settings
                .getCacheExpireDays()));
        cacheDirItem.setText(settings.getCacheDir());
        isProcessing = false;

    }

    private CacheOperationForm() {
        super("キャッシュの操作");

        cacheDirItem = new StringItem("次のディレクトリを対象にします", "", Item.HYPERLINK);

        deleteAllCacheButton = new StringItem("すべてのキャッシュを削除",
                "すべてのキャッシュを削除します", Item.BUTTON);
        deleteAllCacheButton.setDefaultCommand(CMD_DELETE_ALL);
        deleteAllCacheButton.setItemCommandListener(this);

        cacheExpireDaysField = new TextField("キャッシュを保持する日数", null, 3,
                TextField.NUMERIC);
        deleteCacheButton = new StringItem("キャッシュを削除", "保持日数を過ぎブックマークの近辺にない"
                + "キャッシュを削除します", Item.BUTTON);

        deleteCacheButton.setDefaultCommand(CMD_DELETE);
        deleteCacheButton.setItemCommandListener(this);

        deletingGauge = new Gauge("削除の進行状況", false, 100, 0);
        deletingGauge.setLayout(Item.LAYOUT_EXPAND);
        append(cacheDirItem);
        append(deleteAllCacheButton);
        append(cacheExpireDaysField);
        append(deleteCacheButton);
        append(deletingGauge);
        setCommandListener(this);
        addCommand(CMD_BACK);
    }

    public void commandAction(Command command, Displayable displayable) {
        MapMIDlet.getDisplay().setCurrent(previousDisplayable);

    }

    private boolean setFilenameVector() {

        setTicker(new Ticker("削除の前処理中"));
        String cacheDir = Settings.getInstance().getCacheDir();
        long now = System.currentTimeMillis();

        long expireTime = 24 * 60 * 60 * 1000
                * (long) Settings.getInstance().getCacheExpireDays();
        Bookmarks bookmarks = Bookmarks.getInstance();
        int bookmarksSize = bookmarks.size();

        try {
            Enumeration enumeration = FileUtil.getFileNames(cacheDir);
            allFilenameVector = new Vector();
            if (doDelateAll) {
                filenameVector = allFilenameVector;
            } else {
                filenameVector = new Vector();
            }
            while (enumeration.hasMoreElements()) {
                String filename = (String) enumeration.nextElement();
                if (!filename.endsWith("/")) {
                    allFilenameVector.addElement(filename);
                }

                if (!doDelateAll) {
                    try {

                        long lastModified = FileUtil.getLastModified(cacheDir
                                + filename);

                        if (lastModified + expireTime > now) {
                            continue;
                        }
                        boolean flag = false;
                        int[] xyz = ImageInfo.getXYZoom(filename);
                        int x = xyz[0] << xyz[2];
                        int y = xyz[1] << xyz[2];
                        for (int i = 0; i < bookmarksSize; ++i) {
                            Bookmark bookmark = bookmarks.elementAt(i);
                            int bx = bookmark.getX() << bookmark.getZoom();
                            int by = bookmark.getY() << bookmark.getZoom();
                            if (Math.abs(x - bx) + Math.abs(y - by) < 100) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            filenameVector.addElement(filename);
                        }
                    } catch (Exception e) {
                        // キャッシュファイル名の規則に従ってないファイル
                        // 無視してよい
                        //もしくはlastModifiedがとれていないか
                    }
                }

            }
        } catch (Exception e) {
            Alert alert = new Alert(e.toString(), e.getMessage()
                    + "\n キャッシュのディレクトリのリストの取得に失敗しました.", null, null);
            MapMIDlet.getDisplay().setCurrent(alert);
            isProcessing = false;
            setTicker(null);
            return false;
        }
        setTicker(null);
        return true;

    }

    public void commandAction(Command command, Item item) {
        if (isProcessing) {
            return;

        }

        isProcessing = true;
        if (command == CMD_DELETE_ALL) {

            new Thread()
            {

                public void run() {
                    doDelateAll = true;
                    if (setFilenameVector()) {

                        Alert alert = new Alert("キャッシュの全削除",
                                "キャッシュディレクトリにある全てのファイル("
                                        + filenameVector.size()
                                        + "件)を削除します。よろしいですか?", null, null);
                        alert.setTimeout(Alert.FOREVER);

                        alert.addCommand(CMD_OK);
                        alert.addCommand(CMD_CANCEL);

                        alert.setCommandListener(new MyCommandListener());
                        isProcessing = false;
                        MapMIDlet.getDisplay().setCurrent(alert);
                    }
                }

            }.start();

        } else if (command == CMD_DELETE) {
            new Thread()
            {

                public void run() {
                    doDelateAll = false;
                    if (setFilenameVector()) {

                        int tmp = Integer.parseInt(cacheExpireDaysField
                                .getString());
                        if (tmp < 1) {
                            tmp = 1;
                        }
                        settings.setCacheExpireDays(tmp);
                        try {
                            RecordStoreUtil.saveSettings(settings);
                        } catch (RecordStoreException e) {

                        }

                        Alert alert = new Alert("キャッシュの削除",
                                "キャッシュディレクトリにあるファイル("
                                        + allFilenameVector.size()
                                        + "件)のうち条件に合致するもの("
                                        + filenameVector.size()
                                        + "件)を削除します。よろしいですか?", null, null);
                        alert.setTimeout(Alert.FOREVER);
                        alert.addCommand(CMD_OK);
                        alert.addCommand(CMD_CANCEL);

                        alert.setCommandListener(new MyCommandListener());

                        MapMIDlet.getDisplay().setCurrent(alert);
                    }
                }

            }.start();

        }

    }

    private class MyCommandListener implements CommandListener {
        public void commandAction(Command command, Displayable displayable) {
            if (command == CMD_OK) {
                new MyAction().start();
            } else if (command == CMD_CANCEL) {
                isProcessing = false;
                MapMIDlet.getDisplay().setCurrent(
                        CacheOperationForm.getInstance(null));
            }
        }
    }

    private class MyAction extends Thread {
        public void run() {
            MapMIDlet.getDisplay().setCurrent(
                    CacheOperationForm.getInstance(null));
            String cacheDir = Settings.getInstance().getCacheDir();
            CacheFilenames.getInstance().clear();
            setTicker(new Ticker("ファイルの削除中"));

            int deleteCounter = 0;

            if (filenameVector == null) {
                // あえりないはず
                setTicker(null);
                return;
            }

            int size = filenameVector.size();
            for (int j = 0; j < size; ++j) {
                String filename = (String) filenameVector.elementAt(j);

                try {
                    FileUtil.delete(cacheDir + filename);
                    ++deleteCounter;
                    deletingGauge.setValue(deleteCounter * 100 / size);
                } catch (Exception e) {
                    setTicker(null);
                    Alert alert = new Alert(e.toString(), e.getMessage() + "\n"
                            + filename + "\n キャッシュの削除に失敗しました.", null, null);
                    MapMIDlet.getDisplay().setCurrent(alert);
                    isProcessing = false;
                    return;
                }
            }
            isProcessing = false;
            setTicker(null);
            // 0にもどることもないだろう
            // deletingGauge.setValue(0);
            Alert alert = new Alert("キャッシュの削除成功", "キャッシュの削除に成功しました." + "\n"
                    + filenameVector.size() + "件のキャッシュを削除しました。", null, null);
            MapMIDlet.getDisplay().setCurrent(alert);

        }
    }
}
