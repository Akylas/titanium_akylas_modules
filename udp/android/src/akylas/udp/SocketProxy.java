
package akylas.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.RejectedExecutionException;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import ti.modules.titanium.BufferProxy;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import android.os.AsyncTask;
import android.os.Build;

import java.net.InetSocketAddress;

// This proxy can be created by calling Udp.createExample({message: "hello world"})
@Kroll.proxy(creatableInModule = AkylasUdpModule.class)
public class SocketProxy extends KrollProxy {

	// Standard Debugging Variables
	private static final String LCAT = "SocketProxy";

	// Private Instance Variables
	private boolean _continueListening;
//	private Thread _listeningThread;
	private DatagramSocket _socket;
	private Integer _port;
	int ref = 0;
	private int maxPacketSize = 256;
	
//	private Integer counter = 0;

	// Constructor
	public SocketProxy(TiContext tiContext) {
		super(tiContext);
	}
	
	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options)
	{
		super.handleCreationDict(options);
		
		if (options.containsKey("maxPacketSize")) {
			maxPacketSize = TiConvert.toInt(options.get("maxPacketSize"));
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
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
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
			Activity activity = TiApplication.getInstance().getCurrentActivity();
			WifiManager wifi = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
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

	private void fireStarted() {
		fireEvent("started", new HashMap<String, Object>());
	}
	
	private void fireStopped() {
		fireEvent("stopped", new HashMap<String, Object>());
	}

	private void fireError(Object obj) {
		HashMap<String, Object> evt = new HashMap<String, Object>();
		evt.put("error", obj);
		fireEvent("error", evt);
	}

//	private void startListening() {
//		if (_listeningThread != null) {
//			return;
//		}
//		_continueListening = true;
//		_listeningThread = new Thread() {
//			public void run() {
//				while (_continueListening) {
//					try {
//						byte[] buf = new byte[2048];
//						DatagramPacket packet = new DatagramPacket(buf, buf.length);
//						_socket.receive(packet);
//						String receivedMsg = new String(packet.getData(), 0, packet.getLength());
////						byte[] rawResponse = packet.getData();
////						byte[] byteResponse = new byte[packet.getLength()];
////						Integer[] arrayResponse = new Integer[byteResponse.length];
////						for (int i = 0; i < byteResponse.length; i++) {
////							byteResponse[i] = rawResponse[i];
//////							arrayResponse[i] = new Integer(rawResponse[i] & 0xff);
////						}
//						HashMap<String, Object> evt = new HashMap<String, Object>();
////						evt.put("bytesData", arrayResponse);
//						evt.put("stringData", receivedMsg);
//						evt.put("address", packet.getAddress() + ":" + packet.getPort());
//						fireEvent("data", evt);
//					} catch (IOException e) {
//						if (e.getLocalizedMessage().contains("Interrupted system call")) {
//							_continueListening = false;
//						} else {
//							fireError(e);
//						}
//					}
//				}
//			}
//		};
//		_listeningThread.start();
//	}

	private void stopListening() {
		_continueListening = false;
//		_listeningThread.interrupt();
//		_listeningThread = null;
	}
	
	
	protected void fireDataReceived(byte[] data, String sdata, String address, int port)
	{
		HashMap<String, Object> evt = new HashMap<String, Object>();
		evt.put("bytesData", data);
		evt.put("stringData", sdata);
		evt.put("address", address);
		evt.put("port", port);
		fireEvent("data", evt);
	}
	
	// Thread------------------------------------------------------------------------------
//	private class ReceivedData
//	{
//	    public byte[] bytes;
//	    public int length;
//	    public String sender;
//	    public int port;
//	}
	
	private class UDPListeningThread implements Runnable
	{
		
		
		public void createSocket() throws SocketException
		{
			if (_socket != null)
			{
				_socket.close();
				_socket = null;
			}
			_socket = new DatagramSocket(null);
			_socket.setReuseAddress(true);
			_socket.bind(new InetSocketAddress("0.0.0.0", _port));
		}
		@SuppressWarnings("unchecked")
		@Override
		public void run()
		{
			boolean lostConnection = false;
			_continueListening = true;
//			DatagramPacket wPacket = null;
//		    byte[] wBuffer = null;
		    
			Log.i(LCAT, "starting socket with maxPacketSize:" + maxPacketSize);
//		    wBuffer = new byte[ maxPacketSize ];
//            wPacket = new DatagramPacket( wBuffer, wBuffer.length );
			while (_continueListening)
			{
				try
				{
					if (lostConnection || _socket == null)
					{
						createSocket();
						lostConnection = false;
						fireStarted();
					}
					if(_socket != null)
					{
						byte[] buf = new byte[maxPacketSize];
						DatagramPacket packet = new DatagramPacket(buf, buf.length);
						_socket.receive( packet );
						
						BufferProxy buffer = new BufferProxy(packet.getData());
						String receivedMsg = new String(packet.getData(), 0, packet.getLength());
	    				HashMap<String, Object> evt = new HashMap<String, Object>();
	    				evt.put("bytesData", buffer);
	    				evt.put("stringData", receivedMsg);
	    				evt.put("address", packet.getAddress().toString());
	    				evt.put("port", packet.getPort());
//						new SendTiEvent().execute(evt);
	    				
						if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
							new SendTiEvent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, evt);
						}
						else {
							new SendTiEvent().execute(evt);
						}
					}
//					Thread.sleep(5);
				}
				catch(RejectedExecutionException e){}
				catch (IOException e)
				{
					e.printStackTrace();
					fireError(e.toString());
					lostConnection = true;
				} 
//				catch (SocketException e) {
//					fireError(e);
//					lostConnection = true;
//				}
//				catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					fireError(e);
//					lostConnection = true;
//				}
			}
			fireStopped();
		}
	}
	
	// Connect to Server------------------------------------------------------------------------------------
	private class SendTiEvent extends AsyncTask<HashMap<String, Object>, Void, Void>
	{

		@Override
		protected Void doInBackground(HashMap<String, Object>... args)
		{
			HashMap<String, Object> evt = args[0];
			fireEvent("data", evt);

			return null;
		}

		protected void onPostExecute(Void result)
		{
		}
	}
	// Connect to Server------------------------------------------------------------------------------------
	private class ConnectToServer extends AsyncTask<String, Void, Void>
	{

		@Override
		protected Void doInBackground(String... arg0)
		{
			return null;
		}

		protected void onPostExecute(Void result)
		{
			(new Thread(new UDPListeningThread())).start();
		}

	}

	// End Utility Methods

	// Start Public API

	@Kroll.method
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void start(HashMap hm) {
		KrollDict args = new KrollDict(hm);
		
			if (_socket != null) {
				fireError("Socket already started! Explicitly call stop() before attempting to start it again!");
				return;
			}
			_port = args.getInt("port");
//			startListening();
			new ConnectToServer().execute("");

			fireStarted();
			Log.i(LCAT, "Socket Started!");
	}

	@Kroll.method
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void sendString(HashMap hm) {
		KrollDict args = new KrollDict(hm);
		try {
			if (_socket == null) {
				fireError("Cannot send data before the socket is started as a client or server!");
				return;
			}
			String data = args.getString("data");
			byte[] bytes = data.getBytes();

			String host = args.getString("host");
			int port = args.optInt("port", _port);
			InetAddress _address = host != null ? InetAddress.getByName(host) : getBroadcastAddress();
			_socket.send(new DatagramPacket(bytes, bytes.length, _address, port));
//			Log.i(LCAT, "Data Sent!");
		} catch (IOException e) {
			fireError(e.toString());
		}
	}

	@Kroll.method
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void sendBytes(HashMap hm) {
		KrollDict args = new KrollDict(hm);
		try {
			if (_socket == null) {
				fireError("Cannot send data before the socket is started as a client or server!");
				return;
			}
			byte[] bytes;
			Object data = args.get("data");
			if (data instanceof BufferProxy) {
				bytes = ((BufferProxy)data).getBuffer();
			}
			else
			{
				Object[] array =  (Object[])data;
				bytes = new byte[array.length];
				for (int i = 0; i < bytes.length; i++) {
					bytes[i] = (byte) TiConvert.toInt(array[i]);
				}
			}

			String host = args.getString("host");
			int port = args.optInt("port", _port);
			InetAddress _address = host != null ? InetAddress.getByName(host) : getBroadcastAddress();
			_socket.send(new DatagramPacket(bytes, bytes.length, _address, port));
//			Log.i(LCAT, "Data Sent!");
		} catch (IOException e) {
			fireError(e.toString());
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

	// End Public API

}
