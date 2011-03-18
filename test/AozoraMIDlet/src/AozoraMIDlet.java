import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class AozoraMIDlet extends MIDlet {

    private DirectoryList directoryList;

    /**
     * WX310SAでの最初のディレクトリ
     * 
     * 最後に '/' をつけること
     */
    public static final String WX310SA_BUNSYO_DIR = "file:///SD:/PC_INOUT/";

    private static AozoraMIDlet midlet;

    public static Display getDisplay() {
        return Display.getDisplay(midlet);
    }
    
    private static void setMIDlet(AozoraMIDlet m) {
        midlet = m;
    }

    public AozoraMIDlet() {
        super();
        setMIDlet(this);
    }

    protected void startApp() throws MIDletStateChangeException {
        
        try {
            RecordStoreUtil.loadBookmarks();
            
        }catch (Exception e) {
            
        }
        
        FileInfo fileInfo = null;

        try {
            fileInfo = RecordStoreUtil.loadCurrentFileInfo();
        } catch (Exception e) {
        }

        if (fileInfo != null) {
            int index = fileInfo.getPath().lastIndexOf('/');

            String directoryPath = fileInfo.getPath().substring(0, index + 1);
            directoryList = DirectoryList.getInstance(directoryPath);
            AozoraCanvas canvas = AozoraCanvas.getInstance(directoryList, fileInfo);
            getDisplay().setCurrent(canvas);

        } else {
            directoryList = DirectoryList.getInstance(WX310SA_BUNSYO_DIR);
            getDisplay().setCurrent(directoryList);
        }

    }

    protected void pauseApp() {

    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {

    }

}
