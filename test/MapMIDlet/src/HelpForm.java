import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

public class HelpForm extends Form implements CommandListener {
    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private Displayable previousDisplayable;

    private static HelpForm helpForm = new HelpForm();

    public static HelpForm getInstance(Displayable displayable) {
        helpForm.previousDisplayable = displayable;
        return helpForm;
    }

    private HelpForm() {
        super("ショートカットキー一覧");

        if (System.getProperty("microedition.platform").equals("WX310SA")) {
            append("カメラ/1キー: " + "ズームダウン");
        } else {
            append("1キー: " + "ズームダウン");
        }

        append("2キー: " + "中心に十字を表示/非表示");

        if (System.getProperty("microedition.platform").equals("WX310SA")) {
            append("My/3キー: " + "ズームアップ");
        } else {
            append("3キー: " + "ズームアップ");
        }
        append("4キー: " + "検索");
        append("5キー: " + "地図の座標を表示/非表示");
        append("6キー: " + "ズーム・座標の直接設定");
        append("7キー: " + "ブックマークに登録");

        append("8キー: " + "ブックマーク一覧");
        append("9キー: " + "設定");
        append("*キー: " + "地図の再描画");
        append("0キー: " + "地図の縮尺を表示/非表示");
        append("#キー: " + "ショートカットキー一覧");
        if (System.getProperty("microedition.platform").equals("WX310SA")) {
            append("CLRキー: " + "検索結果の再参照");
        }
        addCommand(CMD_BACK);
        setCommandListener(this);

    }

    public void commandAction(Command arg0, Displayable arg1) {
        MapMIDlet.getDisplay().setCurrent(previousDisplayable);

    }

}
