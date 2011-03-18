import java.io.IOException;
import java.util.Enumeration;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Ticker;
import javax.microedition.lcdui.game.GameCanvas;

public class AozoraCanvas extends GameCanvas implements CommandListener,
        Runnable {

    // TODO: リファクタリング
    // TODO: 定数(文字列, Alert, Ticker)のくくりだし

    private final Ticker TICKER_YOKOTATE_KIRIKAETYU = new Ticker("横/縦切り変え中");

    private final Ticker TICKER_FILE_INFO_SYUSYUTYU = new Ticker("ファイル情報の収集中");

    private final Ticker TICKER_FILE_YOMIKOMITYU = new Ticker("ファイル読み込み中");

    private final Alert ALERT_RUBY_UNCLOSED = new Alert("ルビが閉じられていません",
            "ルビが閉じられていません", null, null);

    private Alert alertYomikomiSippai = new Alert("ファイルの読み込みに失敗しました",
            "ファイルの読み込みに失敗しました: ", null, null);

    // private static final Ticker TICKER_SIORI_TUIKA = new
    // Ticker("しおりを追加しました");

    /**
     * 0行めの xないしy座標
     */
    private int zeroPoint;

    /*
     * 最初のzeroPoint
     */
    private int initialZeroPoint;

    /**
     * 行(ルビと本文をあわせたものの幅か高さ
     */
    private int lineWidthOrHeight;

    /**
     * 一行ないし一列に入る最低の文字数
     */
    private int leastCharNumber;

    /**
     * ディレクトリリスト画面
     */
    private DirectoryList directoryList;

    /**
     * 行ごとに分割されたファイル部分(content)の文字列
     */
    private Vector fileStrings;

    private Vector tategakiFileStrings;

    /**
     * 表示につかう文字列(縦書用の変換がされてている)
     */
    private Vector strings;

    /**
     * 横書と縦書を切替えるコマンド
     */
    private final Command CMD_KIRIKAE = new Command("横/縦", Command.SCREEN, 10);

    /**
     * ディレクトリリスト画面に行くコマンド
     */
    private final Command CMD_BACK = new Command("ファイル選択", Command.BACK, 1);

    /**
     * 設定画面を呼び出すコマンド
     */
    private final Command CMD_SETTEI = new Command("設定", Command.SCREEN, 10);

    private final Command CMD_SIORI_TUIKA = new Command("しおりを追加",
            Command.SCREEN, 10);

    private final Command CMD_SIORI_LIST = new Command("しおりのリスト",
            Command.SCREEN, 10);

    /**
     * 縦書か否か
     */
    private boolean isTategaki = false;

    /**
     * ファイルに関する情報を持つクラス
     */
    private FileInfo fileInfo;

    /**
     * Canvas上でのすべての行数
     */
    private int allOfLineNumbers;

    /**
     * Canvas上での行数
     */
    private int lineNumber;

    /**
     * ファイル部分(content)の行ごとに、Canvas上では何行になるかの配列
     */
    private int[] fileLineNumbers;

    /**
     * 現在描画を開始しているファイル部分での行数
     */
    private int fileLineNumber;

    /**
     * Display
     */
    private Display display;

    /*
     * 実行中の状態を表す変数たち Stateパターン使うにはリソースが足りないだろう。
     */
    /**
     * キーが押された直後か
     */
    private boolean doKeyPressed;

    /**
     * 横/縦ないしフォント切り変え時か
     */
    private boolean doKirikae;

    /**
     * paint()する準備はできているか
     */
    private boolean doPaint;

    /**
     * 前ページに戻っている途中か
     */
    private boolean doPrev;

    /**
     * 次ページにすすんでいる途中か
     */

    private boolean doNext;

    /**
     * 設定中か(どういうわけか右上が押されていることになっているので)
     */
    private boolean doSettei;

    /**
     * 設定
     */
    private Settings settings;

    /**
     * Plainな部分を描画するフォント
     */
    private Font plainTextFont;

    /**
     * ルビの部分を描画するフォント
     */
    private Font rubyTextFont;

    /**
     * 一度に移動する行数
     */
    private int step;

    /**
     * キーを押しっぱなしにしたときに一度に移動する行する
     */
    private int fastStep;

    private int textColor;

    private int backGroundColor;

    private int widthBetweenPlains;

    private int widthBetweenPlainAndRuby;

    private int widthBetweenRubyAndPlain;

    private Thread keyProcessThread = null;

    private Thread tategakiProcessThread = null;

    private static AozoraCanvas aozoraCanvas = new AozoraCanvas();

    public static AozoraCanvas getInstance(DirectoryList directoryList,
            FileInfo fileInfo) {
        aozoraCanvas.directoryList = directoryList;
        // if (aozoraCanvas.fileInfo != null) {
        // aozoraCanvas.fileInfo.clean();
        // }
        aozoraCanvas.fileInfo = fileInfo;

        aozoraCanvas.setUpStrings();
        return aozoraCanvas;
    }

    private AozoraCanvas() {
        super(false);

        display = AozoraMIDlet.getDisplay();
        // this.directoryList = directoryList;
        // this.fileInfo = fileInfo;
        this.settings = Settings.getInstance();

        addCommand(CMD_BACK);
        addCommand(CMD_KIRIKAE);
        addCommand(CMD_SETTEI);
        addCommand(CMD_SIORI_TUIKA);
        addCommand(CMD_SIORI_LIST);
        setCommandListener(this);

    }

    private void setUpSettingInfos() {

        step = settings.getStep();
        fastStep = settings.getFastStep();
        // plainTextFont = settings.getPlainTextFont();
        // rubyTextFont = settings.getRubyTextFont();
        textColor = settings.getTextColor();
        backGroundColor = settings.getBackgroundColor();
        widthBetweenPlains = settings.getWidthBetweenPlains();
        widthBetweenPlainAndRuby = settings.getWidthBetweenPlainAndRuby();
        widthBetweenRubyAndPlain = settings.getWidthBetweenRubyAndPlain();

        plainTextFont = fileInfo.getPlainTextFont();
        rubyTextFont = fileInfo.getRubyTextFont();

    }

    private void setUpStrings() {

        setTicker(TICKER_FILE_INFO_SYUSYUTYU);

        fileStrings = splitString(fileInfo.getContent());

        fileLineNumbers = new int[fileStrings.size()];

        tategakiFileStrings = null;

        setUpStates();

    }

    private void setUpStates() {
        setUpSettingInfos();

        if (keyProcessThread == null) {
            keyProcessThread = new Thread(this);
            keyProcessThread.start();
        }

        allOfLineNumbers = 0;

        isTategaki = fileInfo.isTategaki();

        if (isTategaki) {

            if (tategakiFileStrings != null) {
                strings = tategakiFileStrings;
            } else {
                if (tategakiProcessThread != null
                        && tategakiProcessThread.isAlive()) {
                    try {
                        tategakiProcessThread.join();
                    } catch (Exception e) {
                        tategakiFileStrings = TategakiUtil
                                .convertToTategakiStrings(fileStrings);
                    }
                } else {

                    tategakiFileStrings = TategakiUtil
                            .convertToTategakiStrings(fileStrings);
                }
                strings = tategakiFileStrings;
            }

            if (rubyTextFont != null) {
                initialZeroPoint = getWidth() - rubyTextFont.stringWidth("あ");
                lineWidthOrHeight = rubyTextFont.stringWidth("あ")
                        + plainTextFont.stringWidth("あ")
                        + widthBetweenPlainAndRuby + widthBetweenRubyAndPlain;
            } else {
                initialZeroPoint = getWidth();
                lineWidthOrHeight = plainTextFont.stringWidth("あ")
                        + widthBetweenPlains;

            }
            leastCharNumber = getHeight() / plainTextFont.getHeight();

        } else {

            if (rubyTextFont != null) {
                initialZeroPoint = rubyTextFont.getHeight();
                lineWidthOrHeight = rubyTextFont.getHeight()
                        + plainTextFont.getHeight() + widthBetweenPlainAndRuby
                        + widthBetweenRubyAndPlain;

            } else {
                initialZeroPoint = 0;
                lineWidthOrHeight = plainTextFont.getHeight()
                        + widthBetweenPlains;
            }

            leastCharNumber = getWidth() / plainTextFont.stringWidth("あ");
            strings = fileStrings;

            if (tategakiFileStrings == null
                    && (tategakiProcessThread == null || !tategakiProcessThread
                            .isAlive())) {
                tategakiProcessThread = new Thread()
                {
                    public void run() {
                        tategakiFileStrings = TategakiUtil
                                .convertToTategakiStrings(fileStrings);
                    }
                };
                tategakiProcessThread.start();

            }
        }

        doPaint = false;
        new Thread()
        {

            public void run() {
                int all = 0;
                int kirikaeLineNumber = 0;
                int size = strings.size();

                for (int i = 0; i < size; ++i) {
                    fileLineNumbers[i] = 0;
                    parseAndDrawString(null, (String) strings.elementAt(i), 0,
                            i, false);

                    all += fileLineNumbers[i];

                    if (fileLineNumber > i) {
                        kirikaeLineNumber += fileLineNumbers[i];
                    }
                }
                allOfLineNumbers = all;

                if (doNext) {

                    setLineNumber(0);

                } else if (doPrev) {
                    int line;
                    if (isTategaki) {
                        line = getWidth() / lineWidthOrHeight;
                    } else {
                        line = getHeight() / lineWidthOrHeight;

                    }

                    setLineNumber(allOfLineNumbers - line);

                } else if (doKirikae) {

                    setLineNumber(kirikaeLineNumber);

                } else {
                    setLineNumber(fileInfo.getLineNumber());
                }
                doNext = false;
                doPrev = false;
                doKirikae = false;
                doSettei = false;
                doPaint = true;
                setTicker(null);
                repaint();
            }

        }.start();

    }

    private Vector splitString(String string) {
        Vector vector = new Vector();

        int indexBegin = 0;
        int indexEnd;

        if (fileInfo.hasPrev()) {
            vector.addElement("--- これより前の文章を読むには[7]キーを押してください---");
            vector.addElement("");

        }

        while (true) {

            // indexEnd = fileContent.indexOf('\n', indexBegin);
            indexEnd = string.indexOf("\r\n", indexBegin);

            if (indexEnd == -1) {
                vector.addElement(ignoreRubyStartingChar(string
                        .substring(indexBegin)));

                break;
            }

            vector.addElement(ignoreRubyStartingChar(string.substring(
                    indexBegin, indexEnd)));

            indexBegin = indexEnd + 2;

        }

        if (fileInfo.hasNext()) {
            vector.addElement("");
            vector.addElement("--- これより後の文章を読むには[9]キーを押してください---");
        }

        return vector;
    }

    private void drawPlainString(Graphics g, String string, int point) {
        if (isTategaki) {
            if (point >= 0 && point <= getWidth()) {

                g.setFont(plainTextFont);
                drawTategakiString(g, string, point, 0, Graphics.TOP
                        | Graphics.RIGHT, plainTextFont.getHeight());
            }

        } else {
            if (point >= 0 && point <= getHeight()) {
                g.setFont(plainTextFont);
                g.drawString(string, 0, point, Graphics.TOP | Graphics.LEFT);

            }
        }

    }

    private void drawTategakiString(Graphics g, String string, int x, int y,
            int anchor, int fontHeight) {

        int charWidth = g.getFont().stringWidth("あ'");
        int length = string.length();

        for (int i = 0; i < length; ++i) {

            g.drawChar(string.charAt(i), (int) (x + charWidth
                    * TategakiUtil.doZursu(string.charAt(i))), y + fontHeight
                    * i, anchor);

        }

    }

    private void drawRubyString(Graphics g, String string, int x, int y) {

        if (isTategaki) {
            if (x >= 0 && x <= getWidth() && rubyTextFont != null) {
                g.setFont(rubyTextFont);
                int i = y - rubyTextFont.getHeight() * (string.length());
                if (i < 0) {
                    i = 0;
                }

                drawTategakiString(g, string, x + widthBetweenRubyAndPlain, i,
                        Graphics.TOP | Graphics.LEFT, rubyTextFont.getHeight());

            }

        } else {
            if (y >= 0 && y <= getHeight() && rubyTextFont != null) {

                g.setFont(rubyTextFont);
                if (rubyTextFont.stringWidth(string) < x) {
                    g.drawString(string, x, y - widthBetweenRubyAndPlain,
                            Graphics.BOTTOM | Graphics.RIGHT);
                } else {
                    g.drawString(string, 0, y - widthBetweenRubyAndPlain,
                            Graphics.BOTTOM | Graphics.LEFT);
                }
            }
        }
    }

    private String ignoreRubyStartingChar(String string) {

        int indexBegin = 0;

        StringBuffer buffer = new StringBuffer();

        while (true) {
            // 縦書用文字に変換する前に処理すること
            int indexTatesen = indexBegin;
            // if (isTategaki) {
            // indexTatesen = string.indexOf('―', indexBegin);
            // } else {
            indexTatesen = string.indexOf('｜', indexBegin);
            // }
            if (indexTatesen != -1) {
                buffer.append(string.substring(indexBegin, indexTatesen));
                indexBegin = indexTatesen + 1;

            } else {
                buffer.append(string.substring(indexBegin));
                break;
            }

        }

        return buffer.toString();
    }

    private String prosessString(String string, Vector rubyVector) {

        int indexBegin = 0;

        // string = ignoreRubyStartingChar(string);

        StringBuffer buffer = new StringBuffer();
        int rubyDiff = 0;

        // ルビの抽出
        while (true) {

            int indexRubyOpen;
            int indexRubyClose;

            indexRubyOpen = string.indexOf('《', indexBegin);
            if (indexRubyOpen == -1) {
                buffer.append(string.substring(indexBegin));

                break;
            }

            indexRubyClose = string.indexOf('》', indexRubyOpen);

            if (indexRubyClose == -1) {
                display.setCurrent(ALERT_RUBY_UNCLOSED);
                throw new IllegalArgumentException("ruby is not closed");
            }

            if (indexRubyOpen + 1 < indexRubyClose) {

                RubyInfo info = new RubyInfo(indexRubyOpen - rubyDiff, string
                        .substring(indexRubyOpen + 1, indexRubyClose));
                rubyVector.addElement(info);
                rubyDiff += indexRubyClose - indexRubyOpen + 1;
            }

            buffer.append(string.substring(indexBegin, indexRubyOpen));

            indexBegin = indexRubyClose + 1;

        }

        return buffer.toString();

    }

    private int movePaintingPoint(int point) {
        if (isTategaki) {
            return point - lineWidthOrHeight;
        } else {
            return point + lineWidthOrHeight;
        }
    }

    private RubyInfo drawRubyStrings(Graphics g, RubyInfo rubyInfo,
            Enumeration e, String string, int point, int index) {

        while (true) {
            if (rubyInfo != null) {
                if (rubyInfo.getIndex() > index + string.length()) {
                    return rubyInfo;
                }

                if (isTategaki) {
                    drawRubyString(g, rubyInfo.getRuby(), point, (rubyInfo
                            .getIndex() - index)
                            * plainTextFont.getHeight());
                } else {

                    drawRubyString(g, rubyInfo.getRuby(), plainTextFont
                            .substringWidth(string, 0, rubyInfo.getIndex()
                                    - index), point);
                }

                if (e.hasMoreElements()) {
                    rubyInfo = (RubyInfo) e.nextElement();

                } else {
                    return null;
                }

            } else {
                break;
            }

        }
        return null;

    }

    private int parseAndDrawString(Graphics g, String string, int point,
            int lineNumber, boolean isDrawing) {

        Vector rubyVector = new Vector();

        int indexBegin = 0;

        String processedString = prosessString(string, rubyVector);

        Enumeration e = rubyVector.elements();

        RubyInfo rubyInfo = null;
        if (e.hasMoreElements()) {
            rubyInfo = (RubyInfo) e.nextElement();
        }

        int count = 0; // 行数をカウント

        while (true) {
            // 一行に確実に収まる文字数なら
            if (processedString.substring(indexBegin).length() <= leastCharNumber) {

                if (isDrawing) {

                    drawPlainString(g, processedString.substring(indexBegin),
                            point);
                    rubyInfo = drawRubyStrings(g, rubyInfo, e, processedString
                            .substring(indexBegin), point, indexBegin);
                }

                point = movePaintingPoint(point);

                ++count;
                break;
            }

            // 縦書の場合は一行に収まるかどうか確実にわかる。
            // ここにきているなら、収まっていないということ
            if (isTategaki) {

                if (isDrawing) {
                    drawPlainString(g, processedString.substring(indexBegin,
                            indexBegin + leastCharNumber), point);

                    rubyInfo = drawRubyStrings(g, rubyInfo, e,
                            processedString.substring(indexBegin, indexBegin
                                    + leastCharNumber), point, indexBegin);
                }

                indexBegin += leastCharNumber;
                point = movePaintingPoint(point);
                ++count;
                if (isDrawing && point < 0) {
                    break;
                }

            } else {
                // 一行に収まるかわからない場合
                int end = processedString.length();

                // 一行に収まらなかったらtrue;
                boolean isItigyo = true;

                for (int i = indexBegin + leastCharNumber; i < end; ++i) {

                    if (plainTextFont.substringWidth(processedString,
                            indexBegin, (i - indexBegin) + 1) > getWidth()) {

                        if (isDrawing) {

                            drawPlainString(g, processedString.substring(
                                    indexBegin, i - 1), point);

                            rubyInfo = drawRubyStrings(g, rubyInfo, e,
                                    processedString
                                            .substring(indexBegin, i - 1),
                                    point, indexBegin);
                        }

                        indexBegin = i - 1;

                        point = movePaintingPoint(point);
                        ++count;
                        isItigyo = false;
                        break;

                    }
                }

                // 一行に収まる場合
                if (isItigyo) {

                    if (isDrawing) {
                        drawPlainString(g, processedString
                                .substring(indexBegin), point);

                        rubyInfo = drawRubyStrings(g, rubyInfo, e,
                                processedString.substring(indexBegin), point,
                                indexBegin);
                    }

                    point = movePaintingPoint(point);
                    ++count;
                    break;

                }
            }

        }
        if (!isDrawing) {
            fileLineNumbers[lineNumber] = count;
        }

        return point;

    }

    private void parseAndDrawFileContent(Graphics g) {

        int fileLine = 0;
        int lines = 0;
        int length = fileLineNumbers.length;

        for (; fileLine < length; ++fileLine) {

            if (fileLineNumbers[fileLine] == 0) {
                break;
            }

            lines += fileLineNumbers[fileLine];

            if (lineNumber < lines) {
                lines -= fileLineNumbers[fileLine];
                break;
            }

        }

        int point;

        if (isTategaki) {
            point = zeroPoint - lines * lineWidthOrHeight;
        } else {
            point = zeroPoint + lines * lineWidthOrHeight;
        }

        fileLineNumber = fileLine;

        if (fileLine > 0 && fileLineNumbers[fileLine - 1] > 1) {
            ++fileLineNumber;
        }

        int size = strings.size();

        while (fileLine < size) {

            point = parseAndDrawString(g, (String) strings.elementAt(fileLine),
                    point, fileLine, true);

            if (isTategaki) {
                if (point < 0) {

                    break;
                }
            } else {
                if (point > getHeight()) {
                    break;
                }
            }
            ++fileLine;
        }

    }

    public void paint(Graphics g) {

        g.setColor(backGroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (doPaint) {

            g.setColor(textColor);

            // スクロールバー
            if (allOfLineNumbers > 0) {
                if (isTategaki) {
                    g.drawLine(0, getHeight() - 4, getWidth(), getHeight() - 4);

                    g.fillRect(getWidth()
                            - (lineNumber * getWidth() / allOfLineNumbers) - 6,

                    getHeight() - 8, 12, 8);

                } else {

                    g.drawLine(getWidth() - 4, 0, getWidth() - 4, getHeight());

                    g.fillRect(getWidth() - 8,
                            (lineNumber * getHeight() / allOfLineNumbers) - 6,
                            8, 12);
                }

            }

            parseAndDrawFileContent(g);

            try {
                RecordStoreUtil.saveCurrentFileInfo(fileInfo);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    protected void keyPressed(int keyCode) {
        if (doSettei) {
            return;
        }

        if (isTategaki) {
            switch (getGameAction(keyCode))
                {
                case Canvas.RIGHT:
                    processRightPressed(step);
                    doKeyPressed = true;
                    break;
                case Canvas.LEFT:
                    processLeftPressed(step);
                    doKeyPressed = true;
                    break;

                }

        } else {
            switch (getGameAction(keyCode))
                {
                case Canvas.UP:
                    processUpPressed(step);
                    doKeyPressed = true;
                    break;
                case Canvas.DOWN:
                    processDownPressed(step);
                    doKeyPressed = true;
                    break;

                }
        }

        switch (keyCode)
            {
            case KEY_NUM7:
                if (fileInfo.hasPrev()) {
                    new Thread()
                    {
                        public void run() {

                            try {

                                setTicker(TICKER_FILE_YOMIKOMITYU);

                                int offset = ((Integer) fileInfo
                                        .getOffsetStack().pop()).intValue();
                                fileInfo = FileUtil.getFileContent(fileInfo
                                        .getPath(), offset, fileInfo
                                        .getOffsetStack());

                                fileInfo.setTategaki(isTategaki);

                                doPrev = true;
                                setUpStrings();
                            } catch (IOException e) {
                                alertYomikomiSippai
                                        .setString(alertYomikomiSippai
                                                .getTitle()
                                                + ": " + e.toString());

                                display.setCurrent(alertYomikomiSippai);

                            }
                        }
                    }.start();
                }
                break;
            case KEY_NUM9:
                if (fileInfo.hasNext()) {

                    new Thread()
                    {
                        public void run() {

                            try {
                                setTicker(TICKER_FILE_YOMIKOMITYU);

                                fileInfo.getOffsetStack().push(
                                        new Integer(fileInfo.getOffset()));

                                fileInfo = FileUtil.getFileContent(fileInfo
                                        .getPath(), fileInfo.getEnd() + 1,
                                        fileInfo.getOffsetStack());
                                fileInfo.setTategaki(isTategaki);

                                doNext = true;
                                setUpStrings();
                            } catch (IOException e) {
                                alertYomikomiSippai
                                        .setString(alertYomikomiSippai
                                                .getTitle()
                                                + ": " + e.toString());
                                display.setCurrent(alertYomikomiSippai);
                            }
                        }
                    }.start();
                }

                break;

            }

    }

    public void commandAction(Command command, Displayable arg1) {

        if (command == CMD_KIRIKAE) {

            setTicker(TICKER_YOKOTATE_KIRIKAETYU);

            doKirikae = true;
            if (isTategaki) {
                isTategaki = false;
            } else {
                isTategaki = true;
            }
            fileInfo.setTategaki(isTategaki);
            setUpStates();

        } else if (command == CMD_BACK) {
            display.setCurrent(directoryList);
        } else if (command == CMD_SETTEI) {
            doSettei = true;

            display.setCurrent(SettingsForm.getInstance(this));
        } else if (command == CMD_SIORI_TUIKA) {

            doSettei = true;
            Bookmark bookmark = Bookmark.getInstance(fileInfo);

            BookmarkRegisterForm bookmarkRegisterForm = BookmarkRegisterForm
                    .getInstance(bookmark, this);

            display.setCurrent(bookmarkRegisterForm);

        } else if (command == CMD_SIORI_LIST) {
            doSettei = true;
            display.setCurrent(BookmarksList.getInstance(this));

        }

    }

    private void setLineNumber(int lineNumber) {

        this.lineNumber = lineNumber;
        if (lineNumber < 0) {
            this.lineNumber = 0;
        } else if (lineNumber > allOfLineNumbers) {
            this.lineNumber = allOfLineNumbers - 1;
        }

        if (isTategaki) {
            zeroPoint = initialZeroPoint + this.lineNumber * lineWidthOrHeight;

        } else {
            zeroPoint = initialZeroPoint - this.lineNumber * lineWidthOrHeight;
        }
        fileInfo.setTategaki(isTategaki);
        fileInfo.setLineNumber(this.lineNumber);

    }

    private int getLineNumber() {
        return lineNumber;
    }

    private void processRightPressed(int step) {

        setLineNumber(getLineNumber() - step);
        repaint();
    }

    private void processLeftPressed(int step) {

        setLineNumber(getLineNumber() + step);
        repaint();
    }

    private void processUpPressed(int step) {

        setLineNumber(getLineNumber() - step);
        repaint();
    }

    private void processDownPressed(int step) {

        setLineNumber(getLineNumber() + step);
        repaint();
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        doSettei = true;
        setUpStrings();
    }

    public void run() {
        // ThreadではなくTimerを使うのもいいかもしれない
        while (true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }

            // keyPressed()を処理したときには、
            // 処理をしすぎないように一回捨てる
            // 設定時も処理を拾ってるようなので無視させる
            if (doKeyPressed || doSettei) {
                getKeyStates();
                doKeyPressed = false;

                continue;
            }
            // Check user input and update positions if necessary
            int keyState = getKeyStates();
            if (isTategaki) {

                if ((keyState & RIGHT_PRESSED) != 0) {

                    processRightPressed(fastStep);

                } else if ((keyState & LEFT_PRESSED) != 0) {
                    processLeftPressed(fastStep);

                }
            } else {
                if ((keyState & UP_PRESSED) != 0) {
                    processUpPressed(fastStep);

                } else if ((keyState & DOWN_PRESSED) != 0) {
                    processDownPressed(fastStep);

                }

            }
        }

    }

    /**
     * 設定が終了したことを教えるメソッド
     * 
     * @param doKirikae
     *            フォントが切り替わったかどうか
     */
    public void finalizeSettei(boolean doKirikae) {
        this.doKirikae = doKirikae;
        this.doSettei = false;
        if (doKirikae) {
            fileInfo.setRubyTextSize(settings.getRubyTextSize());
            fileInfo.setPlainTextSize(settings.getPlainTextSize());
            fileInfo.setTategaki(settings.isTategaki());
        }
        setUpStates();
    }

    public DirectoryList getDirectoryList() {
        return directoryList;
    }

    public void setDirectoryList(DirectoryList directoryList) {
        this.directoryList = directoryList;
    }

}
