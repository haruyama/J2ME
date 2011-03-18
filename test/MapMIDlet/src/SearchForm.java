import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

public class SearchForm extends Form implements CommandListener {

    private final Command CMD_OK = new Command("検索", Command.OK, 1);

    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private static final String[] MOTO_DATA = { "地名", "駅名", "住所" };

    private TextField searchField;

    private StringItem infoItem;

    private ChoiceGroup motoDataChoiceGroup;

    private Displayable previousDisplayable;

    private static SearchForm searchForm = new SearchForm();

    public static SearchForm getInstance(Displayable displayable) {
        searchForm.previousDisplayable = displayable;
        searchForm.init();
        return searchForm;

    }

    private SearchForm() {
        super("検索");

        searchField = new TextField("地名などを入力してください", "", 256, TextField.ANY);

        infoItem = new StringItem("", "");

        motoDataChoiceGroup = new ChoiceGroup("検索対象", ChoiceGroup.EXCLUSIVE,
                MOTO_DATA, null);

        append(searchField);

        append(motoDataChoiceGroup);
        append(infoItem);
        append("\n市,区をつけたり都道府県から指定するとよい結果が得られます。"
                + "CSISシンプルジオコーディング実験[(街区レベル位置参照情報, 国土数値情報 鉄道, 数値地図25000(地名・公共施設)2001年版)を利用しています");

        addCommand(CMD_OK);
        addCommand(CMD_BACK);
        setCommandListener(this);
    }

    private void init() {
        searchField.setString("");
        infoItem.setText("");
        motoDataChoiceGroup.setSelectedIndex(0, true);

    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_OK) {
            new Thread()
            {
                public void run() {
                    setTicker(new Ticker("検索中"));
                    Vector vector = GeocoodingInfo.getGeocordingInfo(
                            searchField.getString(), motoDataChoiceGroup
                                    .getSelectedIndex());
                    if (vector.isEmpty()) {

                        infoItem.setText("該当する地名などはありません");
                        // new Thread() {
                        // public void run() {
                        // setTicker(new Ticker("該当する地名などはありません。"));
                        // try {
                        // Thread.sleep(5000);
                        // }catch (Exception e) {
                        // }
                        // setTicker(null);
                        // }
                        // }.start();
                        //                        
                    } else {
                        MapMIDlet.getDisplay().setCurrent(
                                SelectList.getInstance(searchForm, vector));

                    }

                    setTicker(null);
                }
            }.start();
        } else if (command == CMD_BACK) {
            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        }

    }
}
