/* -*-mode:java; c-basic-offset:2; -*- */
/*
Copyright (c) 2002,2003,2004 ymnk, JCraft,Inc. All rights reserved.

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

package com.jcraft.jsch;
import javaxxx.io.*;

import java.io.*;


public class PipedStream{
  private final PipedInputStream pis=new PipedInputStream();
  private final PipedOutputStream pos=new PipedOutputStream();
  public PipedStream(){ }
  public InputStream getInputStream(){ return pis; }
  public OutputStream getOutputStream(){ return pos; }

//protected static final int PIPE_SIZE=1024;
  protected static final int PIPE_SIZE=8192;
  protected byte buffer[]=new byte[PIPE_SIZE];

  /*public*/ class PipedInputStream extends InputStream{
    private boolean closedw=false;
    private boolean closedr=false;

    private Thread rThread;
    private Thread wThread;

    // full: in==out
    // empty: in==-1
    // writing 1 byte: in++;
    // reading 1 byte: out++;
    protected int in=-1;
    protected int out=0;

    PipedInputStream(){ }

    private byte[] receive1=new byte[1];
    protected synchronized void receive(int b) throws IOException{
      receive1[0]=(byte)b;
      receive(receive1, 0, 1);
    }
    synchronized void receive(byte b[], int off, int len)  throws IOException{
      if(closedw||closedr){ throw new IOException("pipe is closed"); } 
      else if(rThread!=null&&!rThread.isAlive()){
	throw new IOException("read thread is dead.");
      }
      wThread=Thread.currentThread();
      while(len>0){
	while(in==out){                // full
	  if((rThread!=null) && !rThread.isAlive()){
	    throw new IOException("reader thread is dead.");
	  }
	  notifyAll();
	  try{ wait(1000); } 
	  catch (InterruptedException ex){
	    throw new java.io.InterruptedIOException();
	  }
	}
	if(in<0){                    // empty
	  in=0; out=0;
	}
	if(in<out){
	  int foo=out-in;
	  if(foo>len)foo=len;
	  System.arraycopy(b, off, buffer, in, foo);
	  len-=foo;
	  off+=foo;
	  in+=foo;
	}
	else{
	  int foo=out+buffer.length-in;
	  if(foo>len)foo=len;
	  int bar=foo;
	  if(bar>(buffer.length-in))bar=buffer.length-in;
	  System.arraycopy(b, off, buffer, in, bar);
	  len-=foo;
	  foo-=bar;
	  off+=bar;
	  in+=bar;
	  if(in>=buffer.length){
	    in=0;
	  }
	  if(foo>0){
	    System.arraycopy(b, off, buffer, in, foo);
	    off+=foo;
	    in+=foo;
	  }
	}
      }
//System.out.println("end of reive: in="+in+", out="+out);
    }

    synchronized void receivedLast(){
      closedw=true;
      notifyAll();
    }

    private byte[] read1=new byte[1];
    public int read() throws IOException{
      int foo=read(read1, 0, 1);
      if(foo<0) return -1;
      return read1[0]&0xff;
    }

    private int getAvailable()  throws IOException{
      if(closedr){
	throw new IOException("pipe is closed.");
      } else if(wThread != null && !wThread.isAlive()
		&& !closedw && (in<0)){
	throw new IOException("write thread is dead.");
      }

      int trials=2;
//System.out.println("getAvailable: in="+in+", out="+out);
      while(in<0){
	if(closedw){ return -1; }
	if((wThread!=null) &&
	   (!wThread.isAlive())&&(--trials < 0)){
	  throw new IOException("write thread is dead.");
	}
	notifyAll();
	try{ wait(1000); } 
	catch(InterruptedException ex){
	  throw new java.io.InterruptedIOException();
	}
      }
      return available();
    }
    public synchronized int available() throws IOException{
      if(in<0){ return 0; }
      else if(in>out){ return in-out; }
      //else if(in==out){ return buffer.length; }
      else{ return in+buffer.length-out; }
    }

    public synchronized int read(byte b[], int off, int len)  throws IOException{
      if(b==null){ throw new NullPointerException(); } 
      else if((off<0)||(off>b.length)||(len<0)||
	      ((off+len)>b.length)||((off + len)<0)){
	throw new IndexOutOfBoundsException();
      } 
      else if(len==0){ return 0; }

      rThread=Thread.currentThread();

//System.out.println("in="+in+", out="+out);

      int available=getAvailable();
      if(available<0){
	return -1;
      }
      if(available>len) available=len;
//System.out.print("out: "+out+"->");
      if(out+available>buffer.length){
	System.arraycopy(buffer, out, b, off, buffer.length-out);
	System.arraycopy(buffer, 0, b, off+buffer.length-out, available-(buffer.length-out));
	out=available-(buffer.length-out);
      }
      else{
	System.arraycopy(buffer, out, b, off, available);
	out+=available;
      }
      if(out==buffer.length) out=0;
//System.out.println(out);
      if(in==out){
	in=-1;
	out=0;
      }
//System.out.println("avaialble: "+available);
      return available;
    }

    public void close() throws IOException{
      in=-1;
      closedr=true;
    }
  }

  /*public*/ class PipedOutputStream extends OutputStream{
    PipedOutputStream(){ }
    private byte[] write1=new byte[1];
    public void write(int b) throws IOException {
      write1[0]=(byte)b;
      write(write1, 0, 1);
    }
    public void write(byte b[], int off, int len)throws IOException {
      if(b==null){ throw new NullPointerException(); } 
      else if((off<0)||(off>b.length)||(len<0)||
	      ((off+len)>b.length)||((off+len)<0)){
	throw new IndexOutOfBoundsException();
      } 
      else if(len==0){ return; } 
      pis.receive(b, off, len);
    }
    public synchronized void flush()throws IOException {
      synchronized(pis){
	pis.notifyAll();
      }
    }
    public void close() throws IOException {
      pis.receivedLast();
    }
  }
}
