import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.file.FileSystemRegistry;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

import javax.microedition.lcdui.Displayable;

import javax.microedition.lcdui.List;

public class FileList extends List implements CommandListener, Runnable {
    /**
     * 設定画面を呼び出すコマンド
     */
    private final Command CMD_IDOU = new Command("移動", Command.ITEM, 5);

    private final Command CMD_SELECT = new Command("選択", Command.SCREEN, 1);

    private final Command CMD_MKDIR = new Command("ディレクトリ作成", Command.SCREEN, 3);

    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);

    // private final Command CMD_MKDIR = new Command("ディレクトリ作成", Command.ITEM,
    // 1);

    // private final Command CMD_DELETE = new Command("削除", Command.ITEM, 1);

    private volatile String directoryName;

    private Vector isDirectory;

    private static final String PARENT_DIRECTORY_NAME = "..";

    private static final String TOP_DIRECTORY_NAME = "file:///";

    private Displayable previousDisplayable;

    private static FileList fileList = new FileList();

    public static FileList getInstance(Displayable displayable,
            String directoryName) {
        if (displayable != null) {
            fileList.previousDisplayable = displayable;
        }
        fileList.init(directoryName);
        return fileList;

    }

    private FileList() {

        super("", IMPLICIT);
        isDirectory = new Vector();
        setSelectCommand(CMD_IDOU);
        addCommand(CMD_SELECT);
        addCommand(CMD_MKDIR);
        addCommand(CMD_CANCEL);

        setCommandListener(this);
    }

    private void init(String directoryName) {
        Enumeration fileEnumeration = null;
        this.directoryName = directoryName;
        setTitle(directoryName);
        deleteAll();
        isDirectory.removeAllElements();

        if (directoryName.equals(TOP_DIRECTORY_NAME)) {
            fileEnumeration = FileSystemRegistry.listRoots();
        } else {

            try {
                fileEnumeration = FileUtil.getFileNames(directoryName);
                append(PARENT_DIRECTORY_NAME, null);
                isDirectory.addElement(Boolean.TRUE);

            } catch (Exception e) {

                init(TOP_DIRECTORY_NAME);
                return;
            }
        }
        if (fileEnumeration != null) {

            for (; fileEnumeration.hasMoreElements();) {

                String filename = (String) fileEnumeration.nextElement();
                if (filename.endsWith("/")) {
                    append(filename, null);
                    isDirectory.addElement(Boolean.TRUE);
                } else {
                    append(filename, null);
                    isDirectory.addElement(Boolean.FALSE);
                }

            }

        }

    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_IDOU) {
            new Thread(this).start();
        } else if (command == CMD_SELECT) {
            if (previousDisplayable instanceof SettingsForm) {
                MapMIDlet.getDisplay().setCurrent(
                        SettingsForm.getInstance(null, directoryName));
            } else if (previousDisplayable instanceof BookmarksOperationForm) {
                if (!directoryName.endsWith("/")) {
                    directoryName += "/";
                }

                MapMIDlet.getDisplay().setCurrent(
                        BookmarksOperationForm.getInstance(directoryName));

            }

        } else if (command == CMD_CANCEL) {
            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        } else if (command == CMD_MKDIR) {
            MapMIDlet.getDisplay().setCurrent(
                    MkdirForm.getInstance(directoryName, this));
        }

    }

    public void run() {
        synchronized (this) {
            try {
                if (((Boolean) isDirectory.elementAt(getSelectedIndex()))
                        .booleanValue()) {
                    String childName = getString(getSelectedIndex());

                    if (childName.equals(PARENT_DIRECTORY_NAME)) {
                        int index = directoryName.lastIndexOf('/',
                                directoryName.length() - 2);
                        directoryName = directoryName.substring(0, index + 1);

                    } else {
                        directoryName = directoryName + childName;
                    }
                    init(directoryName);
                    return;
                }
                /*
                 * Last-modifiedはWX310SAで取れる
                 * 
                 * else { String childName = getString(getSelectedIndex()); long
                 * lastModified = FileUtil.getLastModified(directoryName +
                 * childName );
                 * 
                 * Alert alert = new Alert("Last-Modified", "" + lastModified,
                 * null,null); alert.setTimeout(Alert.FOREVER);
                 * MapMIDlet.getDisplay().setCurrent(alert); }
                 */

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
