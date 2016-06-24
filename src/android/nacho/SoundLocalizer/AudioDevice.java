package android.nacho.SoundLocalizer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioDevice
{
	public static final int SAMPLING_RATE = 44100; //44100;
   AudioTrack track;
   short[] buffer = new short[1024];
 
   public AudioDevice( )
   {
      int minSize =AudioTrack.getMinBufferSize( SAMPLING_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );        
      track = new AudioTrack( AudioManager.STREAM_MUSIC, SAMPLING_RATE, 
                                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
                                        minSize, AudioTrack.MODE_STREAM);
      track.play();   
      int nativeSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
       
      
      int sampleRate = track.getSampleRate();     
     
   }	   
 
   public void writeSamples(float[] samples) 
   {	
      fillBuffer( samples );
      track.write( buffer, 0, samples.length );
   }
 
   private void fillBuffer( float[] samples )
   {
      if( buffer.length < samples.length )
         buffer = new short[samples.length];
 
      for( int i = 0; i < samples.length; i++ )
         buffer[i] = (short)Math.round(samples[i] * (float)Short.MAX_VALUE);
   }
}