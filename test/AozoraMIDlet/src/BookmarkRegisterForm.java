import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;


public class BookmarkRegisterForm extends Form implements CommandListener{

    private static BookmarkRegisterForm  bookmarkRegisterForm = new BookmarkRegisterForm();
    
    private Bookmark bookmark;
    private final Command CMD_OK = new Command("OK", Command.OK, 1);

    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);
    
    private TextField nameField;
    
    private Displayable previousDisplayable;

    public static BookmarkRegisterForm getInstance(Bookmark bookmark, Displayable displayable) {
        bookmarkRegisterForm.bookmark = bookmark;
        bookmarkRegisterForm.previousDisplayable = displayable; 
        bookmarkRegisterForm.init();
        return bookmarkRegisterForm;
    }
    
    private void init() {
        nameField.setString(bookmark.getName());
        
    }
    
    private BookmarkRegisterForm() {
        super("しおりへの登録");
        
        nameField = new TextField("ブックマークの名前", null,  200, TextField.ANY);
        append(nameField);
        
        addCommand(CMD_OK);
        addCommand(CMD_CANCEL);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_OK) {
            bookmark.setName(nameField.getString());
            Bookmarks.getInstance().addElement(bookmark);
            
        }
        if (previousDisplayable instanceof AozoraCanvas) {
            ((AozoraCanvas) previousDisplayable).finalizeSettei(false);
        }
        AozoraMIDlet.getDisplay().setCurrent(previousDisplayable);

    }

}
