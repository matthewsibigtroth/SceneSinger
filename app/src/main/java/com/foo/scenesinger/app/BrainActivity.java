package com.foo.scenesinger.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;




/*
TODO:
*/




public class BrainActivity extends Activity
{

    public int W_SCREEN;
    public int H_SCREEN;
    private RelativeLayout topContainer;
    //private ImageAnalyzer imageAnalyzer;
    //private SynesthesiaDisplay synesthesiaDisplay;
    private int RESULT_CODE_PHOTO_TAKER_ACTIVITY;

    private SurfaceView mPreview;
    private SurfaceHolder mPreviewHolder;
    private Camera mCamera;
    private boolean mInPreview = false;
    private boolean mCameraConfigured = false;
    private ImageAnalyzer imageAnalyzer;
    private boolean shouldGrabCameraFrame = false;
    private TonePlayer tonePlayer;
    private CountDownTimer playTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brain);

        this.Init();
    }

    private void Init()
    {
        this.CreateImageAnalzyer();
        this.CreateTonePlayer();
        this.CreatePlayTimer();
        this.InitCamera();
        this.DisableAutoSleep();
    }

    private void CreateImageAnalzyer()
    {
        this.imageAnalyzer = new ImageAnalyzer(this);
    }

    private void CreateTonePlayer() {this.tonePlayer = new TonePlayer(this);}

    private void CreatePlayTimer()
    {
        final int delayBetweenCalls = 4000;
        this.playTimer = new CountDownTimer(delayBetweenCalls, 1000)
        {
            @Override
            public void onTick(long l)
            {

            }

            @Override
            public void onFinish()
            {
                shouldGrabCameraFrame = true;
                this.start();
            }
        };
        this.playTimer.start();
    }

    private void InitCamera()
    {
        mPreview = (SurfaceView)findViewById(R.id.preview);
        mPreviewHolder = mPreview.getHolder();
        mPreviewHolder.addCallback(surfaceCallback);

        mCamera = getCameraInstance();
        if (mCamera != null)
            startPreview();
    }

    // code copied from http://developer.android.com/guide/topics/media/camera.html
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            //Log.e("foo", "Camera is not available");
        }
        return c; // returns null if camera is unavailable
    }

    private void configPreview(int width, int height) {
        //Log.v("foo", "configPreview");
        //Log.v("foo", mCamera == null ? "mCamera is null" : "mCamera is not null");
        //Log.v("foo", mPreviewHolder.getSurface() == null ? "mPreviewHolder.getSurface() is null" : "mPreviewHolder.getSurface() is not null");

        if ( mCamera != null && mPreviewHolder.getSurface() != null) {
            try {
                mCamera.setPreviewDisplay(mPreviewHolder);
            }
            catch (IOException e) {
                Toast.makeText(BrainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }


            //parameters dump  http://datawillconfess.blogspot.com/2013/11/google-glass-gdk.html


            if ( !mCameraConfigured ) {
                Camera.Parameters parameters = mCamera.getParameters();

                //parameters.setPictureSize(1024, 576);
                parameters.setPictureSize(320,240);
                parameters.setPreviewFpsRange(30000, 30000);
                parameters.setPreviewSize(640, 360);

                mCamera.setParameters(parameters);

                mCamera.setPreviewCallback(previewCallback);

                mCameraConfigured = true;
            }
        }
    }

    private void startPreview() {
        //Log.v("foo", "entering startPreview");

        if ( mCameraConfigured && mCamera != null ) {
            //Log.v("foo", "before calling mCamera.startPreview");
            mCamera.startPreview();
            mInPreview = true;
        }
    }

    /////////////////////////////////////
    //accessors
    /////////////////////////////////////

    private ImageAnalyzer GetImageAnalyzer() {return this.imageAnalyzer;}
    private TonePlayer GetTonePlayer() {return this.tonePlayer;}


    /////////////////////////////////////
    //callbacks
    /////////////////////////////////////

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated( SurfaceHolder holder ) {
            //Log.v("foo", "surfaceCreated");
        }

        public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
            //Log.v("foo", "surfaceChanged="+width+","+height);
            configPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed( SurfaceHolder holder ) {
            //Log.v("foo", "surfaceDestroyed");
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    };

    @Override
    public void onResume() {
        //Log.v("foo", "onResume");
        super.onResume();

        // Re-acquire the camera and start the preview.
        if (mCamera == null) {
            mCamera = getCameraInstance();
            if (mCamera != null) {
                //Log.v("foo", "mCamera!=null");
                configPreview(640, 360);
                startPreview();
            }
            //else
            //    Log.v("foo", "mCamera==null");
        }
    }

    @Override
    public void onPause() {
        //Log.v("foo", "onPause");
        if ( mInPreview ) {
            //Log.v("foo",  "mInPreview is true");
            mCamera.stopPreview();

            mCamera.release();
            mCamera = null;
            mInPreview = false;
        }
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.v("foo",  "onKeyDown");

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            //this.TakePicture();
            shouldGrabCameraFrame = true;
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
        {
            Log.i("foo", "dpad down");
        }

        return false;
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback()
    {

        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera)
        {
            if (shouldGrabCameraFrame == true)
            {
                shouldGrabCameraFrame = false;
                Log.i("foo", ">>>>>>>>> Grabbing camera frame" + "    num bytes  " + bytes.length);

                //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                //Log.i("foo", bitmap.getWidth() + "  " + bitmap.getHeight());

                // Convert to JPG
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                YuvImage yuvimage=new YuvImage(bytes, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
                byte[] jdata = baos.toByteArray();
                // Convert to Bitmap
                Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

                ArrayList<Integer> colors = imageAnalyzer.FindImagePalette(bmp);
                OnComplete_findImagePalette(colors);
            }
        }
    };

    private void OnComplete_findImagePalette(final ArrayList<Integer> colors)
    {
        Log.i("foo","OnComplete_findImagePalette");

        this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                UpdateTonePlayer_onPaletteFound(colors);
                PlayPalette();

            }
        });

    }


    /////////////////////////////////////
    //utilities
    /////////////////////////////////////


    public void UpdateTonePlayer_onPaletteFound(final ArrayList<Integer> colors)
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                tonePlayer.ClearTones();

                for (int i = 0; i < colors.size(); i++) {
                    int frequency = MapColorToFrequency(colors.get(i));
                    float duration = .5f;
                    tonePlayer.CreateTone(frequency, duration);
                }
            }
        });

    }

    private int MapColorToFrequency(int color)
    {
        //convert color to hsv
        float[] hsv = new float[ 3 ];
        Color.colorToHSV(color, hsv);
        float hue = hsv[0];
        float value = hsv[2];

        //Log.i("foo", "hue:  " + hue);
        //Log.i("foo", "value;  " + value);

        int numNotesInAScale = 12;

        //calculate the hue component (this gives the note)
        float maxHue = 360f;
        float hue_normalized = hue / maxHue;
        int hue_component = (int)(hue_normalized * numNotesInAScale);

        //Log.i("foo", "hue_component:  " + hue_component);

        //calculate the value component (this gives the scale)s
        int value_component_min = 3;
        int value_component_max = 7;
        int value_component = (int)(((value * (value_component_max - value_component_min)) + value_component_min) * numNotesInAScale);
        //value_component = 6*12;

        //Log.i("foo", "value_component:  " + value_component);

        //determine the associated piano key
        int pianoKey = hue_component + value_component;

        //Log.i("foo", "pianoKey: >>>>>>>>>>>>>>> " + pianoKey);

        //determine that piano key's frequency
        float exponent = (pianoKey - 49) / 12f;
        int frequency = (int)(Math.pow(2, exponent) * 440);

        //Log.d("foo", "frequency:   " + frequency);

        return frequency;
    }

    public void PlayPalette()
    {
        for (int i=0; i<5; i++)
        {
            int delay = 600*i;
            this.tonePlayer.PlayToneAfterDelay(i, delay);
            Log.i("foo", "play tone   " + i  +"   " + "after  " + delay);
        }
    }

    private void DisableAutoSleep()
    {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }




}
