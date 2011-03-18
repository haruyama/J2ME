import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStoreException;

public class MapMIDlet extends MIDlet {

    // private final static Command CMD_EXIT = new Command("Exit", Command.EXIT,
    // 1);
    private static MapMIDlet midlet;

    private boolean isInit = true;

    public static Display getDisplay() {
        return Display.getDisplay(midlet);
    }

    private static void setMIDlet(MapMIDlet m) {
        midlet = m;
    }

    public MapMIDlet() {
        setMIDlet(this);

    }

    protected void startApp() throws MIDletStateChangeException {
        if (isInit) {
            isInit = false;

            try {
                RecordStoreUtil.loadBookmarks();
            } catch (IOException e) {

            } catch (RecordStoreException e) {

            }

            Alert alert = new Alert(
                    "注意",
                    "このアプリケーションはGoogle Mapsの画像をGoogle Maps APIを用いずに取得し使用しています。"
                            + "これは正当な利用法ではありません。"
                            + "Google, Zenrin, NAVTEQなどが画像の権利を所有してます。\n"
                            // + "検索機能のためにGeocoding REST
                            // API(http://www.geocoding.jp/api/)を利用してます。",
                            + "検索機能のためにCSISシンプルジオコーディング実験(街区レベル位置参照情報, 国土数値情報 鉄道, 数値地図25000(地名・公共施設)2001年版)を利用しています",
                    null, null);

            alert.setTimeout(Alert.FOREVER);

            alert.addCommand(new Command("OK", Command.OK, 1));

            alert.setCommandListener(new MyCommandListener());

            Display.getDisplay(this).setCurrent(alert);
        }
    }

    protected void pauseApp() {

    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        if (arg0 == true) {
            notifyDestroyed();
        }
        return;

    }

    private static class MyCommandListener implements CommandListener {
        public void commandAction(Command arg0, Displayable arg1) {
            // 渋谷の情報
            new MyAction().start();
        }
    }

    private static class MyAction extends Thread {
        public void run() {

            int x = 116401;
            int y = 51623;
            int zoom = 0;

            try {
                Bookmark bookmark = RecordStoreUtil.loadLastMapInfo();

                x = bookmark.getX();
                y = bookmark.getY();
                zoom = bookmark.getZoom();
            } catch (IOException e) {
            } catch (RecordStoreException e) {

            }

            MapCanvas canvas = MapCanvas.getInstance(x, y, zoom);
            MapMIDlet.getDisplay().setCurrent(canvas);
        }

    }

}
