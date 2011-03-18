/*
 * This source code file is public domain
 * http://sourceforge.net/projects/uxparser
 */
package uxparser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;



// import java.util.zip.*;

/**
 * XWriter is a specialized Writer that provides support for generating an XML
 * output stream.
 * 
 * @author Brian Frank
 * @creation 29 Sept 00
 * @version $Revision: 8$ $Date: 19-Jul-04 4:19:10 PM$
 */
public class XWriter extends Writer {

    // //////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////

    /**
     * Construct writer for specified file.
     */
    /*
     * public XWriter(File file) throws IOException { this(new
     * BufferedOutputStream(new FileOutputStream(file))); }
     */
    /**
     * Construct writer for specified output stream.
     */
    public XWriter(OutputStream out) throws IOException {
        this.sink = out;
    }

    // //////////////////////////////////////////////////////////////
    // Public
    // //////////////////////////////////////////////////////////////

    /**
     * Write the specified Object and return this.
     */
    public XWriter w(Object x) throws IOException {
        write(String.valueOf(x));
        return this;
    }

    /**
     * Write the specified boolean and return this.
     */
    public final XWriter w(boolean x) throws IOException {
        write(String.valueOf(x));
        return this;
    }

    /**
     * Write the specified char and return this.
     */
    public final XWriter w(char x) throws IOException {
        write(x);
        return this;
    }

    /**
     * Write the specified int and return this.
     */
    public final XWriter w(int x) throws IOException {
        write(String.valueOf(x));
        return this;
    }

    /**
     * Write the specified long and return this.
     */
    public final XWriter w(long x) throws IOException {
        write(String.valueOf(x));
        return this;
    }

    /**
     * Write the specified float and return this.
     */
    public final XWriter w(float x) throws IOException {
        write(String.valueOf(x));
        return this;
    }

    /**
     * Write the specified double and return this.
     */
    public final XWriter w(double x) throws IOException {
        write(String.valueOf(x));
        return this;
    }

    /**
     * Write a newline character and return this.
     */
    public final XWriter nl() throws IOException {
        write('\n');
        return this;
    }

    /**
     * Write the specified number of spaces.
     */
    public final XWriter indent(int indent) throws IOException {
        write(getSpaces(indent));
        return this;
    }

    /**
     * Write an attribute pair <code>name="value"</code> where the value is
     * written using safe().
     */
    public final XWriter attr(String name, String value) throws IOException {
        write(name);
        write('=');
        write('"');
        safe(value);
        write('"');
        return this;
    }

    /**
     * This write the standard prolog
     * <code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;</code>
     */
    public XWriter prolog() throws IOException {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        return this;
    }

    // //////////////////////////////////////////////////////////////
    // Safe
    // //////////////////////////////////////////////////////////////

    /**
     * Convenience for <code>XWriter.safe(this, s, escapeWhitespace)</code>.
     */
    public final XWriter safe(String s, boolean escapeWhitespace)
            throws IOException {
        try {
            XWriter.safe(xout, s, escapeWhitespace);
            return this;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Convenience for <code>XWriter.safe(this, s, true)</code>.
     */
    public final XWriter safe(String s) throws IOException {

        XWriter.safe(xout, s, true);
        return this;

    }

    /**
     * Convenience for <code>XWriter.safe(this, c, escapeWhitespace)</code>.
     */
    public final XWriter safe(int c, boolean escapeWhitespace)
            throws IOException {
        XWriter.safe(this, c, escapeWhitespace);
        return this;
    }

    /**
     * This writes each character in the string to the output stream using the
     * <code>safe(Writer, int, boolean)</code> method.
     */
    public static void safe(Writer out, String s, boolean escapeWhitespace)
            throws IOException {
        int len = s.length();
        for (int i = 0; i < len; ++i)
            safe(out, s.charAt(i), escapeWhitespace);
    }

    /**
     * Write a "safe" character. This method will escape unsafe characters
     * common in XML and HTML markup.
     * <ul>
     * <li>'&lt;' -> &amp;lt;</li>
     * <li>'&gt;' -> &amp;gt;</li>
     * <li>'&amp;' -> &amp;amp;</li>
     * <li>'&apos;' -> &amp;#x27;</li>
     * <li>'&quot;' -> &amp;#x22;</li>
     * <li>Below 0x20 -> &amp;#x{hex};</li>
     * <li>Above 0x7E -> &amp;#x{hex};</li>
     * </ul>
     */
    public static void safe(Writer out, int c, boolean escapeWhitespace)
            throws IOException {
        if (c < 0x20 || c > 0x7e || c == '\'' || c == '"') {
            if (!escapeWhitespace) {
                if (c == '\n') {
                    out.write('\n');
                    return;
                }
                if (c == '\r') {
                    out.write('\r');
                    return;
                }
                if (c == '\t') {
                    out.write('\t');
                    return;
                }
            }
            out.write("&#x");
            out.write(Integer.toHexString(c));
            out.write(';');
        } else if (c == '<')
            out.write("&lt;");
        else if (c == '>')
            out.write("&gt;");
        else if (c == '&')
            out.write("&amp;");
        else
            out.write((char) c);
    }

    // //////////////////////////////////////////////////////////////
    // Zip
    // //////////////////////////////////////////////////////////////

    /**
     * Return if this XWriter is being used to generate a PKZIP file containing
     * the XML document. See <code>setZipped()</code>
     */
    public boolean isZipped() {
        return zipped;
    }

    /**
     * If set to true, then XWriter generates a compressed PKZIP file with one
     * entry called "file.xml". This method cannot be called once bytes have
     * been written. Zipped XWriters should only be used with stand alone files,
     * it should not be used in streams mixed with other data. This feature is
     * used in conjunction with XParser, which automatically detects plain text
     * XML versuse PKZIP documents.
     */
    public void setZipped(boolean zipped) throws IOException {
        if (numWritten != 0)
            throw new IllegalStateException(
                    "Cannot setZipped after data has been written");

        this.zipped = zipped;
    }

    // //////////////////////////////////////////////////////////////
    // Writer
    // //////////////////////////////////////////////////////////////

    public void write(int c) throws IOException {
        if (xout == null)
            initOut();
        numWritten++;
        xout.write(c);

    }

    public void write(char[] buf) throws IOException {
        if (xout == null)
            initOut();
        numWritten += buf.length;
        xout.write(buf);
    }

    public void write(char[] buf, int off, int len) throws IOException {

        if (xout == null)
            initOut();
        numWritten += len;
        xout.write(buf, off, len);
    }

    public void write(String str) throws IOException {
        if (xout == null)
            initOut();
        numWritten += str.length();
        xout.write(str);
    }

    public void write(String str, int off, int len) throws IOException{
            if (xout == null)
                initOut();
            numWritten += len;
            xout.write(str, off, len);

    }

    public void flush() throws IOException {
            if (xout == null)
                initOut();
            xout.flush();
    }

    public void close() throws IOException{

            if (xout == null)
                initOut();
            xout.close();
    }

    void initOut() throws IOException {
        /*
         * if (zipped) { zout = new ZipOutputStream(sink); zout.putNextEntry(new
         * ZipEntry("file.xml")); this.xout = new OutputStreamWriter(zout,
         * "UTF8"); } else {
         */
        this.xout = new OutputStreamWriter(sink, "UTF8");
        // }
    }

    // //////////////////////////////////////////////////////////////
    // Spaces
    // //////////////////////////////////////////////////////////////

    static String getSpaces(int num) {
        try {
            // 99.9% of the time num is going to be
            // smaller than 50, so just try it
            return SPACES[num];
        } catch (ArrayIndexOutOfBoundsException e) {
            if (num < 0)
                return "";

            // too big!
            int len = SPACES.length;
            StringBuffer buf;
            buf = new StringBuffer(num);
            int rem = num;
            while (true) {
                if (rem < len) {
                    buf.append(SPACES[rem]);
                    break;
                } else {
                    buf.append(SPACES[len - 1]);
                    rem -= len - 1;
                }
            }
            return buf.toString();
        }
    }

    private static String[] SPACES;
    static {
        SPACES = new String[50];
        SPACES[0] = "";
        for (int i = 1; i < 50; ++i)
            SPACES[i] = SPACES[i - 1] + " ";
    }

    // //////////////////////////////////////////////////////////////
    // Attributes
    // //////////////////////////////////////////////////////////////

    private OutputStream sink; // the underlying output sink

    private Writer xout; // writer for XML markup

    // private ZipOutputStream zout; // zipped stream if zipped is true
    private boolean zipped; // are we generating a zip file

    private int numWritten; // number of chars written

}
