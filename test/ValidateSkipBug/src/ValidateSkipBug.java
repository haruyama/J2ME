import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class ValidateSkipBug extends MIDlet {

    private static final String BUNSYO_FILEPATH = "file:///SD:/PC_INOUT/chuubounikki.txt";
//    private static final String BUNSYO_FILEPATH = "file:///PS:/$OTHER/chuubounikki.txt";

    public ValidateSkipBug() {
        Form form = new Form("Validate skip bug: " + BUNSYO_FILEPATH);

        try {
            for(int i = 0; i< 5; i++) {
            
                byte [] buffer = FileUtil.getFileContent(BUNSYO_FILEPATH, i*8192);
                Item item = new StringItem("offset: " + i*8192, new String(buffer, 0, 10, "Shift_JIS"));
                    form.append(item);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        
        Display.getDisplay(this).setCurrent(form);

    }

    protected void startApp() throws MIDletStateChangeException {
        // TODO Auto-generated method stub

    }

    protected void pauseApp() {
        // TODO Auto-generated method stub

    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // TODO Auto-generated method stub

    }

}
