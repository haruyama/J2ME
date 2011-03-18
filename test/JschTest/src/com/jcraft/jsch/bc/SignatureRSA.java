/* -*-mode:java; c-basic-offset:2; -*- */
/*
Copyright (c) 2002,2003 ymnk, JCraft,Inc. All rights reserved.

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

import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;

/*
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.ASN1Sequence;
//import org.bouncycastle.crypto.AsymmetricBlockCipher;
//import org.bouncycastle.crypto.Digest;
import org.bouncycastle.asn1.DERInputStream;
import java.io.ByteArrayInputStream;
*/
/*
import java.security.SignatureException;
import java.io.IOException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import java.io.ByteArrayOutputStream;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.DigestInfo;
*/

public class SignatureRSA implements com.jcraft.jsch.SignatureRSA{

  private SHA1Digest digest;
  private RSAEngine cipher;

  public void init() throws Exception{
    digest=new SHA1Digest();
    cipher=new RSAEngine();
  }     
  public void setPubKey(byte[] e, byte[] n) throws Exception{
    RSAKeyParameters pubKey=
      new RSAKeyParameters(false, new BigInteger(n), new BigInteger(e));
    digest.reset();
    cipher.init(false, pubKey);
  }
  public void setPrvKey(byte[] d, byte[] n) throws Exception{
    RSAKeyParameters prvKey=
      new RSAKeyParameters(true, new BigInteger(n), new BigInteger(d));
    digest.reset();
    cipher.init(true, prvKey);
  }

  public void update(byte[] foo) throws Exception{
//    System.out.println("update: "+foo);
//    for(int k=0; k<foo.length; k++){
//      System.out.print(Integer.toHexString(foo[k]&0xff)+" ");
//    }
//    System.out.println("");
    digest.update(foo, 0, foo.length);
  }
  public byte[] sign() throws Exception{
    byte[] hash=new byte[digest.getDigestSize()];
    digest.doFinal(hash, 0);

    byte[] block=new byte[cipher.getInputBlockSize()];
    block[0]=1;
    int j=hash.length+2+9+2+2+1;
    for(int i=1; i<block.length-j; i++){
      block[i]=(byte)0xff;
    }

    j=block.length-j;
    block[j++]=0;

    // DigestInfo::=SEQUENCE{
    //                digestAlgorithm  AlgorithmIdentifier,
    //                digest OCTET STRING }
    //
    // AlgorithmIdentifier ::= SEQUENCE {
    //                           algorithm OBJECT IDENTIFIER,
    //                           parameters ANY DEFINED BY algorithm OPTIONAL }

    block[j++]=(byte)0x30;
    block[j++]=(byte)0x21;  // 2+9+2+hash.length

    block[j++]=(byte)0x30;
    block[j++]=(byte)0x09;
    block[j++]=(byte)0x06;
    block[j++]=(byte)0x05;
    block[j++]=(byte)0x2b;
    block[j++]=(byte)0x0e;
    block[j++]=(byte)0x03;
    block[j++]=(byte)0x02;
    block[j++]=(byte)0x1a;
    block[j++]=(byte)0x05;
    block[j++]=(byte)0x00;

    block[j++]=(byte)0x04;  // OCTET STRING
    block[j++]=(byte)0x14;  // hash.length

    System.arraycopy(hash, 0, block, j, hash.length);

//    System.out.println("sign: ");
//    for(int k=0; k<block.length; k++){
//      System.out.print(Integer.toHexString(block[k]&0xff)+" ");
//    }
//    System.out.println("");

    try{
      return cipher.processBlock(block, 0, block.length);
    }
//    catch (ArrayIndexOutOfBoundsException e){
//      throw new SignatureException("key too small for signature type");
//    }
    catch (Exception e){
//      throw new SignatureException(e.toString());
      throw e;
    }
  }
  public boolean verify(byte[] sig) throws Exception{
    int i=0;
    int j=0;

//    System.out.println("verify: "+sig.length);
//    for(int k=0; k<sig.length; k++){
//      System.out.print(Integer.toHexString(sig[k]&0xff)+" ");
//    }

//    if(sig[0]==0 && sig[1]==0 && sig[2]==0){
      j=((sig[i++]<<24)&0xff000000)|((sig[i++]<<16)&0x00ff0000)|
        ((sig[i++]<<8)&0x0000ff00)|((sig[i++])&0x000000ff);
      i+=j;
      j=((sig[i++]<<24)&0xff000000)|((sig[i++]<<16)&0x00ff0000)|
        ((sig[i++]<<8)&0x0000ff00)|((sig[i++])&0x000000ff);
      byte[] tmp=new byte[j];
      System.arraycopy(sig, i, tmp, 0, j); sig=tmp;
//    }

    byte[]  hash = new byte[digest.getDigestSize()];
    digest.doFinal(hash, 0);

//System.out.println("hash: ");
//for(int k=0; k<hash.length; k++){
//  System.out.print(Integer.toHexString(hash[k]&0xff)+" ");
//}
//System.out.println("");

    try{
      sig=cipher.processBlock(sig, 0, sig.length);
    }
    catch (Exception e){
      return false;
    }

//System.out.println("sig: ");
//for(int k=0; k<sig.length; k++){
//  System.out.print(Integer.toHexString(sig[k]&0xff)+" ");
//}
//System.out.println("");

    //  It seems sig is in PKCS#1 format.
    //  Refer to RFC 2437: PKCS#1:RSA Cryptography Specifications Version 2.0

    if(sig[0]!=1 && sig[0]!=2){
      // unknown block type
      return false;
    }

    // find message block
    for(i=1; i!=sig.length; i++){
      if(sig[i]==0){
	break;
      }
    }
    i++;

    if(i>=sig.length|| i<10){ // HEADER_LENGTH=10
      // no data in block
      return false;
    }

    /*   
      // DigestInfo::=SEQUENCE{
      //                digestAlgorithm  AlgorithmIdentifier,
      //                digest OCTET STRING }
      //
      // AlgorithmIdentifier ::= SEQUENCE {
      //                           algorithm OBJECT IDENTIFIER,
      //                           parameters ANY DEFINED BY algorithm OPTIONAL }
 
      if(sig[i]!=0x30 && sig[i+1]!=sig.length-(i+2)){    
	return false;
      }
      i+=2;

      if(sig[i++]!=0x30){
	return false;
      }
      i+=(sig[i]+1);

      if(sig[i++]!=0x04){           // OCTET_STRING
	return false;
      }
      if(i+sig[i]+1 != sig.length){
	return false;
      }
      i++;
    */

    if(sig.length<hash.length){
      return false;
    }
    i=sig.length-hash.length;


    if(sig.length-i!=hash.length){
      return false;
    }

    for(j=0; j<hash.length; j++){
      if(sig[i++]!=hash[j]){
	return false;
      }
    }

    return true;
  }
}
