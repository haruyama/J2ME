/* -*-mode:java; c-basic-offset:2; -*- */

import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javaxxx.io.*;

import com.jcraft.jsch.*;
import com.jcraft.jhttptunnel.*;

public class RemoteExecOverHttpTunnelMIDlet extends MIDlet implements Runnable,
        CommandListener {

    private static Display display;

    private Form f;

    private StringItem si;

    private TextField tf;

    private TextField pf;

    private TextField cf;

    private Command exitCommand = new Command("Exit", Command.EXIT, 0);

    private Command execCommand = new Command("Exec", Command.ITEM, 1);

    private Command cancelCommand = new Command("Cancel", Command.SCREEN, 0);

    private Command okCommand = new Command("Ok", Command.SCREEN, 1);

    private TextBox resultBox = new TextBox("Result", "", 1024, 0);

    private TextBox tb = new TextBox("Prompt", "", 64, 0);

    private Displayable nextScreen = null;

    private String host = null;

    private String user = null;

    private String command = null;

    private boolean isPaused;

    private final String UH = "";

    private final String PASSWD = "";

    private final String COMMAND = "/bin/date";

    private static RemoteExecOverHttpTunnelMIDlet remoteExecOverHttpTunnelMIDlet = null;

    public RemoteExecOverHttpTunnelMIDlet() {
        display = Display.getDisplay(this);
        remoteExecOverHttpTunnelMIDlet = this;

        f = new Form("Remote Execution on SSH2 over HttpTunnel");
        si = new StringItem("Status", "No connection");
        tf = new TextField("user@hostname", UH, 256, TextField.ANY);
        pf = new TextField("Password", PASSWD, 256, TextField.PASSWORD);
        cf = new TextField("Command", COMMAND, 256, TextField.ANY);
        f.append(si);
        f.append(tf);
        f.append(pf);
        f.append(cf);
        f
                .append("Confirm that 'hts -F 127.0.0.1:22' is running on the remote host.");
        f.addCommand(exitCommand);
        f.addCommand(execCommand);
        f.setCommandListener(this);
        display.setCurrent(f);
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            if (host != null && user != null) {
                connect();
            }
            if (session == null) {
                si.setText("No connection");
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void startApp() {
        isPaused = false;
    }

    public void pauseApp() {
        isPaused = true;
    }

    public void destroyApp(boolean unconditional) {
        stop();
    }

    public void stop() {
        try {
            if (session != null) {
                session.disconnect();
                session = null;
            }
        } catch (Exception e) {
        }
        thread = null;
    }

    private Thread thread = null;

    public void commandAction(Command c, Displayable s) {
        if (c == okCommand && display.getCurrent() == resultBox) {
            if (nextScreen == f) {
                f.addCommand(execCommand);
            }
            display.setCurrent(nextScreen);
            return;
        }
        if (c == okCommand || c == cancelCommand) {
            if (c == cancelCommand) {
                f.addCommand(execCommand);
            }
            display.setCurrent(f);
        }
        if (c == execCommand && !isPaused()) {
            if (thread != null && thread.isAlive()) {
                return;
            }
            String _host = tf.getString();
            String _user = _host.substring(0, _host.indexOf('@'));
            _host = _host.substring(_host.indexOf('@') + 1);
            if (_host == null || _host.length() == 0 || _user == null
                    || _user.length() == 0) {
                return;
            }
            host = _host;
            user = _user;
            thread = new Thread(this);
            thread.start();
            f.removeCommand(execCommand);
        }

        if ((c == Alert.DISMISS_COMMAND) || (c == exitCommand)) {
            stop();
            notifyDestroyed();
            destroyApp(true);
        }
    }

    public class MyUserInfo implements UserInfo, CommandListener {
        Alert prompt = new Alert("Prompt", "", null, AlertType.WARNING);

        boolean result = false;

        String passwd = "";

        MyUserInfo(String passwd) {
            this.passwd = passwd;
            prompt.setTimeout(Alert.FOREVER);
            prompt.setCommandListener(this);
        }

        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            prompt.setString(str);
            prompt.addCommand(okCommand);
            prompt.addCommand(cancelCommand);
            Displayable current = display.getCurrent();
            display.setCurrent(prompt, current);
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                if (prompt != display.getCurrent()) {
                    break;
                }
            }
            prompt.removeCommand(okCommand);
            prompt.removeCommand(cancelCommand);
            return result;
        }

        String passphrase = "";

        public String getPassphrase() {
            return passphrase;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
            prompt.setString(message);
            Displayable current = display.getCurrent();
            display.setCurrent(prompt, current);
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                if (prompt != display.getCurrent()) {
                    break;
                }
            }
        }

        public void commandAction(Command c, Displayable s) {
            if (c == okCommand) {
                result = true;
            }
            if (c == cancelCommand) {
                result = false;
            }
            display.setCurrent(f);
        }
    }

    private void connect() {
        si.setText("Connecting to " + user + "@" + host + "...");
        nextScreen = display.getCurrent();
        try {
            Session session = null;
            try {
                session = getSession(user, host);
            } catch (JSchException ee) {
                // System.out.println(ee);
                si.setText("No connection");
                return;
            }

            si.setText("Connected to " + user + "@" + host);

            resultBox.delete(0, resultBox.size());
            resultBox.setTitle(last_uh + ": " + cf.getString());
            resultBox.addCommand(okCommand);
            resultBox.setCommandListener(this);
            display.setCurrent(resultBox);

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(cf.getString());
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            channel.connect();

            int c;
            while (true) {
                if (thread == null) {
                    break;
                }
                c = in.read();
                if (c == -1)
                    break;
                if (c == 0x0a) {
                    // System.out.println("");
                    resultBox.insert("\n", resultBox.size());
                } else {
                    // System.out.print(new Character((char)c));
                    resultBox.insert(new Character((char) c).toString(),
                            resultBox.size());
                }
            }
            channel.disconnect();
        } catch (Exception e) {
            // System.err.println(e);
        }
    }

    private JSch jsch = null;

    private Session session = null;

    private String last_uh = "";

    private Session getSession(String user, String host) throws JSchException {

        if (jsch == null) {
            jsch = new JSch();
            // ByteArrayInputStream bis=
            // new ByteArrayInputStream(known_hosts.getBytes());
            // jsch.setKnownHosts(bis);
            // jsch.addIdentity(new IdentityMem(id_rsa, id_rsa_pub, jsch));
            // jsch.addIdentity(new IdentityMem(id_dsa, id_dsa_pub, jsch));
        }

        String foo = user + "@" + host;
        if (last_uh.equals(foo) && session != null) {
            return session;
        }
        if (session != null) {
            try {
                session.disconnect();
            } catch (Exception e) {
            }
            session = null;
        }
        Session _session = jsch.getSession(user, host, 8888);
        _session.setUserInfo(new MyUserInfo(pf.getString()));
        _session.setSocketFactory(new SocketFactory()
        {
            InputStream in = null;

            OutputStream out = null;

            JHttpTunnelClient jhtc = null;

            public Object createSocket(String host, int port)
                    throws IOException {
                // System.out.println("create: "+host+", "+port);
                jhtc = new JHttpTunnelClient(host, port);

                IOBoundDoJa iobd = new IOBoundDoJa();
                jhtc.setInBound(iobd.getInBound());
                jhtc.setOutBound(iobd.getOutBound());

                // jhtc.setInBound(new InBoundConnector());
                // jhtc.setOutBound(new OutBoundConnector());
                // jhtc.setProxy(proxy_host, proxy_port);
                jhtc.connect();
                return null;
            }

            public InputStream getInputStream(Object socket) throws IOException {
                if (in == null)
                    in = jhtc.getInputStream();
                return in;
            }

            public OutputStream getOutputStream(Object socket)
                    throws IOException {
                if (out == null)
                    out = jhtc.getOutputStream();
                return out;
            }
        });

        _session.connect();
        last_uh = foo;
        session = _session;
        return session;
    }

    public static Display getDisplay() {
        return display;
    }

}
