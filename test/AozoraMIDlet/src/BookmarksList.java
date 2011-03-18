import java.util.Enumeration;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

public class BookmarksList extends List implements CommandListener, Runnable {
    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);

    private final Command CMD_SELECT = new Command("選択", Command.ITEM, 1);

    private final Command CMD_SAKUZYO = new Command("削除", Command.ITEM, 1);

    private Displayable previousDisplayable;
    
    private static BookmarksList bookmarksList = new BookmarksList();
    
    public static BookmarksList getInstance(Displayable displayable) {
        bookmarksList.previousDisplayable = displayable;
        bookmarksList.init();
        return bookmarksList;
    }
    

    private BookmarksList() {
        super("しおりのリスト", IMPLICIT);
        setSelectCommand(CMD_SELECT);
        addCommand(CMD_CANCEL);
        addCommand(CMD_SAKUZYO);
        setCommandListener(this);



    }

    private void init() {
        deleteAll();

        Enumeration enumeration = Bookmarks.getInstance().elements();

        if (enumeration != null) {

            for (; enumeration.hasMoreElements();) {
                Bookmark bookmark = (Bookmark) enumeration.nextElement();
                append(bookmark.getName(), null);

            }
        }

    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_SELECT) {
            
            
            new Thread(this).start();
        } else if (command == CMD_SAKUZYO) {
            Bookmarks.getInstance().removeElementAt(getSelectedIndex());
            try {
            RecordStoreUtil.saveBookmarks(Bookmarks.getInstance());
            }catch (Exception e) {
                e.printStackTrace();
                
            }
            init();
        } else if (command == CMD_CANCEL) {
            
            if (previousDisplayable instanceof AozoraCanvas) {
                ((AozoraCanvas) previousDisplayable).finalizeSettei(false);
            }
            AozoraMIDlet.getDisplay().setCurrent(previousDisplayable);
        }

    }

    public void run() {
        Bookmark bookmark = Bookmarks.getInstance().elementAt(
                getSelectedIndex());
        FileInfo fileInfo;
        try {
            try {
                setTicker(new Ticker("ファイル読み込み中"));

                fileInfo = FileUtil.getFileContent(bookmark.getPath(), bookmark
                        .getOffset(), bookmark.getOffsetStack());
                fileInfo.setLineNumber(bookmark.getLineNumber());
                fileInfo.setTategaki(bookmark.isTategaki());
                fileInfo.setPlainTextSize(bookmark.getPlainTextSize());
                fileInfo.setRubyTextSize(bookmark.getRubyTextSize());

                
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert("ファイルの読み込みに失敗しました", "ファイルの読み込みに失敗しました",
                        null, null);
                AozoraMIDlet.getDisplay().setCurrent(alert);

                return;
            }
            int index = fileInfo.getPath().lastIndexOf('/');
            String directoryPath = fileInfo.getPath().substring(0, index + 1);
            DirectoryList directoryList = DirectoryList.getInstance(directoryPath);
            AozoraCanvas canvas  ;            
            if(previousDisplayable instanceof AozoraCanvas) {
                canvas = (AozoraCanvas) previousDisplayable;
                canvas.setDirectoryList(directoryList);
                canvas.setFileInfo(fileInfo);
                AozoraMIDlet.getDisplay().setCurrent(canvas);
                
                
            }else {
                canvas = AozoraCanvas.getInstance(directoryList, fileInfo);
                AozoraMIDlet.getDisplay().setCurrent(canvas);
            
                
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            Alert alert = new Alert("OutOfMemoryError", "OutOfMemoryError", null, null);
            AozoraMIDlet.getDisplay().setCurrent(alert);
            return;
        }finally {
        
            setTicker(null);
        }
    }

}
