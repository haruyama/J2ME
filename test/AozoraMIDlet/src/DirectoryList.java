import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Ticker;

import javax.microedition.lcdui.List;

public class DirectoryList extends List implements CommandListener, Runnable {
    /**
     * 設定画面を呼び出すコマンド
     */
    private final Command CMD_SETTEI = new Command("設定", Command.SCREEN, 10);

    private final Command CMD_SELECT = new Command("Open", Command.ITEM, 1);

    private final Command CMD_SIORI_LIST = new Command("しおりのリスト",
            Command.SCREEN, 10);

    private volatile String directoryName;

    private Vector isDirectory;

    private static final String PARENT_DIRECTORY_NAME = "..";

    private static final String TOP_DIRECTORY_NAME = "file:///";

    
    private static DirectoryList directoryList = new DirectoryList();
    
    public static DirectoryList getInstance(String directoryName) {

        directoryList.init(directoryName);
        return directoryList;
        
    }

    private DirectoryList() {

        super("", IMPLICIT);
        isDirectory = new Vector();
        setSelectCommand(CMD_SELECT);
        addCommand(CMD_SETTEI);
        addCommand(CMD_SIORI_LIST);
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

                if (filename.toLowerCase().endsWith(".txt")) {
                    append(filename, null);
                    isDirectory.addElement(Boolean.FALSE);
                } else if (filename.endsWith("/")) {
                    append(filename, null);
                    isDirectory.addElement(Boolean.TRUE);
                }

            }

        }

    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_SELECT) {
            new Thread(this).start();
        } else if (command == CMD_SETTEI) {
            AozoraMIDlet.getDisplay().setCurrent(
                    SettingsForm.getInstance(this));
        } else if (command == CMD_SIORI_LIST) {
            AozoraMIDlet.getDisplay().setCurrent(BookmarksList.getInstance(this));

        }

    }

    public void run() {
        synchronized (this) {
            try {
                FileInfo fileInfo = null;
                try {
                    if (((Boolean) isDirectory.elementAt(getSelectedIndex()))
                            .booleanValue()) {
                        String childName = getString(getSelectedIndex());
                        
                        if (childName.equals(PARENT_DIRECTORY_NAME)) {
                            int index = directoryName.lastIndexOf('/',
                                    directoryName.length() - 2);
                            directoryName = directoryName.substring(0,
                                    index + 1);

                        } else {
/*                            
                            if(System.getProperty("microedition.platform").equals("WX310SA")) {
                                byte b[] = childName.getBytes("Shift_JIS");
                                Vector vector = new Vector();
                                for(int i = 0; i < b.length; ++i) {
                                    if(b[i] == 0x5c) {
                                        vector.addElement(new Byte((byte)0x5c));
                                    }
                                    vector.addElement(new Byte(b[i]));
                                }
                                
                                byte[] newb = new byte[vector.size()];
                                
                                for(int i = 0; i< newb.length; ++i) {
                                    newb[i] = ((Byte)vector.elementAt(i)).byteValue();
                                }
                                
                                childName  = new String(newb, "Shift_JIS");
                                
                            }
                            */
                            directoryName = directoryName + childName;
                        }
                        init(directoryName);
                        return;
                    } else {
                        setTicker(new Ticker("ファイル読み込み中"));
//                        System.gc();
                        fileInfo = FileUtil.getFileContent(directoryName
                                + getString(getSelectedIndex()), 0, null);

                    }

                } catch (OutOfMemoryError e) {
                    Alert alert = new Alert("ファイルが大きすぎます1", "ファイルが大きすぎます1" + e,
                            null, null);
                    AozoraCanvas.getInstance(this, null);
                    AozoraMIDlet.getDisplay().setCurrent(alert);
                    return;
                } finally {
                    setTicker(null);

                }

                if (fileInfo == null) {
                    Alert alert = new Alert("ファイルの読み込みに失敗しました",
                            "ファイルの読み込みに失敗しました", null, null);
                    AozoraMIDlet.getDisplay().setCurrent(alert);

                    return;
                }

                AozoraCanvas canvas = null;
                try {
                    canvas = AozoraCanvas.getInstance(this, fileInfo);
                    AozoraMIDlet.getDisplay().setCurrent(canvas);
                } catch (OutOfMemoryError e) {
                    Alert alert = new Alert("ファイルが大きすぎます2", "ファイルが大きすぎます2", null,
                            null);
                    AozoraMIDlet.getDisplay().setCurrent(alert);
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
