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
import javaxxx.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.DHBasicAgreement;
import org.bouncycastle.crypto.generators.DHKeyPairGenerator;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;

public class DH implements com.jcraft.jsch.DH{
  private BigInteger p;
  private BigInteger g;
  private BigInteger e;  // my public key
  private byte[] e_array;
  private BigInteger f;  // your public key
  private BigInteger K;  // shared secret key
  private byte[] K_array;

  private DHBasicAgreement myKeyAgree;

  public void init() throws Exception{
    myKeyAgree=new DHBasicAgreement();
  }
  public byte[] getE() throws Exception{
//System.err.println("getE: "+e);
    if(e==null){
      DHKeyGenerationParameters params= 
	new DHKeyGenerationParameters(new SecureRandom(), 
                                      getParam()
                                      );

//System.err.println("getE: "+params);
      DHKeyPairGenerator kpgen=new DHKeyPairGenerator();
//System.err.println("getE: "+kpgen);
      kpgen.init(params);
//System.err.println("getE: ??");
long start=System.currentTimeMillis();
      AsymmetricCipherKeyPair myKpair=kpgen.generateKeyPair();
//System.err.println("getE: "+myKpair);
//System.err.println("time: "+(System.currentTimeMillis()-start));
      DHPublicKeyParameters pu=(DHPublicKeyParameters)myKpair.getPublic();
//System.err.println("getE: "+pu);
      DHPrivateKeyParameters pv=(DHPrivateKeyParameters)myKpair.getPrivate();
//System.err.println("getE: "+pv);

      myKeyAgree.init(pv);
//System.err.println("getE: ?");
      e=pu.getY();
//System.err.println("getE: "+e);
      e_array=e.toByteArray();
//System.err.println("getE: "+e_array);
    }
    return e_array;
  }
  public byte[] getK() throws Exception{
    if(K==null){
      DHPublicKeyParameters yourPubKey=
	new DHPublicKeyParameters(f, 
                                  getParam()
                                  );
      K=myKeyAgree.calculateAgreement(yourPubKey);
//    byte[] mySharedSecret=myKeyAgree.generateSecret();
//    K=new BigInteger(mySharedSecret);
      K_array=K.toByteArray();
//System.out.println("K.signum(): "+K.signum()+
//		   " "+Integer.toHexString(mySharedSecret[0]&0xff)+
//		   " "+Integer.toHexString(K_array[0]&0xff));
//    K_array=mySharedSecret;
    }
    return K_array;
  }
  public void setP(byte[] p){ setP(new BigInteger(p)); }
  public void setG(byte[] g){ setG(new BigInteger(g)); }
  public void setF(byte[] f){ setF(new BigInteger(f)); }
  private void setP(BigInteger p){this.p=p;}
  private void setG(BigInteger g){this.g=g;}
  private void setF(BigInteger f){this.f=f;}
  private DHParameters param=null;
  private DHParameters getParam(){
    if(param==null){
      //param=new DHParameters(p,g);
      param=new DHParameters(p,g, null, 160);
    }
    return param;
  }
}
