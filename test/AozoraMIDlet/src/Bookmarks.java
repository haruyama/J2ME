import java.util.Enumeration;
import java.util.Vector;

public class Bookmarks {
    private Vector vector;

    private static Bookmarks bookmarks = new Bookmarks();

    private Bookmarks() {

        vector = new Vector();
    }

    public static Bookmarks getInstance() {
        return bookmarks;

    }

//    public void addElement(FileInfo fileInfo) {
//        addElement(Bookmark.getInstance(fileInfo));
        //    }
    
    public void addElement(Bookmark bookmark) {
        vector.addElement(bookmark);
        try {
            
            RecordStoreUtil.saveBookmarks(this);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeAllElements() {
        vector.removeAllElements();
        try {
            RecordStoreUtil.saveBookmarks(this);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean removeElement(Bookmark bookmark) {
        boolean b =  vector.removeElement(bookmark);
        try {
        RecordStoreUtil.saveBookmarks(this);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return b;
        
        
    }

    public void removeElementAt(int i) {
        vector.removeElementAt(i);
        try {
            RecordStoreUtil.saveBookmarks(this);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Enumeration elements() {
        return vector.elements();
    }

    public Bookmark elementAt(int i) {
        return (Bookmark)vector.elementAt(i);
    }

    public int size() {
        return vector.size();
    }
    

}
