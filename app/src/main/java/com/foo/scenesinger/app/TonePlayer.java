package com.foo.scenesinger.app;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by sibigtroth on 7/15/14.
 */



//modified version of code found at:
//http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android


public class TonePlayer
{

    private BrainActivity brainActivity;
    private int duration = 1; // seconds
    private int sampleRate = 8000;
    private int numSamples = duration * sampleRate;
    private double sample[] = new double[numSamples];
    private double freqOfTone = 440; // hz
    private byte generatedSnd[] = new byte[2 * numSamples];
    Handler handler = new Handler();
    public ArrayList<Tone> tones;


    public TonePlayer(BrainActivity brainActivity)
    {
        this.brainActivity = brainActivity;

        this.Init();
    }

    private void Init()
    {
        this.tones = new ArrayList<Tone>();
    }


    ///////////////////////////
    //accessors
    ///////////////////////////

    private BrainActivity GetBrainActivity() {return this.brainActivity;}
    private ArrayList<Tone> GetTones() {return this.tones;}


    ///////////////////////////
    //utilities
    ///////////////////////////

    public void ClearTones()
    {
        this.tones = new ArrayList<Tone>();
    }

    public void CreateTone(int frequency, float duration)
    {
        Log.i("foo", "CreateTone " + frequency);
        Tone tone = new Tone(frequency, duration);
        this.GetTones().add(tone);
    }

    public void PlayTone(int index_tone)
    {
        this.GetTones().get(index_tone).Play();
    }

    public void PlayToneAfterDelay(int index_tone, int delay)
    {
        this.GetTones().get(index_tone).PlayAfterDelay(delay);
    }
    /*
    public void PlayTone(int frequency, float duration, int delay)
    {
        Tone tone = new Tone(frequency, duration, delay);
        tone.Play();
    }
    */

    class Tone
    {
        private float duration; // seconds
        private int sampleRate;
        private int numSamples;
        private double[] sample;
        private double freqOfTone; // Hz
        private byte[] generatedSnd;
        //Handler handler = new Handler();
        private AudioTrack audioTrack;

        public Tone(int freqOfTone, float duration)
        {
            this.freqOfTone = freqOfTone;
            this.duration = duration;

            this.Init();
        }

        private void Init()
        {
            this.sampleRate = 8000;
            this.numSamples = (int)(duration * sampleRate);
            this.sample = new double[numSamples];
            this.generatedSnd = new byte[2 * numSamples];
            //this.handler = new Handler();

            this.genTone();
            this.CreateAudioTrack();
        }

        public void Play()
        {
            final Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    audioTrack.release();
                    CreateAudioTrack();
                    if (audioTrack.getState() == 1) {
                        audioTrack.setStereoVolume(.3f, .3f);
                        audioTrack.play();
                    }
                }
            });
            thread.start();
        }

        public void PlayAfterDelay(int delay)
        {
            final int delay_ = delay;

            final Thread thread = new Thread(new Runnable() {
                public void run() {
                    //genTone();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            //playSound();
                            audioTrack.release();
                            CreateAudioTrack();
                            if (audioTrack.getState() == 1) {
                                audioTrack.setStereoVolume(.3f, .3f);
                                audioTrack.play();
                            }

                        }
                    }, delay_);
                }
            });
            thread.start();

        }
        /*
        public void Play()
        {
            final Thread thread = new Thread(new Runnable() {
                public void run() {
                    genTone();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Log.i("foo", "playing tone:  " + freqOfTone);
                            playSound();
                        }
                    }, delay);
                }
            });
            thread.start();
        }
        */

        void genTone(){
            // fill out the array
            for (int i = 0; i < numSamples; ++i) {
                sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
            }

            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalised.
            int idx = 0;
            int i = 0;

            int ramp = numSamples / 20 ; // Amplitude ramp as a percent of sample count


            for (i = 0; i< ramp; ++i) { // Ramp amplitude up (to avoid clicks)
                double dVal = sample[i];
                // Ramp up to maximum
                final short val = (short) ((dVal * 32767 * i/ramp));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }


            for (i = i; i< numSamples - ramp; ++i) { // Max amplitude for most of the samples
                double dVal = sample[i];
                // scale to maximum amplitude
                final short val = (short) ((dVal * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            for (i = i; i< numSamples; ++i) { // Ramp amplitude down
                double dVal = sample[i];
                // Ramp down to zero
                final short val = (short) ((dVal * 32767 * (numSamples-i)/ramp ));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }
        }

        void playSound()
        {
            Log.i("foo", "playing tone:  " + freqOfTone);

            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.length,
                    AudioTrack.MODE_STATIC);

            audioTrack.write(generatedSnd, 0, generatedSnd.length);
            audioTrack.play();
        }

        private void CreateAudioTrack()
        {
            this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.length,
                    AudioTrack.MODE_STATIC);

            this.audioTrack.write(generatedSnd, 0, generatedSnd.length);
        }
    }

}






























