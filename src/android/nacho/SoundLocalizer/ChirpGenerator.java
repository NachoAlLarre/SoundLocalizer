package android.nacho.SoundLocalizer;

import android.util.FloatMath;


public class ChirpGenerator {
	
	private AudioDevice device;
	
	
	public ChirpGenerator() {
		device = new AudioDevice( );
	}

/**
 * 
 * @param initialFreq in Hz
 * @param finalFreq in Hz
 * @param impulseDuration in ms
 */
	void playChirp(int initialFreq, int finalFreq, double impulseDuration) {
		
		
		AudioDevice device = new AudioDevice( );
		double k = (double)(finalFreq - initialFreq) / (impulseDuration/1000.0);
		float samples[] = new float[1024];
		double currentFreq = initialFreq;
		double phase;
		double t;
		int j = 0;
				
		for (int i = 0; i < (int)(impulseDuration/1000.0 * AudioDevice.SAMPLING_RATE); i++) {
			t = (double)i / (double)AudioDevice.SAMPLING_RATE;
			currentFreq = initialFreq + 0.5 * k * t;
			phase = 2.0 * Math.PI * (currentFreq) * t;
			

			samples[j++] = (float)Math.sin(phase);
			
			if (j == 1024) {
				device.writeSamples( samples );
				j = 0;
			}
		} 
	}
	
	/**
	 * 
	 * @param initialFreq in Hz
	 * @param finalFreq in Hz
	 * @param pulseDuration in ms
	 * 
	 */
	void playChirpHELLOStream(int initialFreq, int finalFreq, double impulseDuration) {
		AudioDevice device = new AudioDevice( );
		double k = (double)(finalFreq - initialFreq) / (impulseDuration/1000.0);
		float samples[] = new float[1024];
		double currentFreq = initialFreq;
		double phase;
		double t;
		int j = 0;
		Integer digits[]= new Integer[64];
		
		int LengthWelcome=(44*30);
		
		
				
		for (int i = 0; i < (int)(impulseDuration/1000.0 * AudioDevice.SAMPLING_RATE); i++) {
			t = (double)i / (double)AudioDevice.SAMPLING_RATE;
			currentFreq = initialFreq + 0.5 * k * t;
			phase = 2.0 * Math.PI * (currentFreq) * t;
			
			//Fill with zeros where must, the first 60 milisecs: 30 for the welcome, and 30 for the ID
			
			if(i<LengthWelcome) //We introduce the welcome code: up 3ms, down 4ms, up 3ms, down 5ms, up 3ms, down 3ms, up 3ms, down 6ms
			{
				
				
				if((i>132)&(i<308)|(i>440)&(i<660)|(i>792)&(i<924)|(i>1056)&(i<1320))
					samples[j++] = 0;
				else
					samples[j++] = (float)Math.sin(phase);
				
			}
		}
	}
	
	
	
	void playChirpStream(int initialFreq, int finalFreq, double impulseDuration, int ID) {
		AudioDevice device = new AudioDevice( );
		double k = (double)(finalFreq - initialFreq) / (impulseDuration/1000.0);
		float samples[] = new float[1024];
		double currentFreq = initialFreq;
		double phase;
		int IDAux=ID;
		double t;
		int j = 0;
		Integer digits[]= new Integer[64];
		
		//To determine how many milisec is the signal up depending on the binar value
		int LengtUp1=(44*6);
		int LengtUp0=(44*3);
		
		int LengthWelcome=(44*30);
		
		int LengtDigitID=LengtUp1+LengtUp0;
		
		int LengthGoodBye=(2*44);
		
		int LimitSignal=LengthWelcome+3*(LengtDigitID); //The only important part is the welcome and the ID (which has got 3 digits)
														//The rest is filled with zeros
		
		int BegiFirstDig=1320, EndFirstDig= BegiFirstDig+LengtDigitID;
		int BegiSecondDig=EndFirstDig, EndSecondDig=BegiSecondDig+LengtDigitID;
		int BegiThirdDig=EndSecondDig, EndThirdDig=BegiThirdDig+LengtDigitID;
		int Final=EndThirdDig+LengthGoodBye;
		
		//System.out.println("Los valores son: "+BegiFirstDig+"     "+EndFirstDig+"    "+BegiSecondDig+"      "+EndSecondDig+"     "+BegiThirdDig+"    "+EndThirdDig); 
		
		
		for(int m=3; m>0; m--) //To ID from 0 to 7 -> just 8 devices in the network
		{
			
			digits[m]=IDAux%2;
			IDAux/=2;
			System.out.println("Valor de digits"+digits[m]);
		}
				
		for (int i = 0; i < (int)(impulseDuration/1000.0 * AudioDevice.SAMPLING_RATE); i++) {
			t = (double)i / (double)AudioDevice.SAMPLING_RATE;
			currentFreq = initialFreq + 0.5 * k * t;
			phase = 2.0 * Math.PI * (currentFreq) * t;
			
			//Fill with zeros where must, the first 60 milisecs: 30 for the welcome, and 30 for the ID
			
			if(i<LengthWelcome) //We introduce the welcome code: up 3ms, down 4ms, up 3ms, down 5ms, up 3ms, down 3ms, up 3ms, down 6ms
			{
				
				
				if((i>132)&(i<308)|(i>440)&(i<660)|(i>792)&(i<924)|(i>1056)&(i<1320))
					samples[j++] = 0;
				else
					samples[j++] = (float)Math.sin(phase);
				
			}else{
				
				/*
				//Some problems to generate the signal
				if((i>1320)&(i<3036)) //Para probar, metemos el 6 -> 1 1 0 manualmente
				{
					
					if((i>1320)&(i<1672)|(i>1848)&(i<2156)|(i>2376)&(i<2552)|(i>2904)&(i<3036))
						samples[j++] = (float)Math.sin(phase);
					else						
						samples[j++] = 0;
					
				}
				
				*/
				
				//The following 36 ms before the welcome are for the ID: 1= 7ms up, 3ms down ; 0= 3ms up, 7ms down
				
				
				if((i>BegiFirstDig)&(i<EndFirstDig)) //First digit
				{
					if(digits[1]==0) //First digit is 0 
					{
						if((i>BegiFirstDig)&(i<BegiFirstDig+LengtUp0)) //The limits are 44*3=132, 44*7=308
						{
							samples[j++] = (float)Math.sin(phase);
						}else{
							
							samples[j++] = 0;
						}
					}else{ //First digit is 1
						
						if((i>BegiFirstDig)&(i<BegiFirstDig+LengtUp1)) //The limits are 44*3=132, 44*7=308
						{
							samples[j++] = (float)Math.sin(phase);
						}else{
							
							samples[j++] = 0;
						}
					}
					
				}else{
				
					if((i>BegiSecondDig)&(i<EndSecondDig))//Second digit
					{
						if(digits[2]==0) //Second digit is 0 
						{
							if((i>BegiSecondDig)&(i<BegiSecondDig+LengtUp0)) 
							{
								samples[j++] = (float)Math.sin(phase);
							}else{
								
								samples[j++] = 0;
							}
						}else{ //Second digit is 1
							
							if((i>BegiSecondDig)&(i<BegiSecondDig+LengtUp1)) 
							{
								samples[j++] = (float)Math.sin(phase);
							}else{
								
								samples[j++] = 0;
							}
						}
						
					}else{
					
						if((i>BegiThirdDig)&(i<EndThirdDig))//Third digit
						{
							
							if(digits[3]==0) //Third digit is 0 
							{
								if((i>BegiThirdDig)&(i<BegiThirdDig+LengtUp0)) 
								{
									samples[j++] = (float)Math.sin(phase);
								}else{
									
									samples[j++] = 0;
								}
							}else{ //Third digit is 1
								
								if((i>BegiThirdDig)&(i<BegiThirdDig+LengtUp1))
								{
									samples[j++] = (float)Math.sin(phase);
								}else{
									
									samples[j++] = 0;
								}
							}
						}else{
						
						
							if((i>EndThirdDig)&(i<Final))
								samples[j++] =(float)Math.sin(phase);
							else{
							//if(i>Final)
								samples[j++] = 0;
							}
						}
					}
				}
			}
			if (j == 1024) {
				
				/*for(int r=0; r<1024 ; r++)
					{
						System.out.println("El valor del sample es: "+samples[r]);
					}
				*/
				device.writeSamples( samples );
				j = 0;
			}
		} 
	}
	
	void playChirpWithFading(double initialFreq, double finalFreq, double pulseDuration) {
		
		double k = (double)(finalFreq - initialFreq) / (pulseDuration/1000.0);
		float samples[] = new float[1024];
		double currentFreq = initialFreq;
		double phase;
		double t;
		int j = 0;
		double amplitude = 0;
		double sigma = 0.4;
		double M = 1000;
		double nStep = 0;
/*
		int fadingPeriods = 10000;
		double initialPeriod = 1.0 / initialFreq;
		double finalPeriod = 1.0 / finalFreq;

		int nInitialFadingSamples = (int) (fadingPeriods * initialPeriod * AndroidAudioDevice.SAMPLING_RATE); 
		int nFinalFadingSamples = (int) (fadingPeriods * finalPeriod * AndroidAudioDevice.SAMPLING_RATE);
		
		double initialFadingAmplitudeStep = 1.0 / nInitialFadingSamples;
		double finalFadingAmplitudeStep = 1.0 / nFinalFadingSamples; 
		*/
		int nSteps = (int)(pulseDuration/1000.0 * AudioDevice.SAMPLING_RATE);

		for (int i = 0; i < nSteps; i++) {
			
			/*if (i < nInitialFadingSamples) {
				amplitude += initialFadingAmplitudeStep;
				assert(amplitude <= 1);
			} else if (i >= nSteps - nFinalFadingSamples) {
				amplitude -= finalFadingAmplitudeStep;
				assert(amplitude >= 0);
			} else {
				amplitude = 1;
			}*/
			
			t = (double)i / (double)AudioDevice.SAMPLING_RATE;
			currentFreq = initialFreq + 0.5 * k * t;
			phase = 2.0 * Math.PI * (currentFreq) * t;

			if (i < M/2)
				amplitude = Math.exp(-1.0/2.0*Math.pow(((i)-(M-1)/2.0)/(sigma*(M-1)/2.0),2));
			if (i > nSteps - (M-1)/2)
				amplitude = Math.exp(-1.0/2.0*Math.pow((i-((nSteps)-(M-1)/2.0))/(sigma*(M-1)/2.0),2));
		
			
			samples[j++] = (float)(amplitude*Math.sin(phase));
			if (j == samples.length) {
				device.writeSamples( samples );
				j = 0;
				nStep++;
			}
		}
		// append zeros to avoid click sound at the end
		j = 0; 
		for (int i = 0; i < 0.1f*AudioDevice.SAMPLING_RATE; i++) {
			samples[j++] = 0;
			if (j == samples.length) {
				device.writeSamples( samples );
				j = 0;
			}
		}
			
	}
	
	
	void playChirpWithFadingSigmoid(int initialFreq, int finalFreq, double impulseDuration) {
		AudioDevice device = new AudioDevice( );
		double k = (double)(finalFreq - initialFreq) / impulseDuration;
		float samples[] = new float[1024];
		double currentFreq = initialFreq;
		double phase;
		double t;
		int j = 0;
		double amplitude = 0;
		double sigma = 0.4;
		double M = 1000;
		double nStep = 0;
/*
		int fadingPeriods = 10000;
		double initialPeriod = 1.0 / initialFreq;
		double finalPeriod = 1.0 / finalFreq;

		int nInitialFadingSamples = (int) (fadingPeriods * initialPeriod * AndroidAudioDevice.SAMPLING_RATE); 
		int nFinalFadingSamples = (int) (fadingPeriods * finalPeriod * AndroidAudioDevice.SAMPLING_RATE);
		
		double initialFadingAmplitudeStep = 1.0 / nInitialFadingSamples;
		double finalFadingAmplitudeStep = 1.0 / nFinalFadingSamples; 
		*/
		int nSteps = (int)(impulseDuration * AudioDevice.SAMPLING_RATE);

		for (int i = 0; i < nSteps; i++) {
			
			/*if (i < nInitialFadingSamples) {
				amplitude += initialFadingAmplitudeStep;
				assert(amplitude <= 1);
			} else if (i >= nSteps - nFinalFadingSamples) {
				amplitude -= finalFadingAmplitudeStep;
				assert(amplitude >= 0);
			} else {
				amplitude = 1;
			}*/
			
			t = (double)i / (double)AudioDevice.SAMPLING_RATE;
			currentFreq = initialFreq + 0.5 * k * t;
			phase = 2.0 * Math.PI * (currentFreq) * t;

			if (i < M/2)
				amplitude = 1.0/(1.0+Math.exp(-0.1*(i-4)));
			if (i > nSteps - (M-1)/2)
				amplitude = Math.exp(-1.0/2.0*Math.pow((i-((nSteps)-(M-1)/2.0))/(sigma*(M-1)/2.0),2));
		
			
			samples[j++] = (float)(amplitude*Math.sin(phase));
			if (j == samples.length) {
				device.writeSamples( samples );
				j = 0;
				nStep++;
			}
		}
	}
}
