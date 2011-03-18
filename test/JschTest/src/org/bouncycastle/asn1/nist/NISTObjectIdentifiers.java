package org.bouncycastle.asn1.nist;

import org.bouncycastle.asn1.DERObjectIdentifier;

public interface NISTObjectIdentifiers
{
    //
    // NIST
    //     iso/itu(2) joint-assign(16) us(840) organization(1) gov(101) csor(3) 

    //
    // nistalgorithms(4)
    //
    static final String                 nistAlgorithm          = "2.16.840.1.101.3.4";

    static final DERObjectIdentifier    id_sha256               = new DERObjectIdentifier(nistAlgorithm + ".2.1");
    static final DERObjectIdentifier    id_sha384               = new DERObjectIdentifier(nistAlgorithm + ".2.2");
    static final DERObjectIdentifier    id_sha512               = new DERObjectIdentifier(nistAlgorithm + ".2.3");
    static final DERObjectIdentifier    id_sha224               = new DERObjectIdentifier(nistAlgorithm + ".2.4");
}
