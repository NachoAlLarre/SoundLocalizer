package android.nacho.SoundLocalizer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class HelloMessage {

	Context mContext ;
	private final Handler mHandler;		
	private boolean Working=true;
	
	private char Role;
	
	//Prefix List
	public static final int HI 				= 1;
	//public static final int SLAVELIST 	= 2;
	public static final int ROLEDONE	 	= 3;
	//public static final int SLAVELEAVING 	= 4;
	public static final int TIME 			= 5;
	public static final int RESTART			= 6;
	
	//Protocol variables 
	private boolean HasRole=false;
	private boolean MateHasRole=false;
	
	private HelloThread mHelloThread;
	
	//Buffer
	private byte[] buf = new byte[1024]; 
	
	//SlaveList
	public ArrayList<Integer> SlaveList = new ArrayList<Integer>();
	
	private int LocalIP;
	private String Local_IP_String;
	
	
	public HelloMessage(Context context, Handler handler, String IP){
		
		mContext=context;
		mHandler = handler;
		Local_IP_String=IP;
		String[] IP_Parts = IP.split("\\.");
		//System.out.println(" ver lo que sale "+IP);
		//System.out.println("La parte 3 que parece la mas interesante es: "+IP_Parts[3]);
		LocalIP=Integer.parseInt(IP_Parts[3]);
		System.out.println("The local IP is:"+Local_IP_String);
		
	}
	
	
	public synchronized void restart() {
	    
		
		System.out.println("It should be working again");
		Working=true;				 		
		
	}
	 
	 
	public synchronized void start() {
              
		Working=true;				 
		mHelloThread = new HelloThread();
		mHelloThread.start();
		
    }
	
	
	public void stop() {

		Working=false;
    }
		
	
	public void write(String data) {

		mHelloThread.writeBroadcast(buf, data);
    }
		
	public class HelloThread extends Thread {
	    	
		 //Here we define the variable of our thread	 
		 private static final int BCAST_PORT = 2562;
	     DatagramSocket mSocket ;
	     InetAddress myBcastIP;	 
		 
	    	public HelloThread() {
	    			    		
	    		try { 
			    	//Here we get the broadcast address   
	    			myBcastIP = getBroadcastAddress();
	     		   System.out.println("my bcast ip : "+myBcastIP);//if(D)Log.d(TAG,"my bcast ip : "+myBcastIP);		     		  
	     		   
	     		   //This sockect get ready to get datagram packets
	     		   mSocket 		= new DatagramSocket(BCAST_PORT); 
	     		   mSocket.setBroadcast(true); 
     		   
	    		 } catch (IOException e) { 
        	    	 
	    			 System.out.println("No se pudo crear el socket");
	    			 e.printStackTrace();	
	    			 //Log.e(TAG, "Could not make socket", e); 
        	     }
     	
	    	}
	    
	    	public void run(){
	    		
    			//Listen on socket to receive messages 
    			try{
	    		
    				//Here we start and endless loop where the app will listen to all the broadcast message it receives
    				
    				while(true){    				
	    			
    					if(Working){
    						
	    					////The device don't know what it it's role and send HI message to find it out
		    				if(!HasRole  & !MateHasRole) 
		    					writeBroadcast(buf, "1-HI"); 
		    				
			    			DatagramPacket packet = new DatagramPacket(buf, buf.length); 
			    			
			    			//Here we block the thread waiting for messages 
			    			mSocket.receive(packet); 
			    				    			
			    			InetAddress remoteIP = packet.getAddress();
			    			String remote=remoteIP.toString();
			    			
			    			String s = new String(packet.getData(), 0, packet.getLength()); 
			    			System.out.println("We recevied: "+s+" From device with IP: "+remoteIP);
			    				    		    				
			    			
			    			String[] IP_Parts = remote.split("\\.");
			    			String[] MESSAGE_Parts = s.split("\\-");
			    			
			    			int RemoteIP=Integer.parseInt(IP_Parts[3]);	  //This is the part of the IP that matters.     				    			
			    			int Prefix=Integer.valueOf(MESSAGE_Parts[0]); //Convert a string into a integer
			    			
			    			if(RemoteIP!=LocalIP) //The message is from someone else
			    			{
			    				
				    			switch(Prefix){
				    			
				    			case RESTART:
				    				
				    				if(Role=='s'){
				    					
				    					mHandler.obtainMessage(SoundLocalizer.MESSAGE_RESTART,-1,-1, null).sendToTarget();
				    					
				    				}
				    				
				    				break;
				    			
				    			case ROLEDONE:
				    				
				    				MateHasRole=true;
				    				
				    				break;
				    			
				    			case TIME:
				    				
				    				System.out.println("It receives the distance: "+MESSAGE_Parts[2]);
				    				//Send the distance to the handler
				    				mHandler.obtainMessage(SoundLocalizer.MESSAGE_TIME,-1,-1, MESSAGE_Parts[2]).sendToTarget();
				    				
				    				break;
				    						   
				    				
				    			case HI:
				    				
				    			
				    				if(!HasRole){
				    				
					    				//The device with the smaller/bigger IP becomes in the Slave
				    					if(RemoteIP>LocalIP){			    									    						
				    						
				    							HasRole=true; 
				    							writeBroadcast(buf, "3-ROLEDONE");
					    						
				    							Role='m';
				    							
					    						String message="I'm the Sender "+Local_IP_String;
					    						System.out.println(message);
					    						
					    						mHandler.obtainMessage(SoundLocalizer.MESSAGE_TOAST,-1,-1, message).sendToTarget();
					    						
					    						try {
													sleep(3000);//Master should start a little later
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
					    						mHandler.obtainMessage(SoundLocalizer.MESSAGE_AGREEMENT,-1,-1, "MASTER").sendToTarget();
				    						
				    						}else{
				    							
				    							HasRole=true;
				    							Role='s';
				    							writeBroadcast(buf, "3-ROLEDONE");
				    							
					    						String message="I'm the Responder "+Local_IP_String;
					    						System.out.println(message);
					    						
					    						mHandler.obtainMessage(SoundLocalizer.MESSAGE_TOAST,-1,-1, message).sendToTarget(); 
					    						//
					    						try {
													sleep(1500);
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
					    						mHandler.obtainMessage(SoundLocalizer.MESSAGE_AGREEMENT,-1,-1, "SLAVE").sendToTarget();
					    						
				    						}
				    					
				    				}
				    							    				    				
				    				break;
				    				
				    				default:
				    				continue;
	
				    			
				    			}
			    					    	
				    		
		
			    			}else{
			    					    				
			    				try{
				    				
				    			sleep(1500);
				    			}catch(Exception e) {
				    				System.out.println("For some reason can't sleep");
				    			}
			    				
			    				continue;
			    			}
		    			
		    		
    					}//if working
	    			}//While true
    				
    				
	    		
    			} catch (IOException e) {
					System.out.println("error Receiving");
					e.printStackTrace();		
    			}
	    			
	    	} //run()
	    	
	    		    	
	    	public void writeBroadcast(byte[] buffer,String data) { //To transmit Broadcast

	            try {
	            	//String data = new String (buffer);

	                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
	                		myBcastIP, BCAST_PORT);               
	                
	                mSocket.send(packet); 
	               System.out.println("The message"+data+" was sent");
	                
	                // Share the sent message back to the UI Activity
	               // mHandler.obtainMessage(BroadcastChat.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
	                       
	            } catch (Exception e) {
	                //Log.e(TAG, "Exception during write", e);
	            }
	        }
	    	
	    	
	    	public void write(byte[] buffer, String data, InetAddress IPaddress, int port)// To transmit peer to peer
	    	{

	            try {
	            	//String data = new String (buffer);

	                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
	                		IPaddress, port);               
	                
	                mSocket.send(packet); 
	               

	                       
	            } catch (Exception e) {
	                //Log.e(TAG, "Exception during write", e);
	            }
	    		
	    		
	    	}
	    	
	    	

	    	
	    	 private InetAddress getBroadcastAddress() throws IOException {
	    		 
	             WifiManager mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	             
	             WifiInfo info = mWifi.getConnectionInfo();
	     		 System.out.println("\n\nWiFi Status: " + info.toString());// if(D)Log.d(TAG,"\n\nWiFi Status: " + info.toString());
	     		
	       	  // DhcpInfo  is a simple object for retrieving the results of a DHCP request
	             DhcpInfo dhcp = mWifi.getDhcpInfo(); 
	             if (dhcp == null) { 
	              System.out.println("Could not get dhcp info");// Log.d(TAG, "Could not get dhcp info"); 
	               return null; 
	             } 
	          
	             
	             int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask; 
	             byte[] quads = new byte[4]; 
	             for (int k = 0; k < 4; k++) 
	               quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	             
	             // Returns the InetAddress corresponding to the array of bytes. 
	             return InetAddress.getByAddress(quads);  // The high order byte is quads[0].
	           }  
	    	
	    	 
	    	public void cancel() {
	            
	    		//At this moment the cancel function is not useful
	    		/*
	    		try {

	            	//Let the other devices in the network that we are leaving:
	            	System.out.println("Intento que se llegue al cancel");
	            	writeBroadcast(buf,"3-MASTERLEAVING-");
	        		if(Master) {writeBroadcast(buf,"3-MASTERLEAVING-");}//** MODIFY THIS IN CASE THE ONE LEAVING IS THE MASTER
	           	 	if(Slave)writeBroadcast(buf,"4-SLAVELEAVING-"+Local_IP_String);
	        		sleep(2000);
	        		System.out.println("Le damos un margen al socket");
	           	 	//mSocket.close(); //Aun no, para cuando tenga el socket
	            } catch (Exception e) {
	                //Log.e(TAG, "close() of connect socket failed", e);
	            }
	            */
	    		
	    	}
	 
	 
     }
	 
}
