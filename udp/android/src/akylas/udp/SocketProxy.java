
package akylas.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.util.TiConvert;

import ti.modules.titanium.BufferProxy;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import android.os.AsyncTask;

import java.net.InetSocketAddress;
import java.net.MulticastSocket;

// This proxy can be created by calling Udp.createExample({message: "hello world"})
@Kroll.proxy(creatableInModule = AkylasUdpModule.class)
public class SocketProxy extends KrollProxy {

    // Standard Debugging Variables
    private static final String LCAT = "SocketProxy";

    // Private Instance Variables
    private boolean _continueListening;
    // private Thread _listeningThread;
    private MulticastSocket _socket;
    private Integer _port;
    int ref = 0;
    private int maxPacketSize = 256;
    private boolean reuseAddress = true;
    private String address = "0.0.0.0";
    // private Integer counter = 0;

    // Constructor
    public SocketProxy() {
        super();
    }

    // Handle creation options
    @Override
    public void handleCreationDict(HashMap options) {
        super.handleCreationDict(options);

        if (options.containsKey("maxPacketSize")) {
            maxPacketSize = TiConvert.toInt(options.get("maxPacketSize"));
        }
    }

    protected void eventListenerAdded(String event, int count,
            KrollProxy proxy) {
        super.eventListenerAdded(event, count, proxy);
        if (count == 1) {
            switch (event) {
            case "listening":
            case "message":
                startListening();
                break;
            }
        }

    }

    protected void eventListenerRemoved(String event, int count,
            KrollProxy proxy) {
        super.eventListenerRemoved(event, count, proxy);
        if (count == 0) {
            switch (event) {
            case "listening":
                if (!hasListeners("message")) {
                    stopListening();
                }
                break;
            case "message":
                if (!hasListeners("listening")) {
                    stopListening();
                }
                break;
            }
        }
    }

    // Start Utility Methods

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    private InetAddress getBroadcastAddress() {
        try {
            Activity activity = TiApplication.getInstance()
                    .getCurrentActivity();
            WifiManager wifi = (WifiManager) activity
                    .getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();

            // TODO: handle null (for WIFI, or DHCP)

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    // private void fireStarted() {
    // fireEvent("started", new HashMap<String, Object>());
    // }
    //
    // private void fireStopped() {
    // fireEvent("stopped", new HashMap<String, Object>());
    // }

    private void fireError(Object obj) {
        HashMap<String, Object> evt = new HashMap<String, Object>();
        if (obj instanceof Exception) {
            ((Throwable) obj).printStackTrace();
        }
        evt.put("error", obj.toString());
        // }
        fireEvent("error", evt);
    }

    // private void startListening() {
    // if (_listeningThread != null) {
    // return;
    // }
    // _continueListening = true;
    // _listeningThread = new Thread() {
    // public void run() {
    // while (_continueListening) {
    // try {
    // byte[] buf = new byte[2048];
    // DatagramPacket packet = new DatagramPacket(buf, buf.length);
    // _socket.receive(packet);
    // String receivedMsg = new String(packet.getData(), 0, packet.getLength());
    //// byte[] rawResponse = packet.getData();
    //// byte[] byteResponse = new byte[packet.getLength()];
    //// Integer[] arrayResponse = new Integer[byteResponse.length];
    //// for (int i = 0; i < byteResponse.length; i++) {
    //// byteResponse[i] = rawResponse[i];
    ////// arrayResponse[i] = new Integer(rawResponse[i] & 0xff);
    //// }
    // HashMap<String, Object> evt = new HashMap<String, Object>();
    //// evt.put("bytesData", arrayResponse);
    // evt.put("stringData", receivedMsg);
    // evt.put("address", packet.getAddress() + ":" + packet.getPort());
    // fireEvent("data", evt);
    // } catch (IOException e) {
    // if (e.getLocalizedMessage().contains("Interrupted system call")) {
    // _continueListening = false;
    // } else {
    // fireError(e);
    // }
    // }
    // }
    // }
    // };
    // _listeningThread.start();
    // }

    private void stopListening() {
        _continueListening = false;
        // _listeningThread.interrupt();
        // _listeningThread = null;
    }

    protected void fireDataReceived(byte[] data, String sdata, String address,
            int port) {
        HashMap<String, Object> evt = new HashMap<String, Object>();
        evt.put("bytesData", data);
        evt.put("stringData", sdata);
        evt.put("address", address);
        evt.put("port", port);
        fireEvent("data", evt);
    }

    // Thread------------------------------------------------------------------------------
    // private class ReceivedData
    // {
    // public byte[] bytes;
    // public int length;
    // public String sender;
    // public int port;
    // }

    public void createSocket() {
        if (_socket != null) {
            _socket.close();
            _socket = null;
        }
        try {
            _socket = new MulticastSocket(null);
            _socket.setReuseAddress(reuseAddress);
            _socket.bind(new InetSocketAddress(address, _port));
        } catch (IOException e) {
            fireError(e);
        }

    }

    private class UDPListeningThread implements Runnable {

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            boolean lostConnection = false;
            _continueListening = true;
            fireEvent("listening");

            Log.i(LCAT, "starting socket with maxPacketSize:" + maxPacketSize);
            while (_continueListening) {
                try {
                    if (lostConnection || _socket == null) {
                        createSocket();
                        lostConnection = false;
                    }
                    if (_socket != null) {
                        byte[] buf = new byte[maxPacketSize];
                        DatagramPacket packet = new DatagramPacket(buf,
                                buf.length);
                        _socket.receive(packet);
                        byte[] data = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

//                        BufferProxy buffer = new BufferProxy(packet.getData());

                        // String receivedMsg = new String(packet.getData(), 0,
                        // packet.getLength());
                        HashMap<String, Object> evt = new HashMap<String, Object>();
                        // evt.put("bytesData", buffer);
                        // evt.put("stringData", receivedMsg);
                        String address = packet.getAddress().toString();
                        if (address.charAt(0) == '/') {
                            address = address.substring(1);
                        }
                        evt.put("timestamp", (new Date()).getTime());
                        evt.put("address", address);
                        evt.put("port", packet.getPort());
                        evt.put("data", TiBlob.blobFromObject(data));
                        fireEvent("message", evt);
                        // new SendTiEvent().execute(evt);

                        // if
                        // (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                        // {
                        // new
                        // SendTiEvent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        // evt);
                        // }
                        // else {
                        // new SendTiEvent().execute(evt);
                        // }
                    }
                    // Thread.sleep(5);
                } catch (RejectedExecutionException e) {
                } catch (IOException e) {
                    fireError(e);
                    lostConnection = true;
                }
                // catch (SocketException e) {
                // fireError(e);
                // lostConnection = true;
                // }
                // catch (InterruptedException e)
                // {
                // // TODO Auto-generated catch block
                // fireError(e);
                // lostConnection = true;
                // }
            }
            fireEvent("close");
        }
    }

    // Connect to
    // Server------------------------------------------------------------------------------------
    // private class SendTiEvent
    // extends AsyncTask<HashMap<String, Object>, Void, Void> {
    //
    // @Override
    // protected Void doInBackground(HashMap<String, Object>... args) {
    // HashMap<String, Object> evt = args[0];
    // fireEvent("data", evt);
    //
    // return null;
    // }
    //
    // protected void onPostExecute(Void result) {
    // }
    // }

    // Connect to
    // Server------------------------------------------------------------------------------------
    private class ConnectToServer extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... arg0) {
            return null;
        }

        protected void onPostExecute(Void result) {
            (new Thread(new UDPListeningThread())).start();
        }

    }

    // End Utility Methods

    // Start Public API

    public void startListening() {
        // KrollDict args = new KrollDict(hm);
        if (_socket == null) {
            fireError("socket must be bound first");
            return;
        }
        if (_continueListening) {
            return;
        }
        new ConnectToServer().execute("");
    }

    @Kroll.method
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public KrollDict address() {
        if (_socket != null) {
            KrollDict args = new KrollDict();
            args.put("port", _port);
            args.put("address", address);
            return args;
        }
        return null;
    }

    @Kroll.method
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void bind(int port, @Kroll.argument(optional = true) String address,
            @Kroll.argument(optional = true) KrollFunction callback) {
        KrollDict args = new KrollDict();
        args.put("port", port);
        if (address != null && !address.equals("undefined")) {
            args.put("address", address);
        }
        if (callback != null) {
            args.put("listening", callback);
        }
        bind(args);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void bind(HashMap hm) {
        if (_socket != null) {
            fireError(
                    "Socket already started! Explicitly call stop() before attempting to start it again!");
            return;
        }
        if (hm.containsKey("listening")) {
            addEventListener("listening", hm.get("listening"));
        }

        _port = TiConvert.toInt(hm, "port");
        reuseAddress = TiConvert.toBoolean(hm, "reuseAddress", true);
        address = TiConvert.toString(hm, "address", "0.0.0.0");
        createSocket();
        if (_socket != null
                && (hasListeners("listening") || hasListeners("message"))) {
            startListening();
        }
    }

    // TiStream interface methods
    @Kroll.method
    public int read(Object args[]) throws IOException {
        if (_socket == null) {
            fireError("Unable to read from socket, not connected");
        }

        BufferProxy bufferProxy = null;
        int offset = 0;
        int length = 0;

        if (args.length == 1 || args.length == 3) {
            if (args.length > 0) {
                if (args[0] instanceof BufferProxy) {
                    bufferProxy = (BufferProxy) args[0];
                    length = bufferProxy.getLength();

                } else {
                    throw new IllegalArgumentException(
                            "Invalid buffer argument");
                }
            }

            if (args.length == 3) {
                if (args[1] instanceof Integer) {
                    offset = ((Integer) args[1]).intValue();

                } else if (args[1] instanceof Double) {
                    offset = ((Double) args[1]).intValue();

                } else {
                    throw new IllegalArgumentException(
                            "Invalid offset argument");
                }

                if (args[2] instanceof Integer) {
                    length = ((Integer) args[2]).intValue();

                } else if (args[2] instanceof Double) {
                    length = ((Double) args[2]).intValue();

                } else {
                    throw new IllegalArgumentException(
                            "Invalid length argument");
                }
            }

        } else {
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        byte[] buf = new byte[maxPacketSize];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        _socket.receive(packet);

        return bufferProxy.write(offset, packet.getData(), 0,
                packet.getLength());
    }


    
    @Kroll.method
    public void send(Object msg,
            @Kroll.argument(optional = true) Integer portArg,
            @Kroll.argument(optional = true) String addressArg 
            ) {
        try {
            if (_socket == null) {
                fireError(
                        "Cannot send data before the socket is started");
                return;
            }
            byte[] bytes = TiConvert.toBytes(msg);
            if (bytes != null) {
                int port = (portArg != null)? portArg : _port;
                InetAddress address = addressArg != null ? InetAddress.getByName(addressArg)
                        : getBroadcastAddress();
                _socket.send(
                        new DatagramPacket(bytes, bytes.length, address, port));
            } else {
                throw new Exception("nothing to send");
            }

            
            // Log.i(LCAT, "Data Sent!");
        } catch (Exception e) {
            fireError(e);
        }
    }

    @Kroll.method
    public void stop() {
        if (_socket != null) {
            stopListening();
            _socket.close();
            _socket = null;
            Log.i(LCAT, "Stopped!");
        }
    }

    @Kroll.method
    public void close() {
        stop();
    }

    @Kroll.method
    @Kroll.setProperty
    public void setBroadcast(boolean value) {
        if (_socket != null) {
            try {
                _socket.setBroadcast(value);
            } catch (SocketException e) {
                fireError(e);
            }
        }
    }

    @Kroll.method
    @Kroll.setProperty
    public void setReuseAddress(boolean value) {
        if (_socket != null) {
            try {
                _socket.setReuseAddress(value);
            } catch (SocketException e) {
                fireError(e);
            }
        }
    }

    @Kroll.method
    @Kroll.setProperty
    public void setTimeout(int value) {
        if (_socket != null) {
            try {
                _socket.setSoTimeout(value);
            } catch (SocketException e) {
                fireError(e);
            }
        }
    }
    
    @Kroll.method
    public void addMembership(String address) {
        if (_socket != null) {
            try {
                InetAddress group = InetAddress.getByName(address);
                _socket.joinGroup(group);
            } catch (IOException e) {
                fireError(e);
            }
        }
    }
}
