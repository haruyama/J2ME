package org.bouncycastle.bcpg;
import java.io.*;

import javaxxx.io.*;

/**
 * basic packet for a PGP secret key
 */
public class SecretSubkeyPacket 
    extends SecretKeyPacket
{
    /**
     * 
     * @param in
     * @throws IOException
     */
    SecretSubkeyPacket(
        BCPGInputStream    in)
        throws IOException
    { 
        super(in);
    }
    
    /**
     * 
     * @param pubKeyPacket
     * @param encAlgorithm
     * @param s2k
     * @param iv
     * @param secKeyData
     */
    public SecretSubkeyPacket(
        PublicKeyPacket  pubKeyPacket,
        int              encAlgorithm,
        S2K              s2k,
        byte[]           iv,
        byte[]           secKeyData)
    {
        super(pubKeyPacket, encAlgorithm, s2k, iv, secKeyData);
    }
 
    public void encode(
        BCPGOutputStream    out)
        throws IOException
    {
        out.writePacket(SECRET_SUBKEY, getEncodedContents(), true);
    }
}
