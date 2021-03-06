package org.bouncycastle.asn1.x509;

import javaxxx.math.BigInteger;

import org.bouncycastle.asn1.DERInteger;

/**
 * The CRLNumber object.
 * <pre>
 * CRLNumber::= INTEGER(0..MAX)
 * </pre>
 */
public class CRLNumber
    extends DERInteger
{

    public CRLNumber(
        BigInteger number)
    {
        super(number);
    }

    public BigInteger getCRLNumber()
    {
        return getPositiveValue();
    }
}
