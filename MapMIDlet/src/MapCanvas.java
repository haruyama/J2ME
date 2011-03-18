import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.RecordStoreException;

public class MapCanvas extends Canvas implements Runnable, CommandListener {

    private final static Command CMD_ZOOMUP = new Command("up", Command.SCREEN,
            2);

    private final static Command CMD_ZOOMDOWN = new Command("down",
            Command.SCREEN, 3);

    private final static Command CMD_ADD_BOOKMARK = new Command("ブックマークに追加する",
            Command.SCREEN, 5);

    private final static Command CMD_BOOKMARKS_LIST = new Command(
            "ブックマークリストを見る", Command.SCREEN, 5);

    private final static Command CMD_SEARCH = new Command("検索", Command.SCREEN,
            5);

    private final static Command CMD_INFOSET = new Command("座標・ズームの直接指定",
            Command.SCREEN, 5);

    private final static Command CMD_SETTEI = new Command("設定", Command.SCREEN,
            5);

    private final static Command CMD_HELP = new Command("ショートカットキー一覧",
            Command.SCREEN, 10);

    private final static Command CMD_REPAINT = new Command("地図の再描画",
            Command.SCREEN, 10);
    
    
    private final static Command CMD_SELECT = new Command("検索結果の再参照",
            Command.SCREEN, 10);
    
    
    public static final int GOOGLE_MAPS_MIN_ZOOM = -2; 
    public static final int GOOGLE_MAPS_MAX_ZOOM = 17; 

    private static final int CONNECTION_FAILED = 0;

    private static final int CONNECTION_CATCH_OUTOFMEMORYERROR = 2;

    private static final int CONNECTION_OK = 1;

    private int canvasX;

    private int canvasY;

    private Hashtable imageInfos = new Hashtable();

    private int height;

    private int width;

    private int mapX;

    private int mapY;

    private int mapZoom;

    private int lastRelativeX;

    private int lastRelativeY;

    // private boolean doDisplayInfo;

    private Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
            Font.SIZE_SMALL);

    // private boolean doDisplayCenter;

    private Hashtable loadingImageInfos = new Hashtable();

    private static MapCanvas canvas = new MapCanvas();

    /*
     * private ProcessKeyThread processKeyThread;
     * 
     * private boolean doProcessKeyThread = false;
     */

    private boolean doKeyRepeated;

    public static MapCanvas getInstance(int x, int y, int zoom) {
        // canvas.doProcessKeyThread = true;
        canvas.doKeyRepeated = true;

        canvas.loadInitialImage(x, y, zoom, 0, 0);
        return canvas;
    }

    private MapCanvas() {
        // super(false);
        super();
        setCommandListener(this);
        height = getHeight();
        width = getWidth();
    }

    private void loadCache() {

        if (Settings.getInstance().isUseCache()) {
            CacheFilenames cacheFilenames = CacheFilenames.getInstance();
            for (int x = -1; x < 2; ++x) {

                for (int y = -1; y < 2; ++y) {

                    String imagePath = ImageInfo.getImagePath(getCurrentMapX()
                            + x, getCurrentMapY() + y, getMapZoom());

                    // 画像のあるなしは同期させてチェックしたほうがよいのではないかと
                    // 思うが、本当に効果があるかはわからない
                    // 実機ではひっかかるようになった(?)ので見送り
                    // synchronized (this) {

                    if (imageInfos.containsKey(imagePath)) {
                        continue;
                    }
                    if (loadingImageInfos.containsKey(imagePath)) {
                        continue;
                    }

                    if (cacheFilenames.contains(imagePath,
                            getCurrentMapX() + x, getMapZoom())) {

                        try {

                            ImageInfo imageInfo = ImageInfo.getInstance(
                                    Settings.getInstance().getCacheDir(),
                                    imagePath, lastRelativeX + x, lastRelativeY
                                            + y);

                            imageInfos.put(imagePath, imageInfo);

                        } catch (IOException e) {
                            // ファイルが壊れている?
                            try {

                                Alert alert = new Alert("読み込みに失敗",
                                        "キャッシュの読み込みに失敗しました\n"
                                                + "IOExeptionが発生しています。ファイル「"
                                                + imagePath + "」を削除します。", null,
                                        null);
                                MapMIDlet.getDisplay().setCurrent(alert);

                                cacheFilenames.removeElement(imagePath);
                                FileUtil.delete(Settings.getInstance()
                                        .getCacheDir()
                                        + imagePath);
                            } catch (IOException ex) {
                            }

                        } catch (OutOfMemoryError t) {
                            Alert alert = new Alert(
                                    "読み込みに失敗",
                                    "キャッシュの読み込みに失敗しました\n"
                                            + "イメージの生成の際にメモリが足りませんでした(OutOfMemoryError)\n"
                                            + "以後正しく動作しない場合は一旦終了させてください\n"
                                            + "再描画(*キー)をすると改善されるかもしれません",

                                    null, null);
                            MapMIDlet.getDisplay().setCurrent(alert);
                        }

                    }

                    // }
                }

            }

        }

    } /*
         * protected void keyReleased(int keyCode) {
         * 
         * switch (getGameAction(keyCode)) { case Canvas.UP: case Canvas.DOWN:
         * case Canvas.LEFT: case Canvas.RIGHT: doProdessKeyRepeatedThread =
         * false; processKeyRepeatedThread = null; break; default: return; } }
         */

    protected void keyRepeated(int keyCode) {
        if (doKeyRepeated) {
            processGameAction(getGameAction(keyCode));
        }
    }

    private void processGameAction(int gameAction) {

        switch (gameAction)
            {
            case Canvas.UP:
                canvasY += 36;
                break;
            case Canvas.DOWN:
                canvasY -= 36;
                break;
            case Canvas.LEFT:
                canvasX += 36;
                break;
            case Canvas.RIGHT:
                canvasX -= 36;
                break;
            default:
                return;
            }

        // int lastRelativeX = -(canvasX + 128) / 256;
        // if (canvasX <= -128) {
        // ++lastRelativeX;
        // }

        // int lastRelativeY = -(canvasY + 128) / 256;
        // if (canvasY <= -128) {
        // ++lastRelativeY;
        // }

        int lastRelativeX = -((canvasX + 128) >> 8);

        int lastRelativeY = -((canvasY + 128) >> 8);

        repaint();

        if (this.lastRelativeX != lastRelativeX
                || this.lastRelativeY != lastRelativeY) {
            this.lastRelativeX = lastRelativeX;

            this.lastRelativeY = lastRelativeY;
            new Thread(this).start();
        }

    }

    private boolean keyPressedforWX310SA(int keyCode) {
        if (System.getProperty("microedition.platform").equals("WX310SA")) {
            switch (keyCode)
                {

                case 9:
                    // カメラキー
                    processZoomDown();
                    return true;
                case 10:
                    // Myキー
                    processZoomUp();
                    return true;
                case 11:
                // クリアキー
                    MapMIDlet.getDisplay().setCurrent(SelectList.getInstance(this,null));
                    return true;
                case 12:
                    // サイドの「マナー/REC」キー
                    return true;
                }

        }
        return false;

    }

    protected void keyPressed(int keyCode) {

        // ソフトキーのと統一させる?
        switch (keyCode)
            {

            case Canvas.KEY_NUM1:

                processZoomDown();
                return;
            case Canvas.KEY_NUM3:
                processZoomUp();
                return;

            case Canvas.KEY_NUM5:
                if (Settings.getInstance().isDisplayInfo()) {
                    Settings.getInstance().setDisplayInfo(false);
                } else {
                    Settings.getInstance().setDisplayInfo(true);
                }
                try {
                    RecordStoreUtil.saveSettings(Settings.getInstance());
                } catch (RecordStoreException e) {
                }
                repaint();
                return;

            case Canvas.KEY_NUM2:
                if (Settings.getInstance().isDisplayCenter()) {
                    Settings.getInstance().setDisplayCenter(false);
                } else {
                    Settings.getInstance().setDisplayCenter(true);
                }
                try {
                    RecordStoreUtil.saveSettings(Settings.getInstance());
                } catch (RecordStoreException e) {

                }
                repaint();
                return;
            case Canvas.KEY_NUM7:
                Bookmark bookmark = new Bookmark(getCurrentMapX(),
                        getCurrentMapY(), getMapZoom());

                BookmarkRegisterForm bookmarkRegisterForm = BookmarkRegisterForm
                        .getInstance(bookmark, this);

                MapMIDlet.getDisplay().setCurrent(bookmarkRegisterForm);
                return;

            case Canvas.KEY_NUM8:
                MapMIDlet.getDisplay().setCurrent(
                        BookmarksList.getInstance(this));
                return;
            case Canvas.KEY_NUM4:
                MapMIDlet.getDisplay().setCurrent(SearchForm.getInstance(this));
                return;
            case Canvas.KEY_NUM6:
                MapMIDlet.getDisplay().setCurrent(
                        InfoSetForm.getInstance(getCurrentMapX(),
                                getCurrentMapY(), getMapZoom(), this));
                return;
            case Canvas.KEY_NUM9:
                MapMIDlet.getDisplay().setCurrent(
                        SettingsForm.getInstance(this));
                return;
            case Canvas.KEY_POUND:
                MapMIDlet.getDisplay().setCurrent(HelpForm.getInstance(this));
                return;

            case Canvas.KEY_STAR:
                repaintImages();
                return;
            case Canvas.KEY_NUM0:
                if (Settings.getInstance().isDisplayScale()) {
                    Settings.getInstance().setDisplayScale(false);
                } else {
                    Settings.getInstance().setDisplayScale(true);
                }
                try {
                    RecordStoreUtil.saveSettings(Settings.getInstance());
                } catch (RecordStoreException e) {
                }
                repaint();
                return;
            }

        if (System.getProperty("microedition.platform").equals("WX310SA")) {
            if (keyPressedforWX310SA(keyCode)) {
                return;
            }
        }

        processGameAction(getGameAction(keyCode));
        // switch (getGameAction(keyCode))
        // {
        // case Canvas.UP:
        // case Canvas.DOWN:
        // case Canvas.LEFT:
        // case Canvas.RIGHT:
        // break;
        // default:
        // return;
        // }
        //
        //        

        /*
         * if (this.lastRelativeX != lastRelativeX || this.lastRelativeY !=
         * lastRelativeY) { this.lastRelativeX = lastRelativeX;
         * 
         * this.lastRelativeY = lastRelativeY;
         * 
         * new Thread() { public void run() {
         * 
         * loadCache();
         * 
         * String imagePath = ImageInfo.getImagePath(getCurrentMapX(),
         * getCurrentMapY(), getMapZoom());
         * 
         * boolean flag = imageInfos.containsKey(imagePath);
         *          * 
         * 
         * if (!flag) { flag = loadingImageInfos.containsKey(imagePath); }
         * 
         * 
         * if (!flag) { new Thread(canvas).start(); } else {
         * 
         * try { RecordStoreUtil.saveLastMapInfo(new Bookmark( getCurrentMapX(),
         * getCurrentMapY(), getMapZoom())); } catch (Exception e) { } }
         * 
         * repaint(); } }.start(); }
         */
    }

    private int getImageViaHttp(ImageInfo imageInfo) throws IOException {
        try {
            byte[] buffer = HttpUtil.getBytesViaHttp(imageInfo.getImageUrl());
            if (buffer != null) {
                imageInfo.setImage(buffer);
                imageInfos.put(imageInfo.getImagePath(), imageInfo);

                return CONNECTION_OK;
            }
            // } catch (IllegalArgumentException e) {
        } catch (OutOfMemoryError t) {
            return CONNECTION_CATCH_OUTOFMEMORYERROR;
        }

        return CONNECTION_FAILED;

    }

    public void paint(Graphics g) {
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, width, height);

        int shiftX = canvasX + width / 2;
        int shiftY = canvasY + height / 2;

        for (Enumeration e = imageInfos.elements(); e.hasMoreElements();) {
            ImageInfo info = (ImageInfo) e.nextElement();
            try {
                if (info.getImage() != null) {

                    if ((Math.abs(info.getRelativeX() - lastRelativeX) + Math
                            .abs(info.getRelativeY() - lastRelativeY)) < 3) {

                        // g.drawImage(info.getImage(), info.getRelativeX() *
                        // 256
                        // + canvasX + width / 2, info.getRelativeY()
                        // * 256 + canvasY + height / 2, Graphics.HCENTER
                        // | Graphics.VCENTER);

                        g.drawImage(info.getImage(), (info.getRelativeX() << 8)
                                + shiftX, (info.getRelativeY() << 8) + shiftY,
                                Graphics.HCENTER | Graphics.VCENTER);

                    }
                } else {
                    imageInfos.remove(info);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        if (Settings.getInstance().isDisplayInfo()) {
            g.setColor(0);
            g.setFont(smallFont);
            g.drawString("x=" + getCurrentMapX() + " y=" + getCurrentMapY()
                    + " zoom=" + getMapZoom(), 0, 0, Graphics.TOP
                    | Graphics.LEFT);

        }

        if (Settings.getInstance().isDisplayCenter()) {
            g.setColor(255, 0, 0);
            g.drawLine(width / 2, height / 2 - 10, width / 2, height / 2 + 10);
            g.drawLine(width / 2 - 10, height / 2, width / 2 + 10, height / 2);

        }

        // zoom=0でだいたい250m=256px (ほんとは緯度によってかわる)
        // なので97px = 100mとする
        if (Settings.getInstance().isDisplayScale()) {
            g.setColor(0);
            g.drawLine(10, getHeight() - 10, 107, getHeight() - 10);
            g.drawLine(10, getHeight() - 10, 10, getHeight() - 15);
            g.drawLine(107, getHeight() - 10, 107, getHeight() - 15);
            if (mapZoom >= 0) {
                g.drawString("約" + (100 << mapZoom) + "m", 20, getHeight() - 12,
                        Graphics.BOTTOM | Graphics.LEFT);
            } else {
                g.drawString("約" + 50 / (- mapZoom) + "m", 20, getHeight() - 12,
                        Graphics.BOTTOM | Graphics.LEFT);
                
            }

        }

    }

    public void run() {
        // 遠く離れたデータの消去
        for (Enumeration e = imageInfos.elements(); e.hasMoreElements();) {
            ImageInfo info = (ImageInfo) e.nextElement();

            int keepImageDistance = Settings.getInstance()
                    .getKeepImageDistance();
            if ((Math.abs(info.getRelativeX() - lastRelativeX) + Math.abs(info
                    .getRelativeY()
                    - lastRelativeY)) > keepImageDistance) {

                imageInfos.remove(info.getImagePath());

            }

        }

        loadCache();

        String imagePath = ImageInfo.getImagePath(getCurrentMapX(),
                getCurrentMapY(), getMapZoom());

        boolean flag = imageInfos.containsKey(imagePath);

        if (!flag) {
            flag = loadingImageInfos.containsKey(imagePath);

        }

        if (flag) {
            try {
                RecordStoreUtil.saveLastMapInfo(new Bookmark(getCurrentMapX(),
                        getCurrentMapY(), getMapZoom()));
            } catch (RecordStoreException e) {

            } catch (IOException e) {

            }


            if (loadingImageInfos.isEmpty()) {
                setTicker(null);
            }
            repaint();
            return;
        }

        setTicker(new Ticker("通信中"));
        int rc = CONNECTION_FAILED;
        ImageInfo imageInfo;
        imageInfo = new ImageInfo(getCurrentMapX(), getCurrentMapY(), mapZoom,
                lastRelativeX, lastRelativeY);
        loadingImageInfos.put(imageInfo.getImagePath(), imageInfo);

        try {

            doKeyRepeated = false;

            new Thread()
            {

                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }

                    doKeyRepeated = true;

                }
            }.start();

            rc = getImageViaHttp(imageInfo);

        } catch (IOException e) {
        } catch (SecurityException e) {
        } finally {
            loadingImageInfos.remove(imageInfo.getImagePath());
        }

        if (rc == CONNECTION_OK) {
            repaint();

            try {
                RecordStoreUtil.saveLastMapInfo(new Bookmark(getCurrentMapX(),
                        getCurrentMapY(), getMapZoom()));
            } catch (Exception e) {
                Alert alert = new Alert("RecordStoreへの書き込みに失敗しました", e
                        .toString(), null, null);
                MapMIDlet.getDisplay().setCurrent(alert);

            }

        } else if (rc == CONNECTION_FAILED) {

            Alert alert = new Alert("読み込みに失敗", "地図データ読み込みに失敗しました\n"
                    + "これはOutOfMemoryErrorに起因するものではありません\n"
                    + "地図画像がない,通信がタイムアウト, 通信を許可していないなどが考えられます。", null, null);
            MapMIDlet.getDisplay().setCurrent(alert);

        } else if (rc == CONNECTION_CATCH_OUTOFMEMORYERROR) {

            Alert alert = new Alert("読み込みに失敗",
                    "地図データ読み込みに失敗しました\n"
                            + "イメージの生成の際にメモリが足りませんでした(OutOfMemoryError)\n"
                            + "以後正しく動作しない場合は一旦終了させてください\n"
                            + "再描画(*キー)をすると改善されるかもしれません", null, null);
            MapMIDlet.getDisplay().setCurrent(alert);
        }
        if (loadingImageInfos.isEmpty()) {
            setTicker(null);
        }
        repaint();
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
     * @return Returns the mapX.
     */
    public int getCurrentMapX() {
        return mapX + lastRelativeX;
    }

    /**
     * @return Returns the mapY.
     */
    public int getCurrentMapY() {
        return mapY + lastRelativeY;
    }

    //
    // private void loadInitialImage(int canvasX, int canvasY) {
    //        
    // loadInitialImage(getCurrentMapX(), getCurrentMapY(),
    // getMapZoom(), canvasX, canvasY);
    // }

    private void loadInitialImage(int mapX, int mapY, int mapZoom, int canvasX,
            int canvasY) {
        imageInfos.clear();
        loadingImageInfos.clear();
        this.mapX = mapX;
        this.mapY = mapY;
        this.mapZoom = mapZoom;
        this.canvasX = canvasX;
        this.canvasY = canvasY;
        lastRelativeX = 0;
        lastRelativeY = 0;
        setCommand();
        if (Settings.getInstance().isUseCache()) {
            setTicker(new Ticker("キャッシュ ロード中"));
        }
        new Thread(this).start();

    }

    /**
     * @param mapX
     *            The mapX to set.
     */
    public void setMapX(int mapX) {
        this.mapX = mapX;
    }

    /**
     * @param mapY
     *            The mapY to set.
     */
    public void setMapY(int mapY) {
        this.mapY = mapY;
    }

    private void setCommand() {

        removeCommand(CMD_ZOOMUP);
        removeCommand(CMD_ZOOMDOWN);

        removeCommand(CMD_SEARCH);
        removeCommand(CMD_INFOSET);
        removeCommand(CMD_ADD_BOOKMARK);
        removeCommand(CMD_BOOKMARKS_LIST);
        removeCommand(CMD_SETTEI);
        removeCommand(CMD_REPAINT);
        removeCommand(CMD_SELECT);

        removeCommand(CMD_HELP);


        if (getMapZoom() > GOOGLE_MAPS_MIN_ZOOM) {
            addCommand(CMD_ZOOMUP);
        }
        if (getMapZoom() < GOOGLE_MAPS_MAX_ZOOM) {
            addCommand(CMD_ZOOMDOWN);
        }

        addCommand(CMD_SEARCH);
        addCommand(CMD_INFOSET);
        addCommand(CMD_ADD_BOOKMARK);
        addCommand(CMD_BOOKMARKS_LIST);
        addCommand(CMD_SETTEI);
        addCommand(CMD_REPAINT);
        addCommand(CMD_SELECT);
        addCommand(CMD_HELP);

    }

    public void commandAction(Command command, Displayable arg1) {
        // TODO: KeyPressed と統合するか?
        if (command == CMD_ZOOMUP) {
            processZoomUp();

        } else if (command == CMD_ZOOMDOWN) {
            processZoomDown();
        } else if (command == CMD_ADD_BOOKMARK) {
            Bookmark bookmark = new Bookmark(getCurrentMapX(),
                    getCurrentMapY(), getMapZoom());

            BookmarkRegisterForm bookmarkRegisterForm = BookmarkRegisterForm
                    .getInstance(bookmark, this);

            MapMIDlet.getDisplay().setCurrent(bookmarkRegisterForm);
        } else if (command == CMD_BOOKMARKS_LIST) {
            MapMIDlet.getDisplay().setCurrent(BookmarksList.getInstance(this));

        } else if (command == CMD_SEARCH) {
            MapMIDlet.getDisplay().setCurrent(SearchForm.getInstance(this));
        } else if (command == CMD_INFOSET) {
            MapMIDlet.getDisplay().setCurrent(
                    InfoSetForm.getInstance(getCurrentMapX(), getCurrentMapY(),
                            getMapZoom(), this));

        } else if (command == CMD_SETTEI) {
            MapMIDlet.getDisplay().setCurrent(SettingsForm.getInstance(this));
        } else if (command == CMD_HELP) {
            MapMIDlet.getDisplay().setCurrent(HelpForm.getInstance(this));
        } else if (command == CMD_REPAINT) {
            repaintImages();
        } else if (command == CMD_SELECT) {
            MapMIDlet.getDisplay().setCurrent(SelectList.getInstance(this,null));
        }
    }

    private void repaintImages() {
        int x = canvasX + (lastRelativeX << 8);
        int y = canvasY + (lastRelativeY << 8);

        loadInitialImage(getCurrentMapX(), getCurrentMapY(), getMapZoom(), x, y);
    }

    /**
     * 
     */
    private void processZoomDown() {
        if (getMapZoom() < GOOGLE_MAPS_MAX_ZOOM) {
            // setMapZoom(getMapZoom() + 1);
            // setMapX(getCurrentMapX() / 2);
            // setMapY(getCurrentMapY() / 2);
            loadInitialImage(getCurrentMapX() / 2, getCurrentMapY() / 2,
                    getMapZoom() + 1, 0, 0);
        }

    }

    /**
     * 
     */
    private void processZoomUp() {

        if (getMapZoom() > GOOGLE_MAPS_MIN_ZOOM) {

            int modX = canvasX % 256;
            int modY = canvasY % 256;

            if (modX < 0) {
                modX += 256;
            }

            if (modY < 0) {
                modY += 256;
            }

            int correctionX = 0;
            int correctionY = 0;
            if (modX >= 128) {
                correctionX = 1;

            }

            if (modY >= 128) {

                correctionY = 1;
            }
            //
            // setMapZoom(getMapZoom() - 1);
            // setMapX(getCurrentMapX() * 2 + correctionX);
            // setMapY(getCurrentMapY() * 2 + correctionY);

            loadInitialImage(getCurrentMapX() * 2 + correctionX,
                    getCurrentMapY() * 2 + correctionY, getMapZoom() - 1, 0, 0);
        }

    }

    /*
     * private class ProcessKeyThread extends Thread {
     * 
     * private ProcessKeyThread() { }
     * 
     * public void run() {
     * 
     * do {
     * 
     * int keyState = getKeyStates(); if (isShown() && doProcessKeyThread) {
     * 
     * if ((keyState & RIGHT_PRESSED) != 0) { processGameAction(Canvas.RIGHT); }
     * else if ((keyState & LEFT_PRESSED) != 0) {
     * processGameAction(Canvas.LEFT); } else if ((keyState & UP_PRESSED) != 0) {
     * 
     * processGameAction(Canvas.UP); } else if ((keyState & DOWN_PRESSED) != 0) {
     * processGameAction(Canvas.DOWN); } } try { Thread.sleep(200); } catch
     * (Exception e) { } } while (doProcessKeyThread); } }
     */
}
