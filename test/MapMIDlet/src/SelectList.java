import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

public class SelectList extends List implements CommandListener {
    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private final Command CMD_SELECT = new Command("選択", Command.ITEM, 1);

    private static SelectList selectList = new SelectList();

    private Displayable previousDisplayable;

    private Vector vector;

    public static SelectList getInstance(Displayable displayable, Vector vector) {
        selectList.previousDisplayable = displayable;
        if (vector != null) {
            selectList.vector = vector;

        }
        selectList.init();
        return selectList;
    }

    private void init() {
        deleteAll();
        if (vector != null) {

            for (int i = 0; i < vector.size(); ++i) {
                GeocoodingInfo info = (GeocoodingInfo) vector.elementAt(i);
                append(info.getAddress(), null);

            }
        }

    }

    private SelectList() {
        super("検索結果からの選択", IMPLICIT);
        setSelectCommand(CMD_SELECT);
        addCommand(CMD_BACK);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_SELECT) {
            new Thread()
            {
                public void run() {
                    GeocoodingInfo info = (GeocoodingInfo) vector
                            .elementAt(getSelectedIndex());

                    int zoomLevel = Settings.getInstance().getDefaultZoom();

                    MapMIDlet.getDisplay().setCurrent(
                            MapCanvas.getInstance(info.getMapX() >> zoomLevel,
                                    info.getMapY() >> zoomLevel, zoomLevel));
                }

            }.start();
        } else if (command == CMD_BACK) {

            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        }
    }
}