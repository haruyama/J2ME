import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreException;

public class BookmarksOperationForm extends Form implements CommandListener,
        ItemCommandListener {

    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private final Command CMD_EXPORT = new Command("書き出し", Command.ITEM, 1);

    private final Command CMD_IMPORT = new Command("読み込み", Command.ITEM, 1);

    private final Command CMD_ADD = new Command("追加", Command.ITEM, 1);

    private TextField bookmarkFilenameField;

    private StringItem exportButton;

    private StringItem importButton;

    private StringItem addButton;

    private StringItem selectDirButton;
    private static final String DEFAULT_BOOKMARKS_FILENAME = "maps_bookmarks.txt";
    protected boolean doAdd;

    private final static Command CMD_PRESS = new Command("ディレクトリ選択",
            Command.ITEM, 1);

    private static BookmarksOperationForm bookmarksOperationForm = new BookmarksOperationForm();

    public static BookmarksOperationForm getInstance(String filename) {
        // if (displayable != null) {
        // bookmarksOperationForm.previousDisplayable = displayable;
        // }

        if (filename != null) {
            bookmarksOperationForm.bookmarkFilenameField.setString(filename);
        } else {
            bookmarksOperationForm.bookmarkFilenameField.setString(Settings
                    .getInstance().getBookmarkFilename());
        }

        return bookmarksOperationForm;
    }

    private BookmarksOperationForm() {
        super("ブックマーク<->ファイル");
        bookmarkFilenameField = new TextField("ブックマークのファイル名(ディレクトリが指定された場合は"
                + "maps_bookmarks.txtファイルを利用)", null, 512, TextField.ANY);

        selectDirButton = new StringItem("ディレクトリ選択 ", "ディレクトリに移動して「選択」してください",
                Item.BUTTON);
        selectDirButton.setDefaultCommand(CMD_PRESS);
        selectDirButton.setItemCommandListener(this);
        exportButton = new StringItem("ファイルに書き出し", "ファイルに書き出し ", Item.BUTTON);
        exportButton.setDefaultCommand(CMD_EXPORT);
        exportButton.setItemCommandListener(this);

        importButton = new StringItem("ファイルから読み込み", "ファイルから読み込み", Item.BUTTON);
        importButton.setDefaultCommand(CMD_IMPORT);
        importButton.setItemCommandListener(this);

        addButton = new StringItem("ファイルから追加", "ファイルから追加", Item.BUTTON);
        addButton.setDefaultCommand(CMD_ADD);
        addButton.setItemCommandListener(this);
        append(bookmarkFilenameField);
        append(selectDirButton);
        append(exportButton);
        append(importButton);
        append(addButton);

        addCommand(CMD_BACK);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_BACK) {
            // MapMIDlet.getDisplay().setCurrent(previousDisplayable);
            MapMIDlet.getDisplay().setCurrent(BookmarksList.getInstance(null));
        }

    }

    private void importBookmarks() {
        new Thread()
        {
            public void run() {
                String filename = bookmarkFilenameField.getString();
                if (filename.endsWith("/")) {
                    filename += DEFAULT_BOOKMARKS_FILENAME;
                }

                String bookmarksString = null;
                try {
                    bookmarksString = new String(FileUtil
                            .getFileContent(filename), "Shift_JIS");
                } catch (Exception e) {
                    Alert alert = new Alert(e.toString(), e.getMessage()
                            + ": ファイルの読み込みに失敗しました", null, null);
                    MapMIDlet.getDisplay().setCurrent(alert);
                    return;
                }

                if (bookmarksString == null) {
                    Alert alert = new Alert("ファイルの読み込みに失敗しました",
                            "ファイルの読み込みに失敗しました", null, null);
                    MapMIDlet.getDisplay().setCurrent(alert);
                    return;
                }

                if (!doAdd) {
                    Bookmarks.getInstance().removeAllElement();
                }
                int indexBegin = 0;
                int indexEnd;
                do {
                    indexEnd = bookmarksString.indexOf('\n', indexBegin);

                    if (indexEnd == -1) {
                        break;
                    }

                    Bookmarks.getInstance().addElement(
                            Bookmark.fromString(bookmarksString.substring(
                                    indexBegin, indexEnd)));
                    indexBegin = indexEnd + 1;
                } while (true);
                Settings.getInstance().setBookmarkFilename(filename);
                try {
                    RecordStoreUtil.saveSettings(Settings.getInstance());
                } catch (RecordStoreException e) {

                }
                Alert alert = new Alert("ファイルから読み込みました", "ファイルから読み込みました", null,
                        null);
                MapMIDlet.getDisplay().setCurrent(alert);

            }

        }.start();

    }

    
    public void commandAction(Command command, Item item) {
        if (command == CMD_EXPORT) {
            exportBookmarks();

        } else if (command == CMD_IMPORT) {
            doAdd = false;
            importBookmarks();
        } else if (command == CMD_ADD) {
            doAdd = true;
            importBookmarks();

        } else if (command == CMD_PRESS) {

            new Thread()
            {
                public void run() {
                    String filename = bookmarkFilenameField.getString();
                    if (!filename.endsWith("/")) {
                        filename = filename.substring(0, filename
                                .lastIndexOf('/'));

                    }
                    MapMIDlet.getDisplay().setCurrent(
                            FileList.getInstance(bookmarksOperationForm,
                                    filename));
                }
            }.start();
        }
    }

    /**
     * 
     */
    private void exportBookmarks() {
        new Thread()
        {
            public void run() {
                String filename = bookmarkFilenameField.getString();
                if (filename.endsWith("/")) {
                    filename += DEFAULT_BOOKMARKS_FILENAME;
                }

                // Enumeration enumeration = Bookmarks.getInstance()
                // .elements();

                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < Bookmarks.getInstance().size(); ++i) {
                    // while (enumeration.hasMoreElements()) {
                    Bookmark bookmark = (Bookmark) Bookmarks.getInstance()
                            .elementAt(i);
                    buffer.append(bookmark.toString());
                    buffer.append('\n');
                }
                // どうも実機では更新されないことがあったので
                try {
                    FileUtil.delete(filename);
                } catch (Exception e) {
                }

                try {
                    FileUtil.saveFileContent(filename, buffer.toString()
                            .getBytes("Shift_JIS"));
                } catch (Exception e) {
                    Alert alert = new Alert(e.toString(), e.getMessage()
                            + ": ファイルの保存に失敗しました", null, null);
                    MapMIDlet.getDisplay().setCurrent(alert);
                    return;

                }
                Settings.getInstance().setBookmarkFilename(filename);
                try {
                    RecordStoreUtil.saveSettings(Settings.getInstance());
                } catch (RecordStoreException e) {

                }

                Alert alert = new Alert("ファイルへ書き込みました", "ファイルへ書き込みました",
                        null, null);
                MapMIDlet.getDisplay().setCurrent(alert);
            }

        }.start();
    }
}
