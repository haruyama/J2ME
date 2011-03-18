import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;

import javax.microedition.lcdui.TextField;

public class SettingsForm extends Form implements CommandListener {

    private ChoiceGroup rubyFontChoiceGroup;

    private ChoiceGroup plainFontChoiceGroup;

    private String[] rubyFontSizeArray = { "SMALL", "MEDIUM", "LARGE", "なし" };

    private String[] fontSizeArray = { "SMALL", "MEDIUM", "LARGE" };

    private String[] yokotateSizeArray = { "横書き", "縦書き", };

    private Displayable previousDisplayable;

    private TextField stepField;

    private TextField fastStepField;

    private TextField textColorRedField;

    private TextField textColorGreenField;

    private TextField textColorBlueField;

    private TextField backgroundColorRedField;

    private TextField backgroundColorGreenField;

    private TextField backgroundColorBlueField;

    private ChoiceGroup isTategakiChoiceGroup;

    private TextField widthBetweenPlainsField;

    private TextField widthBetweenPlainAndRubyField;

    private TextField widthBetweenRubyAndPlainField;

    // private int prevPlainFontSize;

    // private int prevRubyFontSize;

    private final Command CMD_OK = new Command("OK", Command.OK, 1);

    private final Command CMD_CANCEL = new Command("CANCEL", Command.CANCEL, 1);

    private Display display;

    private Settings settings;

    private static SettingsForm settingsForm = new SettingsForm();

    public static SettingsForm getInstance(Displayable displayable) {
        settingsForm.previousDisplayable = displayable;
        settingsForm.settings = Settings.getInstance();
        settingsForm.display = AozoraMIDlet.getDisplay();
        settingsForm.init();
        return settingsForm;
    }

    private void setRubyFontChoiceGroup(ChoiceGroup choiceGroup, int selected) {
        boolean[] isSelected = new boolean[4];

        if (selected == Font.SIZE_SMALL) {
            isSelected[0] = true;
        } else if (selected == Font.SIZE_MEDIUM) {
            isSelected[1] = true;
        } else if (selected == Font.SIZE_LARGE) {
            isSelected[2] = true;
        } else {
            isSelected[3] = true;
        }

        choiceGroup.setSelectedFlags(isSelected);

        choiceGroup.setFont(0, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));

        choiceGroup.setFont(1, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_MEDIUM));

        choiceGroup.setFont(2, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_LARGE));

        choiceGroup.setFont(3, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));
    }

    private void setFontChoiceGroup(ChoiceGroup choiceGroup, int selected) {
        boolean[] isSelected = new boolean[3];

        if (selected == Font.SIZE_SMALL) {
            isSelected[0] = true;
        } else if (selected == Font.SIZE_MEDIUM) {
            isSelected[1] = true;
        } else if (selected == Font.SIZE_LARGE) {
            isSelected[2] = true;
        }

        choiceGroup.setSelectedFlags(isSelected);

        choiceGroup.setFont(0, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));

        choiceGroup.setFont(1, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_MEDIUM));

        choiceGroup.setFont(2, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_LARGE));

    }

    private void init() {

        setRubyFontChoiceGroup(rubyFontChoiceGroup, settings.getRubyTextSize());
        setFontChoiceGroup(plainFontChoiceGroup, settings.getPlainTextSize());

        stepField.setString(Integer.toString(settings.getStep()));

        fastStepField.setString(Integer.toString(settings.getFastStep()));

        textColorRedField.setString(Integer
                .toString(settings.getTextColor() >> 16));

        textColorGreenField.setString(Integer
                .toString((settings.getTextColor() >> 8) % 256));

        textColorBlueField.setString(Integer
                .toString(settings.getTextColor() % 256));

        backgroundColorRedField.setString(Integer.toString(settings
                .getBackgroundColor() >> 16));

        backgroundColorGreenField.setString(Integer.toString((settings
                .getBackgroundColor() >> 8) % 256));

        backgroundColorBlueField.setString(Integer.toString(settings
                .getBackgroundColor() % 256));

        boolean[] isSelected = new boolean[2];
        if (settings.isTategaki()) {
            isSelected[1] = true;
        } else {
            isSelected[0] = true;
        }
        
        isTategakiChoiceGroup.setSelectedFlags(isSelected);

        widthBetweenPlainsField.setString(Integer.toString(settings
                .getWidthBetweenPlains()));

        widthBetweenPlainAndRubyField.setString(Integer.toString(settings
                .getWidthBetweenPlainAndRuby()));

        widthBetweenRubyAndPlainField.setString(Integer.toString(settings
                .getWidthBetweenRubyAndPlain()));

    }

    private SettingsForm() {

        super("設定");

        rubyFontChoiceGroup = new ChoiceGroup("ルビのフォントサイズ", ChoiceGroup.POPUP,
                rubyFontSizeArray, null);

        plainFontChoiceGroup = new ChoiceGroup("本文のフォントサイズ", ChoiceGroup.POPUP,
                fontSizeArray, null);

        stepField = new TextField("一度に移動する行数", null, 2, TextField.NUMERIC);
        fastStepField = new TextField("キー長押し時に一度に移動する行数", null, 2,
                TextField.NUMERIC);

        textColorRedField = new TextField("テキストの色の赤成分(0-255)", null, 3,
                TextField.NUMERIC);

        textColorGreenField = new TextField("テキストの色の緑成分(0-255)", null, 3,
                TextField.NUMERIC);
        textColorBlueField = new TextField("テキストの色の青成分(0-255)", null, 3,
                TextField.NUMERIC);

        backgroundColorRedField = new TextField("背景の色の赤成分(0-255)", null, 3,
                TextField.NUMERIC);
        backgroundColorGreenField = new TextField("背景の色の緑成分(0-255)", null, 3,
                TextField.NUMERIC);
        backgroundColorBlueField = new TextField("背景の色の青成分(0-255)", null, 3,
                TextField.NUMERIC);

        isTategakiChoiceGroup = new ChoiceGroup("読込時に横書/縦書", ChoiceGroup.POPUP,
                yokotateSizeArray, null);
        widthBetweenPlainsField = new TextField("ルビなし時の行間のピクセル", null, 2,
                TextField.NUMERIC);

        widthBetweenPlainAndRubyField = new TextField("ルビと上/右の文の間のピクセル", null,
                2, TextField.NUMERIC);

        widthBetweenRubyAndPlainField = new TextField("ルビと下/左の文の間のピクセル", null,
                2, TextField.NUMERIC);

        append(rubyFontChoiceGroup);
        append(plainFontChoiceGroup);
        append(stepField);
        append(fastStepField);
        append(textColorRedField);
        append(textColorGreenField);
        append(textColorBlueField);
        
        append(backgroundColorRedField);
        append(backgroundColorGreenField);
        append(backgroundColorBlueField);

        append(isTategakiChoiceGroup);
        append(widthBetweenPlainsField);

        append(widthBetweenPlainAndRubyField);
        append(widthBetweenRubyAndPlainField);

        addCommand(CMD_OK);
        addCommand(CMD_CANCEL);
        setCommandListener(this);
    }

    public int getFontSize(int index) {
        if (index == 0) {
            return Font.SIZE_SMALL;
        } else if (index == 1) {
            return Font.SIZE_MEDIUM;
        } else if (index == 2) {
            return Font.SIZE_LARGE;
        }

        return Settings.FONT_SIZE_NONE;

    }

    private int getRegularizdColorComponent(int color) {
        if (color < 0) {
            color = 0;
        } else if (color > 255) {
            color = 255;
        }

        return color;

    }

    private int getColor(int red, int green, int blue) {
        return (getRegularizdColorComponent(red) << 16)
                + (getRegularizdColorComponent(green) << 8)
                + getRegularizdColorComponent(blue);

    }

    private int getRegulizedStep(int step) {
        if (step <= 0) {
            step = 1;
        }
        return step;
    }

    private int getRegulizedWidth(int width) {
        if (width < 0) {
            width = 0;
        }
        return width;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == CMD_OK) {

            try {

                int index = rubyFontChoiceGroup.getSelectedIndex();
                settings.setRubyTextSize(getFontSize(index));

                index = plainFontChoiceGroup.getSelectedIndex();

                settings.setPlainTextSize(getFontSize(index));

                settings.setStep(getRegulizedStep(Integer.parseInt(stepField
                        .getString())));

                settings.setFastStep(getRegulizedStep(Integer
                        .parseInt(fastStepField.getString())));

                settings.setTextColor(getColor(Integer
                        .parseInt(textColorRedField.getString()), Integer
                        .parseInt(textColorGreenField.getString()), Integer
                        .parseInt(textColorBlueField.getString())));

                settings
                        .setBackgroundColor(getColor(Integer
                                .parseInt(backgroundColorRedField.getString()),
                                Integer.parseInt(backgroundColorGreenField
                                        .getString()), Integer
                                        .parseInt(backgroundColorBlueField
                                                .getString())));

                if (isTategakiChoiceGroup.getSelectedIndex() == 0) {
                    settings.setTategaki(false);
                } else {
                    settings.setTategaki(true);
                }
                settings.setWidthBetweenPlains(getRegulizedWidth(Integer
                        .parseInt(widthBetweenPlainsField.getString())));

                settings.setWidthBetweenPlainAndRuby(getRegulizedWidth(Integer
                        .parseInt(widthBetweenPlainAndRubyField.getString())));
                settings.setWidthBetweenRubyAndPlain(getRegulizedWidth(Integer
                        .parseInt(widthBetweenRubyAndPlainField.getString())));

                RecordStoreUtil.saveSettings(settings);

            } catch (NumberFormatException e) {
                Alert alert = new Alert("数字が正しく入力されていません", "数字が正しく入力されていません",
                        null, null);
                display.setCurrent(alert);

            } catch (Exception e) {
                Alert alert = new Alert("設定の保存に失敗しました", "設定の保存に失敗しました", null,
                        null);
                display.setCurrent(alert);

            }
        } else if (command == CMD_CANCEL) {
        }
        if (previousDisplayable instanceof AozoraCanvas) {
            ((AozoraCanvas) previousDisplayable).finalizeSettei(true);
        }
        display.setCurrent(previousDisplayable);

    }

}
