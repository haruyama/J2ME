import java.util.Hashtable;
import java.util.Vector;

public class TategakiUtil {

    private static Hashtable convertTable;

    private static Hashtable zurasiTable;
    
    

    static {
        zurasiTable = new Hashtable();
        
        zurasiTable.put(new Character('゜'), new Double(0.5));
        zurasiTable.put(new Character('｀'), new Double(0.3));
        zurasiTable.put(new Character('┐'), new Double(0.4));
        zurasiTable.put(new Character('└'), new Double(-0.3));
        zurasiTable.put(new Character('┓'), new Double(0.4));
        zurasiTable.put(new Character('┗'), new Double(-0.3));

        // 「たてがき君」
        // http://hp.vector.co.jp/authors/VA022533/tate/note.html
        // の表を用いています。表の著作権は縦の会 <Ojisam@hotmail.com> にあります。
        // 「たてがき君」のコードは用いていません。

        convertTable = new Hashtable();
        convertTable.put(new Character('。'), new Character('゜'));
        convertTable.put(new Character('、'), new Character('｀'));
        convertTable.put(new Character('，'), new Character('｀'));
        convertTable.put(new Character('．'), new Character('・'));
        convertTable.put(new Character('「'), new Character('┐'));
        convertTable.put(new Character('」'), new Character('└'));
        convertTable.put(new Character('『'), new Character('┓'));
        convertTable.put(new Character('』'), new Character('┗'));
        convertTable.put(new Character('ー'), new Character('｜'));
        convertTable.put(new Character('−'), new Character('｜'));
        convertTable.put(new Character('‐'), new Character('｜'));
        convertTable.put(new Character('-'), new Character('｜'));
        
        convertTable.put(new Character('—'), new Character('｜'));
        convertTable.put(new Character('─'), new Character('｜'));
        convertTable.put(new Character('｜'), new Character('―'));
        convertTable.put(new Character('〜'), new Character('∫'));
        convertTable.put(new Character('＜'), new Character('∧'));
        convertTable.put(new Character('〈'), new Character('∧'));
        convertTable.put(new Character('｛'), new Character('∧'));
        convertTable.put(new Character('＞'), new Character('∨'));
        convertTable.put(new Character('〉'), new Character('∨'));
        convertTable.put(new Character('｝'), new Character('∨'));
        convertTable.put(new Character('∧'), new Character('＞'));
        convertTable.put(new Character('∨'), new Character('＜'));
        convertTable.put(new Character('⊂'), new Character('∩'));
        convertTable.put(new Character('⊃'), new Character('∪'));
        convertTable.put(new Character('∩'), new Character('⊃'));
        convertTable.put(new Character('∪'), new Character('⊂'));

        convertTable.put(new Character('⇒'), new Character('↓'));
        convertTable.put(new Character('→'), new Character('↓'));
        convertTable.put(new Character('←'), new Character('↑'));
        convertTable.put(new Character('↑'), new Character('→'));
        convertTable.put(new Character('↓'), new Character('←'));
        convertTable.put(new Character('‥'), new Character('：'));
        convertTable.put(new Character('…'), new Character('：'));
        convertTable.put(new Character('：'), new Character('‥'));
        convertTable.put(new Character('；'), new Character('‥'));
        convertTable.put(new Character('＝'), new Character('‖'));
        convertTable.put(new Character('='), new Character('‖'));
        convertTable.put(new Character('‖'), new Character('＝'));

        convertTable.put(new Character('（'), new Character('∧'));
        convertTable.put(new Character('〔'), new Character('∧'));
        convertTable.put(new Character('['), new Character('∧'));
        convertTable.put(new Character('('), new Character('∧'));
        convertTable.put(new Character('［'), new Character('∧'));
        convertTable.put(new Character('<'), new Character('∧'));
        convertTable.put(new Character('【'), new Character('∧'));
        convertTable.put(new Character('≪'), new Character('∧'));
        
        
        convertTable.put(new Character('）'), new Character('∨'));
        convertTable.put(new Character('〕'), new Character('∨'));
        convertTable.put(new Character('］'), new Character('∨'));
        convertTable.put(new Character(']'), new Character('∨'));
        convertTable.put(new Character(')'), new Character('∨'));
        convertTable.put(new Character('】'), new Character('∨'));
        convertTable.put(new Character('≫'), new Character('∨'));
        convertTable.put(new Character('>'), new Character('∨'));

    }

    public static Vector convertToTategakiStrings(Vector strings) {
        Vector convertedStrings = new Vector();
        int size = strings.size();
        
        for (int i = 0; i < size; ++i) {
            convertedStrings.addElement(convertToTategaki((String) strings
                    .elementAt(i)));

        }
        return convertedStrings;

    }

    private static String convertToTategaki(String string) {

        StringBuffer buffer = new StringBuffer();
        int length = string.length();
        
        Hashtable table  = convertTable; 

        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            
            Character converted = (Character) table.get(new Character(c));
            if (converted != null) {

                buffer.append(converted.charValue());
            } else {
                buffer.append(c);
            }

        }

        return buffer.toString();
    }
    
    public static double doZursu(char c) {
        Double double1 = (Double)zurasiTable.get(new Character(c));
        if(double1 == null) {
            return 0.0d;
        }
        return double1.doubleValue();
        
    }
    
}
