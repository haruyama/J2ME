import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Font;

public class Settings {

    private static final int SETTINGS_FORMAT_VERSION = 1;
    
    /**
     * plainTextSize, rubyTextSize, step, fastStep, textColor, backgroundColor, isTategaki 
     */
    
    private static final int SETTINGS_FORMAT_VERSION_0 = 0;

    /**
     * Plainな部分を描画するフォントのサイズ
     */
    private int plainTextSize;

    /**
     * ルビの部分を描画するフォントのサイズ
     */
    private int rubyTextSize;


    /**
     * 一度に移動する行数
     */
    private int step;

    /**
     * キーを押しっぱなしにしたときに一度に移動する行する
     */
    private int fastStep;

    private int textColor;

    private int backgroundColor;

    private boolean isTategaki;
    
    private int widthBetweenPlains;
    private int widthBetweenPlainAndRuby;
    private int widthBetweenRubyAndPlain;

    /* Font.SIZE_ が 0, 8, 16 であることに依存している */
    public static final int FONT_SIZE_NONE = 32;

    private static Settings settings;

    private static final Object LOCK = new Object();

    public static Settings getInstance() {

        synchronized (LOCK) {
            if (settings != null) {
                return settings;
            }

            if (settings == null) {
                try {
                    settings = RecordStoreUtil.loadSettings();
                } catch (Exception e) {
                }

            }

            if (settings == null) {
                settings = new Settings();
            }
            return settings;
        }

    }

    private Settings() {
        plainTextSize = Font.SIZE_MEDIUM;
        rubyTextSize = Font.SIZE_SMALL;
        step = 3;
        fastStep = 6;
        textColor = 0;
        backgroundColor = (255 << 8) + 255;
        isTategaki = false;
        widthBetweenPlains = 5;
        widthBetweenPlainAndRuby = 0;
        widthBetweenRubyAndPlain = 0;
    }

   

    /**
     * @param plainTextSize
     * @param rubyTextSize
     * @param step
     * @param fastStep
     * @param textColor
     * @param backgroundColor
     * @param isTategaki
     * @param widthBetweenPlains
     * @param widthBetweenPlainAndRuby
     * @param widthBetweenRubyAndPlain
     */
    public Settings(int plainTextSize, int rubyTextSize, int step, int fastStep, int textColor, int backgroundColor, boolean isTategaki, int widthBetweenPlains, int widthBetweenPlainAndRuby, int widthBetweenRubyAndPlain) {
        super();
        this.plainTextSize = plainTextSize;
        this.rubyTextSize = rubyTextSize;
        this.step = step;
        this.fastStep = fastStep;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.isTategaki = isTategaki;
        this.widthBetweenPlains = widthBetweenPlains;
        this.widthBetweenPlainAndRuby = widthBetweenPlainAndRuby;
        this.widthBetweenRubyAndPlain = widthBetweenRubyAndPlain;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getFastStep() {
        return fastStep;
    }

    public void setFastStep(int fastStep) {
        this.fastStep = fastStep;
    }

    public int getPlainTextSize() {
        return plainTextSize;
    }

    public void setPlainTextSize(int plainTextSize) {
        this.plainTextSize = plainTextSize;
    }

    public int getRubyTextSize() {
        return rubyTextSize;
    }

    public void setRubyTextSize(int rubyTextSize) {
        this.rubyTextSize = rubyTextSize;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }



    public boolean isTategaki() {
        return isTategaki;
    }

    public void setTategaki(boolean isTategaki) {
        this.isTategaki = isTategaki;
    }

    // public void clearTempFont() {
    // tempPlainTextFont = null;
    // tempRubyTextFont = null;
    // }

    public static byte[] toByteArray(Settings settings) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] b = null;
        try {
            dos.writeInt(SETTINGS_FORMAT_VERSION);

            dos.writeInt(settings.getPlainTextSize());
            dos.writeInt(settings.getRubyTextSize());
            dos.writeInt(settings.getStep());
            dos.writeInt(settings.getFastStep());
            dos.writeInt(settings.getTextColor());
            dos.writeInt(settings.getBackgroundColor());
            dos.writeBoolean(settings.isTategaki());
            dos.writeInt(settings.getWidthBetweenPlains());
            dos.writeInt(settings.getWidthBetweenPlainAndRuby());
            dos.writeInt(settings.getWidthBetweenRubyAndPlain());
            
            b = baos.toByteArray();
        } finally {
            if(dos!=null) {
                dos.close();
            }
            if(baos!=null) {
                baos.close();
            }
        }

        return b;
    }

    public static Settings fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Settings settings = null;

        try {

            int version = dis.readInt();
            if (version > SETTINGS_FORMAT_VERSION) {
                throw new IOException("バージョンが異なります");
            }
            int plainTextSize = dis.readInt();
            int rubyTextSize = dis.readInt();
            int step = dis.readInt();
            int fastStep = dis.readInt();
            int textColor = dis.readInt();
            int backgroundColor = dis.readInt();
            boolean isTategaki = dis.readBoolean();
            
            int widthBetweenPlains;
            int widthBetweenPlainAndRuby;
            int widthBetweenRubyAndPlain;
            if(version != SETTINGS_FORMAT_VERSION_0) {
                widthBetweenPlains = dis.readInt();
                widthBetweenPlainAndRuby = dis.readInt();
                widthBetweenRubyAndPlain = dis.readInt();
            }else {
                widthBetweenPlains = 1;
                widthBetweenPlainAndRuby = 0;
                widthBetweenRubyAndPlain = 0;

            }

            settings = new Settings(plainTextSize, rubyTextSize, step,
                    fastStep, textColor, backgroundColor, isTategaki,
                    widthBetweenPlains, widthBetweenPlainAndRuby,
                    widthBetweenRubyAndPlain );

        } finally {
            if(dis!=null) {
                dis.close();
            }
            if(bais!=null) {
                bais.close();
            }
        }

        return settings;

    }

    public int getWidthBetweenPlainAndRuby() {
        return widthBetweenPlainAndRuby;
    }

    public void setWidthBetweenPlainAndRuby(int widthBetweenPlainAndRuby) {
        this.widthBetweenPlainAndRuby = widthBetweenPlainAndRuby;
    }

    public int getWidthBetweenPlains() {
        return widthBetweenPlains;
    }

    public void setWidthBetweenPlains(int widthBetweenPlains) {
        this.widthBetweenPlains = widthBetweenPlains;
    }

    public int getWidthBetweenRubyAndPlain() {
        return widthBetweenRubyAndPlain;
    }

    public void setWidthBetweenRubyAndPlain(int widthBetweenRubyAndPlain) {
        this.widthBetweenRubyAndPlain = widthBetweenRubyAndPlain;
    }


}
