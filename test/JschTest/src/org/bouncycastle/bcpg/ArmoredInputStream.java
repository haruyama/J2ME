package org.bouncycastle.bcpg;

import java.util.Vector;
import java.io.*;

import javaxxx.io.*;

/**
 * reader for Base64 armored objects - read the headers and then start returning
 * bytes when the data is reached. An IOException is thrown if the CRC check
 * fails.
 */
public class ArmoredInputStream
    extends InputStream
{
    /*
     * set up the decoding table.
     */
    private static final byte[] decodingTable;

    static
    {
        decodingTable = new byte[128];

        for (int i = 'A'; i <= 'Z'; i++)
        {
            decodingTable[i] = (byte)(i - 'A');
        }

        for (int i = 'a'; i <= 'z'; i++)
        {
            decodingTable[i] = (byte)(i - 'a' + 26);
        }

        for (int i = '0'; i <= '9'; i++)
        {
            decodingTable[i] = (byte)(i - '0' + 52);
        }

        decodingTable['+'] = 62;
        decodingTable['/'] = 63;
    }

    /**
     * decode the base 64 encoded input data.
     *
     * @return the offset the data starts in out.
     */
    private int decode(
        int      in0,
        int      in1,
        int      in2,
        int      in3,
        int[]    out)
        throws EOFException
    {
        int    b1, b2, b3, b4;

        if (in3 < 0)
        {
            throw new EOFException("unexpected end of file in armored stream.");
        }

        if (in2 == '=')
        {
            b1 = decodingTable[in0] &0xff;
            b2 = decodingTable[in1] & 0xff;

            out[2] = ((b1 << 2) | (b2 >> 4)) & 0xff;

            return 2;
        }
        else if (in3 == '=')
        {
            b1 = decodingTable[in0];
            b2 = decodingTable[in1];
            b3 = decodingTable[in2];

            out[1] = ((b1 << 2) | (b2 >> 4)) & 0xff;
            out[2] = ((b2 << 4) | (b3 >> 2)) & 0xff;

            return 1;
        }
        else
        {
            b1 = decodingTable[in0];
            b2 = decodingTable[in1];
            b3 = decodingTable[in2];
            b4 = decodingTable[in3];

            out[0] = ((b1 << 2) | (b2 >> 4)) & 0xff;
            out[1] = ((b2 << 4) | (b3 >> 2)) & 0xff;
            out[2] = ((b3 << 6) | b4) & 0xff;

            return 0;
        }
    }

    InputStream    in;
    boolean        start = true;
    int[]          outBuf = new int[3];
    int            bufPtr = 3;
    CRC24          crc = new CRC24();
    boolean        crcFound = false;
    boolean        hasHeaders = true;
    String         header = null;
    boolean        newLineFound = false;
    boolean        clearText = false;
    boolean        restart = false;
    Vector         headerList= new Vector();
    
    /**
     * Create a stream for reading a PGP armoured message, parsing up to a header 
     * and then reading the data that follows.
     * 
     * @param in
     */
    public ArmoredInputStream(
        InputStream    in) 
        throws IOException
    {
        this.in = in;
        
        if (hasHeaders)
        {
            parseHeaders();
        }

        start = false;
    }
    
    /**
     * Create an armoured input stream which will assume the data starts
     * straight away, or parse for headers first depending on the value of 
     * hasHeaders.
     * 
     * @param in
     * @param hasHeaders true if headers are to be looked for, false otherwise.
     */
    public ArmoredInputStream(
        InputStream    in,
        boolean        hasHeaders) 
        throws IOException
    {
        this.in = in;
        this.hasHeaders = hasHeaders;
        
        if (hasHeaders)
        {
            parseHeaders();
        }

        start = false;
    }
    
    public int available()
        throws IOException
    {
        return in.available();
    }
    
    private boolean parseHeaders()
        throws IOException
    {
        header = null;
        
        int        c;
        int        last = 0;
        boolean    headerFound = false;
        
        headerList = new Vector();
        
        //
        // if restart we already have a header
        //
        if (restart)
        {
            headerFound = true;
        }
        else
        {
            while ((c = in.read()) >= 0)
            {
                if (c == '-' && (last == 0 || last == '\n'))
                {
                    headerFound = true;
                    break;
                }
    
                last = c;
            }
        }

        if (headerFound)
        {
            StringBuffer    buf = new StringBuffer("-");
            
            if (restart)    // we've had to look ahead two '-'
            {
                buf.append('-');
            }
            
            while ((c = in.read()) >= 0)
            {
                if (last == '\n' && c == '\n')
                {
                    break;
                }
                if (c == '\r' || c == '\n')
                {
                    headerList.addElement(buf.toString());
                    buf.setLength(0);
                }
                if (c != '\r')
                {
                    last = c;
                    if (c != '\n')
                    {
                        buf.append((char)last);
                    }
                }
            }
        }
        
        if (headerList.size() > 0)
        {
            header = (String)headerList.elementAt(0);
        }
        
        clearText = "-----BEGIN PGP SIGNED MESSAGE-----".equals(header);
        newLineFound = true;
        
        return headerFound;
    }

    /**
     * @return true if we are inside the clear text section of a PGP
     * signed message.
     */
    public boolean isClearText()
    {
        return clearText;
    }
    
    /**
     * Return the armor header line (if there is one)
     * @return the armor header line, null if none present.
     */
    public String    getArmorHeaderLine()
    {
        return header;
    }
    
    /**
     * Return the armor headers (the lines after the armor header line),
     * @return an array of armor headers, null if there aren't any.
     */
    public String[] getArmorHeaders()
    {
        if (headerList.size() <= 1)
        {
            return null;
        }
        
        String[]    hdrs = new String[headerList.size() - 1];
        
        for (int i = 0; i != hdrs.length; i++)
        {
            hdrs[i] = (String)headerList.elementAt(i + 1);
        }
        
        return hdrs;
    }
    
    private int readIgnoreSpace() 
        throws IOException
    {
        int    c = in.read();
        
        while (c == ' ' || c == '\t')
        {
            c = in.read();
        }
        
        return c;
    }
    
    public int read()
        throws IOException
    {
        int    c;

        if (start)
        {
            if (hasHeaders)
            {
                parseHeaders();
            }

            start = false;
        }
        
        if (clearText)
        {
            c = in.read();

            if (c == '\n')
            {
                newLineFound = true;
            }
            else if (newLineFound && c == '-')
            {
                c = in.read();
                if (c == '-')            // a header, not dash escaped
                {
                    clearText = false;
                    start = true;
                    restart = true;
                }
                else                   // a space - must be a dash escape
                {
                    c = in.read();
                }
                newLineFound = false;
            }
            else
            {
                newLineFound = false;
            }
            return c;
        }
        
        if (bufPtr > 2)
        {
            c = readIgnoreSpace();
            
            if (c == '\n' || c == '\r')
            {
                c = readIgnoreSpace();
                while (c == '\n' || c == '\r')
                {
                    c = readIgnoreSpace();
                }

                if (c < 0)                // EOF
                {
                    return -1;
                }
    
                if (c == '=')            // crc reached
                {
                    bufPtr = decode(readIgnoreSpace(), readIgnoreSpace(), readIgnoreSpace(), readIgnoreSpace(), outBuf);
                    if (bufPtr == 0)
                    {
                        int i = ((outBuf[0] & 0xff) << 16)
                                | ((outBuf[1] & 0xff) << 8)
                                | (outBuf[2] & 0xff);

                        crcFound = true;

                        if (i != crc.getValue())
                        {
                            throw new IOException("crc check failed in armored message.");
                        }

                        return -1;
                    }
                    else
                    {
                        throw new IOException("no crc found in armored message.");
                    }
                }
                else if (c == '-')        // end of record reached
                {
                    while ((c = in.read()) >= 0)
                    {
                        if (c == '\n')
                        {
                            break;
                        }
                    }

                    if (!crcFound)
                    {
                        throw new IOException("crc check not found.");
                    }

                    return -1;
                }
                else                   // data
                {
                    bufPtr = decode(c, readIgnoreSpace(), readIgnoreSpace(), readIgnoreSpace(), outBuf);
                }
            }
            else
            {
                if (c >= 0)
                {
                    bufPtr = decode(c, readIgnoreSpace(), readIgnoreSpace(), readIgnoreSpace(), outBuf);
                }
                else
                {
                    return -1;
                }
            }
        }

        c = outBuf[bufPtr++];

        crc.update(c);

        return c;
    }
}
