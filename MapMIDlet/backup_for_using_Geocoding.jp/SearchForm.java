import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

public class SearchForm extends Form implements CommandListener {

    private final Command CMD_OK = new Command("OK", Command.OK, 1);

    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);

    private TextField searchField;

    private StringItem infoItem;

    private Displayable previousDisplayable;

    private static SearchForm searchForm = new SearchForm();

    public static SearchForm getInstance(Displayable displayable) {
        searchForm.previousDisplayable = displayable;
        searchForm.init();
        return searchForm;

    }

    private SearchForm() {
        super("検索");
        searchField = new TextField("住所、ランドマークなどを入力してください", "", 256,
                TextField.ANY);

        infoItem = new StringItem("", "");

        append(searchField);
        append(infoItem);
        addCommand(CMD_OK);
        addCommand(CMD_CANCEL);
        setCommandListener(this);
    }

    private void init() {
        searchField.setString("");
        infoItem.setText("");
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_OK) {
            new Thread()
            {
                public void run() {
                    setTicker(new Ticker("検索中"));
                    GeocoodingInfo info = GeocoodingInfo
                            .getGeocordingInfo(searchField.getString());
                    switch (info.getStatus())
                        {
                        case GeocoodingInfo.STATUS_SUCCESS:
                            int zoomLevel = Settings.getInstance()
                                    .getDefaultZoom();

                            MapMIDlet.getDisplay().setCurrent(
                                    MapCanvas.getInstance(
                                            info.getMapX() >> zoomLevel, info
                                                    .getMapY() >> zoomLevel,
                                            zoomLevel));

                            break;
                        case GeocoodingInfo.STATUS_MULTI:
                            MapMIDlet.getDisplay().setCurrent(
                                    SelectList.getInstance(searchForm, info
                                            .getChoices()));

                            // infoItem.setText("複数の選択肢があります。そのうち選択できるようにします。: "
                            // + info.getChoices().toString());

                            break;
                        case GeocoodingInfo.STATUS_FAILURE:
                            infoItem.setText("該当する地名などはありません");
                            break;

                        }
                    setTicker(null);
                }
            }.start();
        } else if (command == CMD_CANCEL) {
            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        }

    }
}
