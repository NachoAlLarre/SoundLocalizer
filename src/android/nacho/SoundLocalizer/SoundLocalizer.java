package android.nacho.SoundLocalizer;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.nacho.SoundLocalizer.HelloMessage.HelloThread;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
 
/**
 * This is the main Activity that displays the current chat session.
 */
public class SoundLocalizer extends Activity {

	/*
	 * VARIABLES FOR THE WIFI SERVICE
	 */
	//Variable to define if the app is going to use correlation or not
	private boolean Correlation=false;
	
	private boolean BeganHello=false;
	private boolean BeganListen=false;
	private boolean BeganDistanceThread=false;
	
	private boolean Threads_Active=false;
	
	//For the simple version when the role is selected
	private RadioGroup radioRoleGroup;
	private RadioButton radioRoleButton;
	private Button btnSelect;
	
	// Debugging
    private static final String TAG = "BcastChat";
    private static final boolean D = true;

    // Message types sent from the WifiService Handler
    public static final int MESSAGE_READ 		= 1;  
    public static final int MESSAGE_WRITE 		= 2;
    public static final int MESSAGE_TOAST 		= 3;
    public static final int MESSAGE_HELLO 		= 4;
    public static final int SLAVELIST 			= 5;
    public static final int MESSAGE_LEAVING		= 6;
    public static final int MESSAGE_AGREEMENT	= 7;
    public static final int MESSAGE_TIME		= 8;
    public static final int NEWONE		 		= 9;
    public static final int MESSAGE_DISTANCE    =10;
    public static final int MESSAGE_RESTART		=11;
    public static final int MESSAGE_MISSING     =12;
    
    private HelloMessage mHelloService=null;
    
    //The threads we have to use
    //private HelloThread mHelloThread;
    private Thread mListenThread = null;
    private Thread mDistanceThread = null;
    
    //Data from the Master and the Slave to find out the distance between each other
    
    private static int MasterData = 0;
    private static int SlaveData = 0;
    
    boolean ReadyMeasure=true;
    
    //T define a bucle for the MiniSlave to repeat the stream signal
    public static boolean Loop=true;
    public static int RepetitionEllapse=1500;  //Milliseconds

    // Key names received from the BroadcastChatService Handler
    public static final String TOAST = "toast";
    
    public String LocalIP="192.168.183.122"; //Es una tonteria hacer esto poque se le da un valor a la variable más adelante.

    // Layout Views
    private Button 		mAbortButton, mStartButton, mMeasureButton,  mMiniSlaveButton;
    
    private Button mStream7Button;
    private Button mStream5Button;
    private Button mStream3Button;
    
    private Context ThisContext=this;

    
    private ArrayAdapter<String> mConversationArrayAdapter;
    
    //private int InexCorr=0;
    
    /*
	 * VARIABLES FOR THE CHIRPER GENERATOR
	 */
    
    //To assign role
    private boolean Master=false,Slave=false;
    
    private char Role;
    
    private MiniSlave mMiniSlave;
    
    //Constanst for the recording process    
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO; //Probably in Mono would be better
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    //private float rate=176400;        
    private int sizePattern=2205;
    private int bufferSize = 2*(AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING));
    
    //Variables for the recording process
    private int LastIndex=0, Echo;
    //private Thread recordingThread = null;
    // private Thread mListenThread = null;
    private boolean isRecording = false;  
    private AudioRecord recorder = null;
    private long detection;
    double TimeSec;
    private String mess;
    private boolean selfActv;
    
    private boolean MasterReady=false;
    
    private boolean WaitForMaster=true;
    
    //Some provisional variable
    private boolean TheFirst;   
    private int CounterLimit;
    
    private double Distance_MASTER=0.0;
    private String Status=null;
    
    private float patt1[] = new float[sizePattern];
    
    
    private int countTrap=0;
    
    
    /*	Handler: This method will handle the messages sent by HelloMessage.java
     *  This messages will be used to establish the role of each device, that can 
     *  be Master or Slave.
     */
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	
        	if(D) Log.e(TAG, "[handleMessage !!!!!!!!!!!! ]");
        	
            switch (msg.what) {
            
            	case MESSAGE_RESTART:
            		
            		ReadyMeasure=true;          		
            		((ListenThread) mListenThread).RestartThread();
            		
            		break;
            
            	case MESSAGE_DISTANCE:
            		
            		//System.out.println("A ver que llega");

            		String readBuf5 = (String) msg.obj;	                          
            		
            		double Sample_diff=Integer.valueOf(readBuf5);
            		         						
					double Tiempo=Sample_diff/8820000;//17640000;
																		
					double Distance_Devices=Tiempo*343;
            						
					
					
					
					String Final_Message = "Distances: "+rounded+" (m)";	    
					
					//String Final_Message = "Distances: "+Distance_Devices+" (m)";	    
					
					Toast toast = Toast.makeText(SoundLocalizer.this, Final_Message , Toast.LENGTH_SHORT);
	                LinearLayout toastLayout = (LinearLayout) toast.getView();
	                TextView toastTV = (TextView) toastLayout.getChildAt(0);
	                toastTV.setTextSize(25);
	                toast.show();
		            	           		 
            		break;
            		
            	case MESSAGE_MISSING:
            		
            		String Message = "Information Missed";	    
					
	            	//Toast.makeText(SoundLocalizer.this, Message , Toast.LENGTH_SHORT);
	            	
	            	Toast toast2 = Toast.makeText(SoundLocalizer.this, Message , Toast.LENGTH_SHORT);
	                LinearLayout toastLayout2 = (LinearLayout) toast2.getView();
	                TextView toastTV2 = (TextView) toastLayout2.getChildAt(0);
	                toastTV2.setTextSize(25);
	                toast2.show();
	            	
	            	
	            	
            		
            		break;
            
	            case MESSAGE_WRITE:
	            	
	                byte[] writeBuf = (byte[]) msg.obj;
	                // construct a string from the buffer
	                String writeMessage = new String(writeBuf);
	                mConversationArrayAdapter.add("Sent:  " + writeMessage);

	                break;
	                
	            case MESSAGE_READ:
	                String readBuf = (String) msg.obj;
	                mConversationArrayAdapter.add("Received:  " + readBuf);
	                break;    
	               
	            //Lo podriamos suprimir
	            case MESSAGE_TOAST:
	            	String readBuf3 = (String) msg.obj;
	            	
	            	//Toast.makeText(SoundLocalizer.this, readBuf3 , Toast.LENGTH_SHORT).show();
	            	
	            	Toast toast3 = Toast.makeText(SoundLocalizer.this, readBuf3 , Toast.LENGTH_SHORT);
	                LinearLayout toastLayout3 = (LinearLayout) toast3.getView();
	                TextView toastTV3 = (TextView) toastLayout3.getChildAt(0);
	                toastTV3.setTextSize(25);
	                toast3.show();
	            	
	                break;
	                
	            case MESSAGE_HELLO:
	            	String readBuf2 = (String) msg.obj;
	            	//System.out.println("Desde el Handler: "+readBuf2);
	            	
	            	break;
	            	
	            case MESSAGE_LEAVING:
	            	
	            	String readBuf4 = (String) msg.obj+" Is leaving";	            	
	            	Toast.makeText(SoundLocalizer.this, 
	            			readBuf4 , Toast.LENGTH_SHORT).show();
	            	
	            	break;
	            	
	            	
	            case  MESSAGE_AGREEMENT:
	            	
	            	Status=(String) msg.obj;
	            	
	            	//System.out.println("I got this from handler "+Status);
	            	
	            	if(Status=="MASTER"){

	            		//The master must create and launch a thread to perform the measures
	            		if(!BeganDistanceThread)
	            		{
	            			mDistanceThread = new DistanceThread();
	            			mDistanceThread.start();
	            			BeganDistanceThread=true;
	            		}
	            		Role='m';	                             
	            		
	            	}	            		
	            		
	            	if(Status=="SLAVE"){//SLave
	            		
	            		//Those are variables for the Slave, that I think are not useful
	            		Echo=0; CounterLimit=1000000;
                    	Role='s';
                    	
	            	}
	            	
	            	//Start Listen Thread
	            	if(!BeganListen)
            		{
	            		((ListenThread) mListenThread).RestartThread();
	            		mListenThread.start();
            			BeganListen=true;
            		}
                    
	            	
	            	
	            	break;
	            	
	            case MESSAGE_TIME:
	            	
	            	//System.out.println("Should be receiving something");
	            	
	            	
	            	String Distance2=(String) msg.obj;
	            	
	            	SlaveData=Integer.valueOf(Distance2);
	            	
	            	//System.out.println("The Salve sent it's value: "+SlaveData);	            
					
	            	break;	            	
	         
            }
        }
    };    
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.main);
        
        //System.out.println("Se admiten: "+(bufferSize/4));
        
        //Define the Listener to the button
        //addListenerOnButton();
    }

   //Enable/disable bottoms, so the user can't press anything and spoil the communication
    
    private void enableButton(int id,boolean isEnable){
        ((Button)findViewById(id)).setEnabled(isEnable);
    }
    
    private void enableButtons(boolean activate) {
        enableButton(R.id.btnStart,activate);
    }
    
    
    
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        
        //First we get the local IP
        GetLocalIP();
              
        mListenThread = new ListenThread();
        
        setupChat();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        
        
    }
    
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    public void onReStart(){
    	
    	mHelloService.restart();
    }
    
    public void onStop() {
        super.onStop();
        
        mHelloService.stop();
        
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    /*This is new, let see what happen */
    public void onWait(){
    	
    	 mHelloService.stop();
    	
    }
    
    public void GetLocalIP(){
    	

        WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
   
        LocalIP=Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
    	System.out.println("La Local IP se declara en este punto: "+LocalIP);
    	
    }
    
    public void onDestroy() {
        super.onDestroy();

        
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
   
    
	private void setupChat() {
		
        Log.d(TAG, "setupChat()");

        
        mStartButton = (Button) findViewById(R.id.btnStart);
        mStartButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				//The aplicacion have already been lauched or not
				if(BeganHello){				
					onReStart();
				}else{
					//Since we still don't have the bluethoot part ready, we work only with the Wifi as soon as the application is lauched
			        mHelloService = new HelloMessage(ThisContext, mHandler, LocalIP);
			        mHelloService.start();
			        BeganHello=true;
				}
				isRecording = true;
				enableButtons(false);
				
				
            	
            }
        });
        
        mAbortButton = (Button) findViewById(R.id.btnAbort);
        mAbortButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				enableButtons(true);
				
				Loop=false;
				isRecording=false;
				WaitForMaster=false;
				onWait();	
				
            	
            }
        });          
        
    
	}//End of SetupChat
	
   /* 
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radio_Corr:
	            if (checked)
	            	Correlation=true;
	            break;
	        case R.id.radio_NoCorr:
	            if (checked)
	            	Correlation=false;
	            break;
	            
	    }
	}
	*/
	
	
	/* 
	 *     addListenerOnButton
	 *    
	 *     With this method I add a listener for the btn_select button
	 *     that is used to select a role fo the device, between slave and master
	 *     
	 *     source: http://www.mkyong.com/android/android-radio-buttons-example/
	 */
	
	
	 private class DistanceThread extends Thread{
	    	
	    	//Constructor
	    	public DistanceThread(/*Here we should place any variable*/){
	    		
	    	}
	    	
	    	//Run class
	    	 public void run() {
	    		 	    		 
	    		 while(true){
	    			 
	    			 if((MasterData>0) & (SlaveData>0) & ReadyMeasure)
		    		 {
		    			 
	    				 ReadyMeasure=false;
	    				 int RealDistance=MasterData-SlaveData;
	    				 
	    				 //We restart the data for the next record round:
	    				 SlaveData=0;
	    				 MasterData=0;
	    				 
		    			 System.out.println("The distance in samples is: "+RealDistance);
		    			 String MessageDis=""+RealDistance;
		    			 
		    			 mHandler.obtainMessage(SoundLocalizer.MESSAGE_DISTANCE,-1,-1, MessageDis).sendToTarget();
		    			
		    			 //After this the master will be sleeping for two seconds
		    			 try {
							sleep(4000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    			 
		    			 //Finally the Master announce the Slave another round of recording
		    			 String send="6-RESTART";
				         mHelloService.write(send);    
				         
				         //The Master gets ready itself for another round of recording
				         mHandler.obtainMessage(SoundLocalizer.MESSAGE_RESTART,-1,-1, null).sendToTarget();

		    		 } 
	    			 
	    			 
	    		 }
	    		 
		 
	    	 }
	
	 }
    
	 
    private class ListenThread extends Thread{
    	
    	int Receive, Send, Generation, CountBuffers;		  		    
		boolean Deaf, Data, JustOne, JustOne2, Possible;
		  	
    	//Constructor
    	public ListenThread(/*Here we should place any variable*/){
    				
    		
    	}
    	
    	//Run class
    	 public void run() {
    		 
    		 switch(Role){
	     		
    		 	case 's':
	     					     			    			
	     			SlaveRecording(Correlation);
	     			
	     			break;
	     			
	     		case 'm':
	     			
	     			MasterRecording(Correlation);
	     			
	     			break;

     		}
    		 
    		 
    	 }
    	 
    	 public void RestartThread(){
    		 
    		Receive=0;  Send=0; Generation=0;		  		    
    		Deaf=false; JustOne=true; JustOne2=true; Possible=false; Data=true;
    		CountBuffers=-10000;
    		
    		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
   		
			recorder.startRecording();  
		    isRecording = true;		    		    
    		 
		    System.out.println("We are in restart");
		    
    	 }
    	 
    	 
    	 private void SlaveRecording(boolean Correl){
 				
    		System.out.println("El valor de la correlacion es: "+Correl);
    		
    		byte data[] = new byte[bufferSize];
    		int read = 0;
  		
    		//float patt1[] = new float[sizePattern];
			//float patt3[] = new float[sizePattern];
    		
    		if(Correl) patt();

 		   int counter=0; 
 		   int prov[]=new int[bufferSize];
 		   int valueSample=0;
 
 		   int PrevSamples=0;
 		   
 		   int i, sum;

 		   int FromCorr=0;
 		   
 		   double mean;
 	    		  
	    	 //The user decided not to use the correlation
 			 
 			 while(true){
 		        
 				 if(isRecording){	 					
 				 	 		
		        	read = recorder.read(data, 0,bufferSize);           	
		        	
	        		//System.out.println("El valor de CounterLimit es: "+CounterLimit);
	        		CountBuffers+=1;
			
	        		i=0;
	        		sum=0;
	        				
	        		for(int m=0; m<(bufferSize); i++, m+=4, counter++) //1 sample every 4 bytes
	            	{
	        			        			
	        			//if(i==0)System.out.println("The value of Countbuffers is: "+CountBuffers);
	        			
	        			
	        			if(5<CountBuffers & JustOne)//if(CounterLimit<counter & JustOne) // Count down to reply
	            		{	
	        				
	        				Deaf=false;  
	        				System.out.println("CounterBuffer is greater than 5 and JustOne es true");
	        				JustOne=false;
	        				Generation=counter;
	        				
	        				
	        				System.out.println("Let's see the difference between the detection and the generation: "+Receive+" and "+Generation);
	        				
	        				new Thread( new Runnable( ) 
	        				{
              		           public void run( )
              		           {   
              		        	   int startFreq = 3000;
              		        	   int endFreq = 4000;
              		        	   double impulseDuration = 100;
              		        	   new ChirpGenerator().playChirp(startFreq, endFreq, impulseDuration);
              		           }
              		        } ).start();
	        				
	        			}
		        			
		        			 
	        			valueSample=data[m+1]*256+data[m];
	        			prov[i]=valueSample;
	        			sum+=Math.abs(prov[i]);
	        					        			
	        			if(!Correl)		
	        			{
	        			
	        				if(Math.abs(valueSample)>6000 & !Deaf)            				
		               		{    
			        			
			        			CountBuffers=0;
		           				Deaf=true;
		           				
		           				//System.out.println("Los valores de Send and Receive son: "+Send+"  and  "+Receive);
		           				
		           				if(Send==0 & Receive>0)
		           				{    		           					
		           					Send=counter;	
		           					System.out.println("The second pick detected has a value of: "+valueSample+"And it was at the time: "+Send);
		               				
		           				}
		           				
		           				if(Receive==0) //This means it has detect the first high pick )
		           				{	           					
		           					Receive=counter;
		           					System.out.println("The first pick detected has a value of: "+valueSample+"And it was at the time: "+Receive);            					        					
		           					
		           				}	
				        			
		               		}		        				
	        				
	        			}
		        		        			
	        			
		            }  
	        		

	        		mean=sum/i;
	        		
	        		System.out.println("The mean is: "+mean);
	        		//FromCorr=Correlation(prov);
	        		
	        		//Correlacion(patt1, prov, i);
	        		        		
	        		
	        		if(Correl & !Deaf) //If we are using the correlation, we check every buffer looking for a peack
	        		{
	        				        			 			
	        			PrevSamples=counter-4410;
	        			
	        			if(Send==0 & Receive>0)  // check second peack
	        			{	
	        				FromCorr=Correlation(prov);
	        				//FromCorr=Correlacion(patt3, prov, i);	
	        				if(FromCorr>0) 
	        				{ 
	        					CountBuffers=0;
	               				Deaf=true;
	        					Send=FromCorr+PrevSamples;
	        					System.out.println("Ingredients: FromCorr "+FromCorr+" PrevSamples "+PrevSamples);
	        					System.out.println("The second pick detected was at the time: "+Send);
	        				}
	        				
	        			}
	        			
	        			if(Receive==0) // check first peack
	        			{
	        				FromCorr=Correlation(prov);
	        				//FromCorr=Correlacion(patt1, prov, i);
	        				if(FromCorr>0) 
	        				{
	        					CountBuffers=0;
	               				Deaf=true;
	        					Receive=FromCorr+PrevSamples; 
	        					System.out.println("Ingredients: FromCorr "+FromCorr+" PrevSamples "+PrevSamples);
	        					System.out.println("The first pick detected was at the time: "+Receive);  
	        				}
           						
	        			}
	        		}
	        		      		
	        		
	        		//This means the Slave has already detected the Master signal and it's own
	        		if(Send!=0 && Data)
	        		{
	        			Data=false;
	        			stopRecording();		        			
	        			String Difference=""+(Send-Receive);
	        			System.out.println("The difference between Receive: "+Receive+" and Send: "+Send+" is this: "+Difference);
	        			
	        			String send="5-TIME-"+Difference;
			            mHelloService.write(send);        			
	        		
       				//Goodbye();
	        		}
    	
	        		
 			 	}//If isrecording
 			 
 			 } //End While(true) 			
	    	
 			
 		}	//End SlaveRercording	
 		     
    	 
    	private void MasterRecording(boolean Correl){
    		       		
			System.out.println("El valor de la correlación es: "+Correl);
			
        	byte data[] = new byte[bufferSize];
            
        	
			//float patt3[] = new float[sizePattern];
    		
    	   if(Correl) 
    	   {
    		   float patt1[] = new float[sizePattern];
    		   patt();
    	   }
    		      			
        	
           int read = 0;          
   
           int PrevSamples=0;
           
           int FromCorr=0;
           int counter=0;
           int prov[]=new int[bufferSize];
          
           int valueSample=0;            
           int i,sum;
           double mean;
           int meanCounters=0;
           double mean3=0;
            
            
        	while(true){
        		        		 	
            	if(isRecording){
            		                 	
                	read = recorder.read(data, 0,bufferSize);               
                	
                	if(CountBuffers==-10000) System.out.println("In the recording");
                	
            		sum=0;
            		CountBuffers+=1;
            		
            		if(5<CountBuffers)//if(CounterLimit<counter & JustOne) // Count down to reply
            		{	
        				
        				Deaf=false;
            		}
            		
            		//It means the Slave didn't reply
            		if(15<CountBuffers)
            		{
            			TimeOutRestart();
            			
            		}
            	
            		i=0;
            				
            		for(int m=0; m<(bufferSize); i++, m+=4, counter++) //Metemos una muestra de cada 4, combinando las dos primeras_> monomodo little endian
                	{
        			         			        			            			
            			//The master make his first chirp to be replyed by the Slave
            			if(JustOne)
                    	{
                    		JustOne=false;
                    		Generation=counter; //Counter si not set to 0 every time the buffer is filled with new information
                    		
                    	   new Thread( new Runnable( ) 
                        	{
               		           public void run( )
               		           {   
               		        	   int startFreq = 3000;
               		        	   int endFreq = 4000;
               		        	   double impulseDuration = 100;
               		        	   new ChirpGenerator().playChirp(startFreq, endFreq, impulseDuration);
               		           }
               		        } ).start();
                           
                    	}
            			
            			valueSample=data[m+1]*256+data[m];
            			prov[i]=valueSample;
            			sum+=Math.abs(prov[i]);	   	               
            			
            			if(!Correl)
            			{
            			
	            			if(Math.abs(valueSample)>6000 & !Deaf)            				
	                		{    
	            				
	            				Deaf=true;
	            				if(Receive==0 & Send>0)
	            				{
	            					//if(Correl) Possible=true;
	            					System.out.println("The second pick detected has a value of: "+valueSample+"And it was at the time: "+counter);
	                				Receive=counter;
	            					
	            				}
	            				
	            				if(Send==0) //This means it has detect the first high pick )
	            				{
	            					//if(Correl) Possible=true;
	            					System.out.println("The first pick detected has a value of: "+valueSample+"And it was at the time: "+counter);
	            					Send=counter;  
	            					CountBuffers=0;
	            					
	            				}            				       				
	            				      			
	                		}
            			
            			}
            			
                	}
            		   
            		mean=sum/i;
            		
            		if(mean>500)
            		{
            			meanCounters++;
            			mean3+=mean;
            			
            			if(meanCounters>3)
            				{
            					
            					System.out.println("The mean is: "+mean3);
            					meanCounters=0;
            					mean3=0;
            				}
            			
            		}else{
            			meanCounters=0;
            			
            		}
            		
            		
            		
            		
            		if(Correl & !Deaf) //If we are using the correlation, we check every buffer looking for a peak
	        		{
            			PrevSamples=counter-4410;
	        				        			
	        			if(Receive==0 & Send>0)  // check second peak
	        			{	
	        				FromCorr=Correlation(prov);
	        				//FromCorr=Correlacion(patt3, prov, i);	
	        				if(FromCorr>0)
	        				{ 
	                			CountBuffers=0;
	                			Deaf=true;
	        					Receive=FromCorr+PrevSamples;
	        					System.out.println("Ingredients: FromCorr "+FromCorr+" PrevSamples "+PrevSamples);
	        					System.out.println("The second pick detected was at the time: "+Receive);
	        				}
	        				
	        				
	        			}
	        			
	        			if(Send==0) // check first peak
	        			{
	        				FromCorr=Correlation(prov);
	        				//FromCorr=Correlacion(patt1, prov, i);
	        				if(FromCorr>0)
	        				{
	                			CountBuffers=0;
	                			Deaf=true;
	        					Send=FromCorr+PrevSamples;
	        					System.out.println("Ingredients: FromCorr "+FromCorr+" PrevSamples "+PrevSamples);
	        					System.out.println("The first pick detected was at the time: "+Send); 
	        				}
	        				           					        					
           						
	        			}
	        			
	        			
	        		}
            		  
            		          		
            		
            		if(Receive>0 & JustOne2)
            		{
            			stopRecording();
            			JustOne2=false;       			
            			MasterData=Receive-Send;
            			System.out.println("En el Master tenemos como datos: "+MasterData);
            		
            		}

        		
            	}//End of if
            	
        	}//End of while
          
	        	
	    }
    				 
    	
		private void stopRecording(){
		    if(null != recorder){
		    	
	    		isRecording=false;
	    		
	            recorder.stop();
	            recorder.release();
	           
	            recorder = null;
	            
		    }
		   
		} 
		
		private void TimeOutRestart(){
			
			stopRecording();
			
			//Finally the Master announce the Slave another round of recording
			 String send="6-RESTART";
	         mHelloService.write(send);    
	         
	         //The Master gets ready itself for another round of recording
	         mHandler.obtainMessage(SoundLocalizer.MESSAGE_MISSING,-1,-1, null).sendToTarget();
	         mHandler.obtainMessage(SoundLocalizer.MESSAGE_RESTART,-1,-1, null).sendToTarget();
			
		}

    	 
    }
    	
    private int Correlation(int RecordedSamples[])
    {
    	
    	
    	//Variable I need to stablish
    	int mx, my, sx, sy, sxy;
    	int maxSxy=0; 
    	int IndexCorr=0;
    	
    	int sizePattern=patt1.length;
    	double denom;
    	int delay, Provi;
    	int Smaller=0;
    	int CorrLimit=(RecordedSamples.length)-30;
    	int SmallerLimit=(RecordedSamples.length-sizePattern);
    	
    	System.out.println("CoorLimit: "+CorrLimit+" SmallerLimit: "+SmallerLimit);
    	
    	for(delay=0 ; delay<CorrLimit ; delay++ )
		{		
    		
    		if(delay>= SmallerLimit) Smaller++;
    		
				// Calculate the mean of the two series provDouble[], patt1[] 
				
				mx = 0;
				my = 0;
				int index;						
				
				for(index=0; index<(sizePattern-Smaller); index++)
				{
																						  	   
				      mx += RecordedSamples[index+delay];						     
				      my += patt1[index];								     							   							  

				}
				
				mx /= (sizePattern-Smaller);
				my /= (sizePattern-Smaller);	
				
				
				
				// Calculate the denominator 
				sx = 0;
				sy = 0;
				
				
				for(index=0; index<(sizePattern-Smaller); index++)
				{
																						  	   
					sx += (RecordedSamples[index+delay] - mx) * (RecordedSamples[index+delay] - mx);					     
					sy += (patt1[index] - my) * (patt1[index] - my);							     							   							  

				}								
				   
				denom = Math.sqrt(sx*sy);	
				
				// Calculate the correlation series 
				
				sxy = 0;
				
				for(index=0; index<(sizePattern-Smaller); index++)
				{
					
					sxy += Math.abs( ( (RecordedSamples[index+delay] - mx) * (patt1[index] - my) / denom) );	
													
				}
				
				 
				//sxy*=1000; //This is to store it better in the file
				Provi=(int) sxy;
				
				if(delay<10)
					System.out.println("mx "+mx+" my "+my+" sx "+sx+" sy "+sy+" sxy "+sxy);
				
				if(sxy>maxSxy) 
				{
					maxSxy=sxy;
					IndexCorr=delay;
					
				}
				
				//Here is where I need to make some kind of comparation
				
				
		}
    	
    	System.out.println("El valor de la correlación es: "+maxSxy);
  	   	
    	if(maxSxy>0.85) return IndexCorr;
    	else return -1;
    	
    }

		
	private int Correlacion(float patt[], int prov[], int i)
	{
		int IndexCorr=-1;
		boolean Found=false;
		int Maxprov=0;
		double NormValue, CorrMax=0, Corr=0;
		double provDouble[]= new double[i];	

		//A maximum it 13000 and we normalize always respect this value
		Maxprov=13000;
		
		
		//First determine the maximum value of prov[] to normalize it
		for(int k=0; k<i ; k++)   // En vez de i antes era b **
		{
			//if(prov[k]>Maxprov) Maxprov=prov[k];
			provDouble[k]=prov[k];

		}
		
		
		
		for(int p=0; p<(i-sizePattern); p++)   
		{
			for(int q=0; q<sizePattern ; q++)
			{
				NormValue=(float)(provDouble[p+q]);///Maxprov);						
				Corr+=NormValue*patt[q];//(accur[p+1+q]*256+accur[p])*patt1[q]; **
			}
			
			if(Corr>CorrMax) {CorrMax=Corr; IndexCorr=p;}
			Corr=0;
		}
		
		System.out.println("La correlación vale: "+CorrMax);
		
		if(CorrMax>100000)return IndexCorr;
		else return -1;

	} 
	
	
	private float [] patt()
	{
		
		float patt1[] = new float[2205];
		
		int initialFreq=3000;		
		int  finalFreq=4000;
		double impulseDuration=100;
		int SAMPLING_RATE=44100;
		int size= 4410;//(int)(impulseDuration/1000.0 * SAMPLING_RATE);
		
		//AudioDevice device = new AudioDevice( );
		double k = (double)(finalFreq - initialFreq) / (impulseDuration/1000.0);
		double currentFreq = initialFreq;
		double phase;
		double t;
		int j = 0;
				
		for (int i = 0; i < size; i++) {
			t = (double)i / (double)SAMPLING_RATE;
			currentFreq = initialFreq + 0.5 * k * t;
			phase = 2.0 * Math.PI * (currentFreq) * t;
			
			
			
			if(i<(size/2))
			{
				patt1[j++] = (float)Math.sin(phase);
				
			}else{
				
				return patt1;
			}
			
		} 	
		
		return patt1;
		
	}	

		
		
}


