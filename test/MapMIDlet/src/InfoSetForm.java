import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

public class InfoSetForm extends Form implements CommandListener {
    private final Command CMD_OK = new Command("選択", Command.OK, 1);

    private final Command CMD_BACK = new Command("戻る", Command.BACK, 1);

    private Gauge zoomGauge;

    private TextField xField;

    private TextField yField;

    private StringItem zoomItem;

    private int x;

    private int y;

    private int zoom;

    private Displayable previousDisplayable;

    private static InfoSetForm infoSetForm = new InfoSetForm();

    public static InfoSetForm getInstance(int x, int y, int zoom,
            Displayable displayable) {
        infoSetForm.x = x;
        infoSetForm.y = y;
        infoSetForm.zoom = zoom;
        infoSetForm.previousDisplayable = displayable;

        infoSetForm.zoomGauge.setValue(zoom);
        infoSetForm.setFields();
        return infoSetForm;
    }

    private void setFields() {
        infoSetForm.zoomItem.setText(Integer.toString(zoom));
        infoSetForm.xField.setString(Integer.toString(x));
        infoSetForm.yField.setString(Integer.toString(y));

    }

    private InfoSetForm() {
        super("ズーム・座標の設定");
        zoomGauge = new Gauge("ズーム", true, 16, 0);
        zoomGauge.setLayout(Item.LAYOUT_EXPAND);

        zoomItem = new StringItem("zoom(0,最大...16最小)=", "");
        xField = new TextField("x=", "", 6, TextField.NUMERIC);
        yField = new TextField("y=", "", 6, TextField.NUMERIC);

        append(zoomGauge);
        append(zoomItem);
        append(xField);
        append(yField);

        addCommand(CMD_OK);
        addCommand(CMD_BACK);
        setItemStateListener(new MyItemStateListner());
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {

        if (command == CMD_OK) {

            MapMIDlet.getDisplay()
                    .setCurrent(MapCanvas.getInstance(x, y, zoom));
        } else if (command == CMD_BACK) {
            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        }

    }

    // Displayable.itemStateChangedがprivateなため警告が出てうざいので
    // 内部クラスにした
    private class MyItemStateListner implements ItemStateListener {

        public void itemStateChanged(Item item) {

            x = Integer.parseInt(xField.getString());
            y = Integer.parseInt(yField.getString());

            int currentZoom = zoomGauge.getValue();
            if (currentZoom != zoom) {
                if (currentZoom > zoom) {
                    x >>= (currentZoom - zoom);
                    y >>= (currentZoom - zoom);
                } else {
                    x <<= (zoom - currentZoom);
                    y <<= (zoom - currentZoom);

                }

                zoom = currentZoom;
                setFields();
            }

        }
    }

}
