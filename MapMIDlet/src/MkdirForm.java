import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

public class MkdirForm extends Form implements CommandListener {

    private static MkdirForm mkdirForm = new MkdirForm();

    private final Command CMD_OK = new Command("作成", Command.OK, 1);

    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private TextField nameField;

    private String parentDirectory;

    private Displayable previousDisplayable;

    public static MkdirForm getInstance(String parentDirectory,
            Displayable displayable) {

        if (!parentDirectory.endsWith("/")) {
            parentDirectory += "/";
        }
        mkdirForm.parentDirectory = parentDirectory;

        mkdirForm.previousDisplayable = displayable;
        mkdirForm.init();
        return mkdirForm;
    }

    private void init() {
        nameField.setString("");

    }

    private MkdirForm() {
        super("ディレクトリの作成");

        nameField = new TextField("作成するディレクトリの名前", null, 100, TextField.ANY);
        append(nameField);

        addCommand(CMD_OK);
        addCommand(CMD_BACK);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_OK) {

            new Thread()
            {

                public void run() {
                    try {
                        FileUtil.mkdir(parentDirectory + nameField.getString());
                        MapMIDlet.getDisplay().setCurrent(
                                FileList.getInstance(null, parentDirectory));
                    } catch (IOException e) {
                        Alert alert = new Alert("ディレクトリの作成に失敗しました",
                                "ディレクトリの作成に失敗しました", null, null);
                        MapMIDlet.getDisplay().setCurrent(alert);
                    }
                }
            }.start();

        }

        MapMIDlet.getDisplay().setCurrent(previousDisplayable);

    }
}
