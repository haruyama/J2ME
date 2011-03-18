/*
 * This source code file is public domain
 * http://sourceforge.net/projects/uxparser
 * 
 * modified by HARUYAMA Seigo <haruyama@unixuser.org>
 */
package uxparser;

/**
 * XNs models an XML namespace. XNs are usually created as attributes on XElems
 * using the <code>XElem.defineNs()</code> and
 * <code>XElem.defineDefaultNs()</code> methods. Two XNs instances are equal
 * if they have the same uri.
 * 
 * @author Brian Frank
 * @creation 6 Apr 02
 * @version $Revision: 1$ $Date: 4/9/2002 5:35:16 PM$
 */
public final class XNs {

    // //////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////

    /**
     * Create a new XNs instance with the specified prefix and uri.
     */
    public XNs(String prefix, String uri) {
        if (prefix == null || uri == null)
            throw new NullPointerException();

        this.prefix = prefix;
        this.uri = uri;
    }

    // //////////////////////////////////////////////////////////////
    // Access
    // //////////////////////////////////////////////////////////////

    /**
     * Return if this a default XNs namespace which has a prefix of "".
     */
    public boolean isDefault() {
        return prefix.equals("");
    }

    /**
     * Get the prefix used to tag elements with this namespace. If this is the
     * default namespace then return "".
     */
    public final String prefix() {
        return prefix;
    }

    /**
     * Get the uri which defines a universally unique namespace.
     */
    public final String uri() {
        return uri;
    }

    /**
     * Return uri.
     */
    public String toString() {
        return uri;
    }

    // //////////////////////////////////////////////////////////////
    // Identity
    // //////////////////////////////////////////////////////////////

    /**
     * Two instances of XNs are equal if they have the exact same uri
     * characters.
     */
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof XNs) {
            return uri.equals(((XNs) obj).uri);
        }
        return false;
    }

    /**
     * Two instances of XNs are equal if they have the exact same uri
     * characters.
     */
    static boolean equals(Object ns1, Object ns2) {
        if (ns1 == null)
            return ns2 == null;
        return ns1.equals(ns2);
    }

    public int hashCode() {
        return prefix.hashCode() + uri.hashCode();
    }

    // //////////////////////////////////////////////////////////////
    // Attributes
    // //////////////////////////////////////////////////////////////

    String prefix;

    String uri;
    // XElem declaringElem;

}
