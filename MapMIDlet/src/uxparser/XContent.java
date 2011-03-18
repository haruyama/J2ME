/*
 * This source code file is public domain
 * http://sourceforge.net/projects/uxparser
 * 
 * modified by HARUYAMA Seigo <haruyama@unixuser.org>
 */
package uxparser;

/**
 * XContent is the super class of the various element content classes.
 * 
 * @author Brian Frank
 * @creation 6 Apr 02
 * @version $Revision: 2$ $Date: 6/18/2002 11:39:17 AM$
 */
public abstract class XContent {

    // //////////////////////////////////////////////////////////////
    // Access
    // //////////////////////////////////////////////////////////////

    /**
     * Get the parent element or null if not currently parented.
     */
    public final XElem parent() {
        return parent;
    }

    /**
     * XContent equality is defined by the == operator.
     */
    // public final boolean equals(Object obj)
    // {
    // return this == obj;
    // }
    /**
     * Write to the XWriter.
     * 
     * @throws IOException
     */
    // public abstract void write(XWriter out) throws IOException;
    // //////////////////////////////////////////////////////////////
    // Attributes
    // //////////////////////////////////////////////////////////////
    XElem parent;

}
