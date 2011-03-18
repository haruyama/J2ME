import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreException;

public class SettingsForm extends Form implements CommandListener,
        ItemCommandListener {

    private ChoiceGroup isUseCacheDirChocieGroup;

    private ChoiceGroup isDisplayInfoChocieGroup;

    private ChoiceGroup isDisplayCenterChocieGroup;
    
    private ChoiceGroup isDisplayScaleChocieGroup;

    private TextField cacheDirField;

    private StringItem selectCacheDirButton;

    private TextField defaultZoomField;

    private TextField keepImageDistanceField;

    private final Command CMD_OK = new Command("設定反映", Command.OK, 1);
    
    private final Command CMD_CACHE_OPERATION = new Command("キャッシュの操作", Command.SCREEN, 5);

    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);

    private final static Command CMD_PRESS = new Command("ディレクトリ選択",
            Command.ITEM, 1);

    private Settings settings;

    private Displayable previousDisplayable;

    private static SettingsForm settingsForm = new SettingsForm();

    public static SettingsForm getInstance(Displayable displayable) {
        return getInstance(displayable, null);
    }

    public static SettingsForm getInstance(Displayable displayable,
            String cacheDir) {
        if (displayable != null) {
            settingsForm.previousDisplayable = displayable;
        }
        settingsForm.settings = Settings.getInstance();
        settingsForm.init(cacheDir);
        return settingsForm;
    }

    private void setSelectedFlags(ChoiceGroup choiceGroup, boolean isTrue) {

        boolean[] isSelected = new boolean[2];
        if (isTrue) {
            isSelected[1] = true;
        } else {
            isSelected[0] = true;
        }

        choiceGroup.setSelectedFlags(isSelected);

    }

    private void init(String cacheDir) {

        defaultZoomField.setString(Integer.toString(settings.getDefaultZoom()));

        setSelectedFlags(isUseCacheDirChocieGroup, settings.isUseCache());

        if (cacheDir != null) {
            cacheDirField.setString(cacheDir);
        } else {
            cacheDirField.setString(settings.getCacheDir());
        }

        keepImageDistanceField.setString(Integer.toString(settings
                .getKeepImageDistance()));

        setSelectedFlags(isDisplayInfoChocieGroup, settings.isDisplayInfo());

        setSelectedFlags(isDisplayCenterChocieGroup, settings.isDisplayCenter());
        
        setSelectedFlags(isDisplayScaleChocieGroup, settings.isDisplayScale());

    }

    private SettingsForm() {

        super("設定");

        isUseCacheDirChocieGroup = new ChoiceGroup("キャッシュ", ChoiceGroup.POPUP,
                new String[] { "無効", "有効" }, null);
        cacheDirField = new TextField("キャッシュ用ディレクトリ", null, 512, TextField.ANY);
        selectCacheDirButton = new StringItem("ディレクトリ選択 ",
                "ディレクトリに移動して「選択」してください", Item.BUTTON);
        selectCacheDirButton.setDefaultCommand(CMD_PRESS);
        selectCacheDirButton.setItemCommandListener(this);

        defaultZoomField = new TextField("検索結果のズームレベル", null, 2,
                TextField.NUMERIC);

        keepImageDistanceField = new TextField("これ以上離れたら画像を消去", null, 2,
                TextField.NUMERIC);

        isDisplayInfoChocieGroup = new ChoiceGroup("座標・ズームの表示",
                ChoiceGroup.POPUP, new String[] { "しない", "する" }, null);

        isDisplayCenterChocieGroup = new ChoiceGroup("中心に十字を表示",
                ChoiceGroup.POPUP, new String[] { "しない", "する" }, null);
        
        isDisplayScaleChocieGroup = new ChoiceGroup("縮尺を表示",
                ChoiceGroup.POPUP, new String[] { "しない", "する" }, null);


        append(defaultZoomField);
        append("ズームレベルは0で最大16で最小");
        append(isUseCacheDirChocieGroup);
        append(cacheDirField);
        append(selectCacheDirButton);
        append(keepImageDistanceField);
        append("画像が現在位置よりもこの値以上離れたらヒープに保持しないようにします。WX310SAでは2が適当です。");
        append(isDisplayInfoChocieGroup);

        append(isDisplayCenterChocieGroup);
        append(isDisplayScaleChocieGroup);

        addCommand(CMD_OK);
        addCommand(CMD_CANCEL);
        addCommand(CMD_CACHE_OPERATION);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_OK) {

            try {

                if (isUseCacheDirChocieGroup.getSelectedIndex() == 0) {
                    settings.setUseCache(false);
                } else {
                    settings.setUseCache(true);
                }

                settings.setCacheDir(cacheDirField.getString());

                settings.setDefaultZoom(Integer.parseInt(defaultZoomField
                        .getString()));

                settings.setKeepImageDistance(Integer
                        .parseInt(keepImageDistanceField.getString()));

                if (isDisplayInfoChocieGroup.getSelectedIndex() == 0) {
                    settings.setDisplayInfo(false);
                } else {
                    settings.setDisplayInfo(true);
                }

                if (isDisplayCenterChocieGroup.getSelectedIndex() == 0) {
                    settings.setDisplayCenter(false);
                } else {
                    settings.setDisplayCenter(true);
                }
                if (isDisplayScaleChocieGroup.getSelectedIndex() == 0) {
                    settings.setDisplayScale(false);
                } else {
                    settings.setDisplayScale(true);
                }
                RecordStoreUtil.saveSettings(settings);

            } catch (NumberFormatException e) {
                Alert alert = new Alert("数字が正しく入力されていません", "数字が正しく入力されていません",
                        null, null);
                MapMIDlet.getDisplay().setCurrent(alert);

            } catch (RecordStoreException e) {
                Alert alert = new Alert("設定の保存に失敗しました", "設定の保存に失敗しました", null,
                        null);
                MapMIDlet.getDisplay().setCurrent(alert);

            }
            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        } else if (command == CMD_CANCEL) {
            MapMIDlet.getDisplay().setCurrent(previousDisplayable);
        } else if (command == CMD_CACHE_OPERATION) {
            MapMIDlet.getDisplay().setCurrent(CacheOperationForm.getInstance(this));
        }


    }

    public void commandAction(Command command, Item item) {

        new Thread()
        {
            public void run() {

                MapMIDlet.getDisplay().setCurrent(
                        FileList.getInstance(settingsForm, cacheDirField
                                .getString()));
            }
        }.start();

    }
}
