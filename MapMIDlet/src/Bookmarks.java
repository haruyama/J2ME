import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

public class Bookmarks {
    private Vector vector;

    private static Bookmarks bookmarks = new Bookmarks();

    private Bookmarks() {

        vector = new Vector();
    }

    public static Bookmarks getInstance() {

        return bookmarks;

    }

    // public void addElement(FileInfo fileInfo) {
    // addElement(Bookmark.getInstance(fileInfo));
    // }

    public void addElement(Bookmark bookmark) {
        vector.addElement(bookmark);
        try {

            RecordStoreUtil.saveBookmarks(this);
        } catch (RecordStoreException e) {

        } catch (IOException e) {
        }

    }

    public void removeAllElement() {
        vector.removeAllElements();
        try {
            RecordStoreUtil.saveBookmarks(this);
        } catch (RecordStoreException e) {
        } catch (IOException e) {
        }

    }

    // public boolean removeElement(Bookmark bookmark) {
    // boolean b = vector.removeElement(bookmark);
    // try {
    // RecordStoreUtil.saveBookmarks(this);
    // } catch (RecordStoreException e) {
    // } catch (IOException e) {
    // }
    // return b;
    //
    // }

    public void removeElementAt(int i) {
        vector.removeElementAt(i);
        try {
            RecordStoreUtil.saveBookmarks(this);
        } catch (RecordStoreException e) {
        } catch (IOException e) {
        }
    }

    // public Enumeration elements() {
    // return vector.elements();
    // }

    public Bookmark elementAt(int i) {
        return (Bookmark) vector.elementAt(i);
    }

    public int size() {
        return vector.size();
    }

}
