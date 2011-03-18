import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

public class BookmarksList extends List implements CommandListener, Runnable {
    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private final Command CMD_SELECT = new Command("選択", Command.ITEM, 1);

    private final Command CMD_SAKUZYO = new Command("削除", Command.SCREEN, 1);

    private final Command CMD_FILE = new Command("ファイルへ書き出し/から読み込み",
            Command.SCREEN, 5);

    private Displayable previousDisplayable;

    private static BookmarksList bookmarksList = new BookmarksList();

    public static BookmarksList getInstance(Displayable displayable) {
        if (displayable != null) {
            bookmarksList.previousDisplayable = displayable;
        }
        bookmarksList.init();
        return bookmarksList;
    }

    private BookmarksList() {
        super("ブックマークのリスト", IMPLICIT);
        setSelectCommand(CMD_SELECT);
        addCommand(CMD_SAKUZYO);
        addCommand(CMD_BACK);
        addCommand(CMD_FILE);

        setCommandListener(this);

    }

    private void init() {
        deleteAll();

        // Enumeration enumeration = Bookmarks.getInstance().elements();

        // if (enumeration != null) {

        // for (; enumeration.hasMoreElements();) {

        for (int i = 0; i < Bookmarks.getInstance().size(); ++i) {
            Bookmark bookmark = (Bookmark) Bookmarks.getInstance().elementAt(i);
            append(bookmark.getName(), null);

        }

    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_SELECT) {

            new Thread(this).start();
        } else if (command == CMD_SAKUZYO) {
            Bookmarks.getInstance().removeElementAt(getSelectedIndex());
            init();
        } else if (command == CMD_BACK) {

            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        } else if (command == CMD_FILE) {
            MapMIDlet.getDisplay().setCurrent(
                    BookmarksOperationForm.getInstance(null));
        }

    }

    public void run() {
        Bookmark bookmark = Bookmarks.getInstance().elementAt(
                getSelectedIndex());

        MapMIDlet.getDisplay().setCurrent(
                MapCanvas.getInstance(bookmark.getX(), bookmark.getY(),
                        bookmark.getZoom()));

    }

}
