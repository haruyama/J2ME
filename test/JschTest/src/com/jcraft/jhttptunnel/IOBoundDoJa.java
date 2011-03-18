/* -*-mode:java; c-basic-offset:2; -*- */
/*
 Copyright (c) 2005 ymnk, JCraft,Inc. All rights reserved.

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

package com.jcraft.jhttptunnel;

import java.io.*;

import javax.microedition.io.*;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;

import RemoteExecOverHttpTunnelMIDlet;

// import com.nttdocomo.io.*;

public class IOBoundDoJa {
    OutBoundDoJa obdj = null;

    InBoundDoJa ibdj = null;

    private String sessionid = null;

    public IOBoundDoJa() {
        obdj = new OutBoundDoJa();
        ibdj = new InBoundDoJa();
    }

    public OutBound getOutBound() {
        return obdj;
    }

    public InBound getInBound() {
        return ibdj;
    }

    class OutBoundDoJa extends OutBound {
        private InputStream in = null;

        private OutputStream out = null;

        private HttpConnection con = null;

        private final byte[] _TUNNEL_DISCONNECT = { (byte) 0x47 };

        private int count = -1;

        boolean connected = false;

        public void connect() throws IOException {
            // System.out.println("OutBound: connect");

            String host = getHost();
            int port = getPort();
            ++count;
            String uri = "http://" + host + ":" + port + "/index.html?crap=1"
                    + "&count=" + count;
            if (sessionid != null) {
                uri = uri + "&SESSIONID=" + sessionid;
            }
            // System.out.println("uri: "+uri);
            if (con != null) {
                con.close();
            }

            con = (HttpConnection) Connector.open(uri);
            con.setRequestMethod(HttpConnection.POST);
            sendCount = getContentLength();
        }

        public synchronized void sendData(byte[] foo, int s, int l,
                boolean flush) throws IOException {
            // System.out.println("sendData: l=" + l + " sendCount=" + sendCount
            // + "flush=" + flush);

            String debugMessage = "\nsendData()\n";

            if (foo != null && l <= 0)
                return;
            // if (l <= 0)

            // if (con == null) {
            // connect();
            // } else if (sendCount <= 0) {
            // connect();
            // }
            connect();

            try {
            if(out !=null) {
                out.close();
            }
            }catch(IOException e) {
                
            }
            out = con.openOutputStream();

            int retry = 2;
            while (retry > 0) {
                try {
                    // if (l > 0) {
                    debugMessage += "out.write() :" + "s=" + s + " l=" + l
                            + "\n";
                    
                    if (foo == null ) {
                        byte[] tmp = new byte[1];
                        
                        out.write(tmp, 0, 0);
                    }else if(l == 0) {
                        byte[] tmp = new byte[1];
                        out.write(tmp, 0, 0);
                    } else {
                        out.write(foo, s, l);

                    }

                    debugMessage += "out.write() done\n";
                    // out.write(foo);
                    // }
                    sendCount -= l;

                    int rc = con.getResponseCode();

                    debugMessage += "con.getResponseCode() done\n";
                    if (rc != HttpConnection.HTTP_OK) {
                        throw new IOException("HTTP response code: " + rc);
                    }

                    // if(flush){
                    // try{
                    // connect();
                    // // connect();
                    // }
                    // catch(IOException e) {
                    // try{con.close();}catch(Exception
                    // ee){System.out.println(e);}
                    // con=null;
                    // if(foo==null){ // data retrieve
                    // connect();
                    // retry--;
                    // continue;
                    // }
                    // ibdj.connected=false;
                    // return;
                    // }
                    in = con.openInputStream();
                    debugMessage += "con.openInputStream() done\n ";

                    debugMessage += "getSessionID(): ";
                    sessionid = con.getHeaderField("x-SESSIONID");
                    // System.out.println("sessionid: "+sessionid);
                    debugMessage += sessionid + "\n";

                    debugMessage += "readData() start";
                    readData();
                    // sendCount=0;

                    // return;
                    // }
                    // Alert alert = new Alert("sendData() end.");
                    // RemoteExecOverHttpTunnelMIDlet.getDisplay().setCurrent(
                    // alert);
                    return;
                } catch (Exception e) {
                    Alert alert = new Alert(e.toString(), e.getMessage()
                            + debugMessage, null, null);
                    RemoteExecOverHttpTunnelMIDlet.getDisplay().setCurrent(
                            alert);

                    connect();
                }
                retry--;
            }

        }

        private void readData() throws IOException {
            String debugMessage = "readData()\n";
            // System.out.println("readData: "+this+" con="+con+" in="+in);
            if (con != null) {
                if (out != null) {
                    try {
                        out.close();
                        out = null;
                    } catch (IOException e) {
                        Alert alert = new Alert(e.toString(), e.getMessage()
                                + debugMessage, null, null);
                        RemoteExecOverHttpTunnelMIDlet.getDisplay().setCurrent(
                                alert);
                    }
                }
                long datalen = con.getLength();

                debugMessage += "datalen : " + datalen + "\n";
                // System.out.println("datalen: "+datalen);
                if (in != null) {
                    try {
                        while (true) {
                            // System.out.println("now read");
                            debugMessage += "in.read()\n";

                            int c = in.read();
                            // System.out.println("c="+c);
                            debugMessage += "in.read() done\n";
                            if (c == -1)
                                break;
                            while (true) {
                                if (ibdj.space() >= 1) {
                                    break;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                }
                            }

                            debugMessage += "ibdj.push()\n";
                            ibdj.push(c);
                            debugMessage += "ibdj.push() done\n";

                        }
                        in.close();

                        debugMessage += "in.close()\n";
                        in = null;
                    } catch (IOException e) {
                        Alert alert = new Alert(e.toString(), e.getMessage()
                                + debugMessage, null, null);
                        RemoteExecOverHttpTunnelMIDlet.getDisplay().setCurrent(
                                alert);

                    }
                }
                try {
                    con.close();
                } catch (Exception e) {
                    System.out.println(e);
                    Alert alert = new Alert(e.toString(), e.getMessage(), null,
                            null);
                    RemoteExecOverHttpTunnelMIDlet.getDisplay().setCurrent(
                            alert);

                }
                con = null;
            }
            // System.out.println("close() done");
        }

        public void close() throws IOException {
            connected = false;
        }
    }

    class InBoundDoJa extends InBound {
        private InputStream in = null;

        private HttpConnection con = null;

        boolean connected = false;

        public void connect() throws IOException {
            connected = true;
        }

        public int receiveData(byte[] buf, int s, int l) throws IOException {
            // System.out.println("receiveData: buf="+buf+" l="+l);
            String debugMessage = "\nreceiveData()\n";
            try {

                if (l <= 0) {
                    return -1;
                }
                int retry = 2;
                while (connected) {
                    synchronized (this) {
                        int bl = be - bs;

                        debugMessage += "nyo1\n";
                        // System.out.println(" connected="+connected+"
                        // bl="+bl);
                        if (bl > 0) {
                            if (buf == null) {
                                if (bl > l) {
                                    bl = l;
                                }
                                bs += bl;
                                System.arraycopy(this.buf, bs, this.buf, 0, be
                                        - bs);
                                be = be - bs;
                                bs = 0;
                                if (bl == l) {
                                    return -1; // ??
                                }
                                l -= bl;
                                continue;
                            }
                            if (bl > l)
                                bl = l;

                            debugMessage += "nyo2\n";
                            System.arraycopy(this.buf, bs, buf, s, bl);
                            bs += bl;
                            System
                                    .arraycopy(this.buf, bs, this.buf, 0, be
                                            - bs);
                            be = be - bs;
                            bs = 0;
                            return bl;
                        }
                    }
                    if (retry > 0) {
                        retry--;
                        debugMessage += "nyo3\n";
                        obdj.sendData(null, 0, 0, true);
                        // obdj.readData();
                        // System.out.println("be-bs="+(be-bs));
                        if (be - bs > 0) {
                            continue;
                        }

                        /*
                         * if(blockToRead>0){ try{ Thread.sleep(blockToRead); }
                         * catch(Exception e){ } continue; } else
                         * if(blockToRead==0){ // non block return 0; } else{
                         * try{ Thread.sleep(10000); } // block foreaver
                         * catch(Exception e){ } retry=1; continue; }
                         */

                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                        }
                        continue;

                    } else {
                        return 0;
                    }
                    // return 0;
                }
            } catch (IOException e) {
                Alert alert = new Alert(e.toString(), e.getMessage()
                        + debugMessage, null, null);
                RemoteExecOverHttpTunnelMIDlet.getDisplay().setCurrent(alert);
                throw e;
            }
            return -1;
        }

        // byte[] buf=new byte[1024];
        byte[] buf = new byte[4096];

        int bs = 0;

        int be = 0;

        public synchronized int space() {
            return buf.length - be;
        }

        public synchronized void push(int c) {
            // System.out.println("push: "+c);
            buf[bs + be] = (byte) c;
            be++;
            // System.out.println(" be: "+be);
        }

        public void close() throws IOException {
            connected = false;
        }
    }
}
