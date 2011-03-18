/* -*-mode:java; c-basic-offset:2; -*- */
/*
Copyright (c) 2004 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.jcraft.jsch.bc;

import javaxxx.math.BigInteger;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.params.DSAKeyParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;

public class SignatureDSA implements com.jcraft.jsch.SignatureDSA{
  private DSASigner signature;
  private SHA1 sha;

  public void init() throws Exception{
    signature=new DSASigner();
    sha=new SHA1();
    sha.init();
//System.out.println(this+".init()");
  }     
  public void setPubKey(byte[] y, byte[] p, byte[] q, byte[] g) throws Exception{
//System.out.println(this+".setPubKey()");
    DSAPublicKeyParameters pubKey=
      new DSAPublicKeyParameters(new BigInteger(y),
				 new DSAParameters(new BigInteger(p),
						   new BigInteger(q),
						   new BigInteger(g)
						   ));
    signature.init(false, pubKey);
  }
  public void setPrvKey(byte[] x, byte[] p, byte[] q, byte[] g) throws Exception{
//System.out.println(this+".setPrvKey()");
    DSAPrivateKeyParameters prvKey = 
	new DSAPrivateKeyParameters(new BigInteger(x),
				 new DSAParameters(new BigInteger(p),
						   new BigInteger(q),
						   new BigInteger(g)));
    signature.init(true, prvKey);
  }
  public void update(byte[] foo) throws Exception{
//System.out.println(this+".update()"+foo.length);
    sha.update(foo, 0, foo.length);
    //signature.update(foo);
  }
  public byte[] sign() throws Exception{
//System.out.println(this+".sign()");
    BigInteger[] sig=signature.generateSignature(sha.digest());
/*
System.out.print("sign["+sig.length+"] ");
for(int i=0; i<sig.length;i++){
System.out.print(Integer.toHexString(sig[i]&0xff)+":");
}
System.out.println("");
*/
    byte[] r=sig[0].toByteArray();
    byte[] s=sig[1].toByteArray();

    byte[] result=new byte[40];

    // result must be 40 bytes, but length of r and s may not be 20 bytes  

    System.arraycopy(r, (r.length>20)?1:0,
		     result, (r.length>20)?0:20-r.length,
		     (r.length>20)?20:r.length);
    System.arraycopy(s, (s.length>20)?1:0,
		     result, (s.length>20)?20:40-s.length,
		     (s.length>20)?20:s.length);
 
//  System.arraycopy(sig, (sig[3]==20?4:5), result, 0, 20);
//  System.arraycopy(sig, sig.length-20, result, 20, 20);

    return result;
  }
  public boolean verify(byte[] sig) throws Exception{
//System.out.println(this+".verify()");

    int i=0;
    int j=0;
    if(sig[0]==0 && sig[1]==0 && sig[2]==0){
    j=((sig[i++]<<24)&0xff000000)|((sig[i++]<<16)&0x00ff0000)|
	((sig[i++]<<8)&0x0000ff00)|((sig[i++])&0x000000ff);
    i+=j;
    j=((sig[i++]<<24)&0xff000000)|((sig[i++]<<16)&0x00ff0000)|
	((sig[i++]<<8)&0x0000ff00)|((sig[i++])&0x000000ff);
    byte[] tmp=new byte[j]; 
    System.arraycopy(sig, i, tmp, 0, j); sig=tmp;
    }
//System.out.println("tmp.length="+tmp.length);
//System.out.println(Integer.toHexString(tmp[0])+", "+Integer.toHexString(tmp[20]));

    int pad=((sig[0]&0x80)!=0?1:0);
    byte[] foo=new byte[20+pad];
    System.arraycopy(sig, 0, foo, pad, 20);
    BigInteger r=new BigInteger(foo);

    pad=((sig[20]&0x80)!=0?1:0);
    foo=new byte[20+pad];
    System.arraycopy(sig, 20, foo, pad, 20);
    BigInteger s=new BigInteger(foo);
    return signature.verifySignature(sha.digest(), r, s);
  }
}
