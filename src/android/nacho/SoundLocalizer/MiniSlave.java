package android.nacho.SoundLocalizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;


public class MiniSlave {
	
	private Context Context;
	
	private InetAddress ServerAddress;
	private Integer ServerPort;
	private Integer MiniSlavePort;
	private String ClientName;
	
	private TCPConnectionThread tcpConnectionThread;
	
	private static final String CLIENT_HELLO = "CHLO";
	private static final String CLIENT_GOODBYE = "CBYE";
	
	
	public MiniSlave(Context context) {
		
		this.Context = context;
		
	}
	
	public void StartMiniSlave(/*final String name*/) {
		
		//This is the address of the server, it may change frequently
		try {
			ServerAddress = InetAddress.getByName("192.52.24.204");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerPort=2022;
		MiniSlavePort=2012;
		
		System.out.println("Estamos en StartMiniSlave");
		
		//startClient("First Client");
		tcpConnectionThread = new TCPConnectionThread(ServerAddress, ServerPort);
		tcpConnectionThread.start();						

	}
	
	public void startClient(final String name) {
		
		System.out.println("Estamos en StartClient");
		
		//ServerAddress = null;
		this.ClientName = name;
		new Thread(new Runnable() {
			//@Override
			public void run() {
				//while(ServerAddress != null) {
					try {
						sendHello(name);
					} catch (SocketException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try{Thread.sleep(1000);}catch(InterruptedException ex){}
				//}
			}
		}).start();
	}
	
	public void sendHello(String name) throws IOException {
		
		System.out.println("Entramos en sendHello");
		
		String msg = CLIENT_HELLO+/*shortToString((short)name.length())+*/name;
		
		System.out.println("Lo que se está almacenando en el datagrama es: "+msg);
		
		DatagramPacket p = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ServerAddress /*getBroadcastAddress()*/, ServerPort);
		DatagramSocket s = new DatagramSocket(MiniSlavePort);
		
		//s.setBroadcast(true); //No broadcast at this moment
		
		//InetAddress localAddress = getLocalIpAddress();
		
		/*
		 *Esta es otra forma de obtener la dirección IP que me ha dado buenos resultados
		 * 
		WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
   
        LocalIP=Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
    	System.out.println("La Local IP se declara en este punto: "+LocalIP);
		 * 
		 * 
		 */
		
		
		try {
			do {
				s.send(p);
				byte[] buf = new byte[1024];
				p = new DatagramPacket(buf, buf.length);
				s.setSoTimeout(2000);
				System.out.println("En teoría se ha enviado y ahora queda esperando a ver que nos llega");
				
				s.receive(p);

				//if (!p.getAddress().equals(localAddress)) //This is not neccesary for this experiment because no broadcast sent
					
				evaluateUDPServerAnswer(p);
				
			} while (!new String(p.getData()).startsWith("CWLC"));
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		s.close();
	} 
	
	
	public void evaluateUDPServerAnswer(DatagramPacket packet) {
		//this.serverAddress = packet.getAddress();  We should already know the address of the server
		evaluateServerAnswer(packet.getData());
		
		System.out.println("Se prodecerá a evaluar la respuesta");
		
	}
	
	
	public void evaluateServerAnswer(byte[] byteData) {
		String data = new String(byteData);
		System.out.println("Hemos recibido: "+data);
		
		/*
		if (data.startsWith(CLIENT_WELCOME)) {
			int offset = 4;
			short lServerName = stringToShort(byteData, offset);
			
			offset += 2;
			String serverName = data.substring(offset, offset + lServerName);
			
			offset += lServerName;
			int clientID = stringToInt(byteData, offset);
			
			offset += 4;
			short nParams = stringToShort(byteData, offset);
			
			offset += 2;
			float params[] = new float[nParams];
			
			for (int i = 0; i < nParams; i++) {
				params[i] = byteToFloat(byteData, offset);
				offset += 4;
			}
			
			float initialFreq = params[0];
			float finalFreq = params[1];
			float pulseDuration = params[2];
			float intervalDuration = params[3];
			
			this.serverName = serverName;
			this.clientID = clientID;
			
			// set configuration and convert s in ms.
			this.chirpDeamon.setConfiguration(initialFreq, finalFreq, pulseDuration*1000.0f, intervalDuration*1000.0f);
			
			tcpConnectionThread = new TCPConnectionThread(serverAddress, TCP_SERVER_PORT);
			tcpConnectionThread.start();
			
			
		} else if (data.startsWith(CLIENT_POS)) {
			int offset = 4;
			short dimension = stringToShort(byteData, offset);
			
			offset += 2;
			short numOfEntries = stringToShort(byteData, offset);
			
			offset += 2;
			float positions[][] = new float[numOfEntries][dimension];
			int types[] = new int[numOfEntries];
			                                 
			for (int i = 0; i < numOfEntries; i++) {
				
				types[i] = stringToInt(byteData, offset);
				offset += 4;
				
				for (int dim = 0; dim < dimension; dim++) {
					positions[i][dim] = byteToFloat(byteData, offset);
					offset += 4;
				}
			}
		}
		*/
	}
	
	/* Esto esta mal, o por lo menos produce cosas raras que ademas no sirven para nada
	private String shortToString(short value) {
		char highByte = (char)(value & 0xff);
		char lowByte = (char)((value >> 8) & 0xff00);
		return ""+highByte+lowByte;
	}
	*/
	
	/*
	private InetAddress getBroadcastAddress() throws IOException {
	    WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    // handle null somehow

	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}
	
	/**
	 * source: http://www.droidnova.com/get-the-ip-address-of-your-device,304.html
	 * @return
	 */
	
	
	
	class TCPConnectionThread extends Thread {
		
		private InetAddress serverAddress;
		private int port;
		private Socket socket;
		BufferedInputStream input;
		BufferedOutputStream output;
		private boolean closedConnection = false;
		
		public TCPConnectionThread(InetAddress serverAddress, int port) {
			this.serverAddress = serverAddress;
			this.port = port;
		}
		
		/**
		 * send Client Goodbye (CBYE) to the server and close the socket
		 */
		public void closeConnection() {
			closedConnection = true;
			if (socket.isConnected()) {
				try {
					if (output != null)
						output.write(CLIENT_GOODBYE.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						input.close();
						output.close();
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		@Override
		public synchronized void start() {
			closedConnection = false;
			System.out.println("Estamos en start");
			super.start();
		}
		
		@Override
		public void run() {
			try {
				
				System.out.println("Entramos dentro del run");
				
				socket = new Socket(serverAddress, port);
				input = new BufferedInputStream(socket.getInputStream());
				output = new BufferedOutputStream(socket.getOutputStream());
				
				
				//Para enviar un mensaje de prueba:
				String msg = CLIENT_HELLO+"nacho";//*shortToString((short)name.length())+*/name;
				
				System.out.println("Lo que se enviará por el buffer: "+msg);
				
				//DatagramPacket p = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ServerAddress /*getBroadcastAddress()*/, ServerPort);
				
				output.write(msg.getBytes(), 0, msg.getBytes().length);
				
				byte buffer[] = new byte[1024];
				
				// read till end of socketstream
				while (input.read(buffer) != -1) {
					evaluateServerAnswer(buffer);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// if something went wrong, close everything...
				try {
					input.close();
					output.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// ...and restart the ClientHello Broadcast, if the connection was not manually closed.
				if (!closedConnection)
					System.out.println("Aqui habria que reiniciar el cliente");//startClient(clientName);
			}
		}
	}
	
	
	
	public InetAddress getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress;
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        
	    }
	    return null;
	}
	
	/*
	public void FromIdToStream(byte[] buffer){
		
		String MessageFromServer = buffer;
		
	}
	*/
	
}
