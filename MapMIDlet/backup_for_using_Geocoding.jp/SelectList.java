import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

public class SelectList extends List implements CommandListener {
    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);

    private final Command CMD_SELECT = new Command("選択", Command.ITEM, 1);

    private static SelectList selectList = new SelectList();

    private Displayable previousDisplayable;

    private Vector vector;

    public static SelectList getInstance(Displayable displayable, Vector vector) {
        selectList.previousDisplayable = displayable;
        selectList.vector = vector;
        selectList.init();
        return selectList;
    }

    private void init() {
        deleteAll();

        Enumeration enumeration = vector.elements();

        if (enumeration != null) {

            for (; enumeration.hasMoreElements();) {
                append((String) enumeration.nextElement(), null);
            }
        }

    }

    private SelectList() {
        super("複数候補からの選択", IMPLICIT);
        setSelectCommand(CMD_SELECT);
        addCommand(CMD_CANCEL);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_SELECT) {
            new Thread()
            {
                public void run() {
                    setTicker(new Ticker("検索中"));
                    GeocoodingInfo info = GeocoodingInfo
                            .getGeocordingInfo(getString(getSelectedIndex()));
                    switch (info.getStatus())
                        {
                        case GeocoodingInfo.STATUS_SUCCESS:
                            MapMIDlet.getDisplay().setCurrent(
                                    MapCanvas.getInstance(info.getMapX() / 8,
                                            info.getMapY() / 8, 3));

                            break;
                        default:
                            Alert alert = new Alert("検索に失敗しました", "検索に失敗しました",
                                    null, null);

                            MapMIDlet.getDisplay().setCurrent(alert);

                        }
                    setTicker(null);
                }
            }.start();
        } else if (command == CMD_CANCEL) {

            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        }
    }

}