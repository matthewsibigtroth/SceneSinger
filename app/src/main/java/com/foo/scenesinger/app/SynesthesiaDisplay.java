package com.foo.scenesinger.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by sibigtroth on 7/16/14.
 */





/**
 * Created by sibigtroth on 6/29/14.
 */

public class SynesthesiaDisplay extends RelativeLayout
{

    private BrainActivity brainActivity;
    private GestureDetector gestureDetector;
    private TonePlayer tonePlayer;
    private Random random;
    private ImageListenerDisplay imageListenerDisplay;


    public SynesthesiaDisplay(Context context)
    {
        super(context);

        this.brainActivity = (BrainActivity) context;

        this.Init();
    }

    private void Init()
    {
        this.random = new Random();

        this.CreateGestureDetector();
        this.CreateTonePlayer();
        this.CreateImageListenerDisplay();

        this.setVisibility(INVISIBLE);
        //this.setY(this.GetBrainActivity().H_SCREEN);
        this.setAlpha(0);
    }

    private void CreateGestureDetector()
    {
        this.gestureDetector = new GestureDetector(getContext());
        this.gestureDetector.setBaseListener(GestureDetectorBaseListener);
        this.gestureDetector.setScrollListener(GestureDetectorScrollListener);
        this.gestureDetector.setFingerListener(GestureDetectorFingerListener);
    }

    private void CreateTonePlayer() {this.tonePlayer = new TonePlayer(this.brainActivity);}

    private void CreateImageListenerDisplay()
    {
        this.imageListenerDisplay = new ImageListenerDisplay(getContext(), this.brainActivity);
        this.addView(this.imageListenerDisplay);

    }

    ///////////////////////////
    //accessors
    ///////////////////////////

    private BrainActivity GetBrainActivity() {return this.brainActivity;}
    private SynesthesiaDisplay GetSelf() {return this.GetBrainActivity().GetSynesthesiaDisplay();}
    private TonePlayer GetTonePlayer() {return this.tonePlayer;}
    private ImageListenerDisplay GetImageListenerDisplay() {return this.imageListenerDisplay;}


    ///////////////////////////
    //callbacks
    ///////////////////////////

    @Override
    public boolean dispatchGenericFocusedEvent(MotionEvent event) {
        if (isFocused()) {
            return this.gestureDetector.onMotionEvent(event);
        }
        return super.dispatchGenericFocusedEvent(event);
    }

    private GestureDetector.BaseListener GestureDetectorBaseListener = new GestureDetector.BaseListener()
    {
        @Override
        public boolean onGesture(Gesture gesture)
        {
            if (gesture == Gesture.TAP)
            {
                Log.d("foo", "onGesture TAP");
                return performClick();
            }
            return false;
        }
    };

    private GestureDetector.ScrollListener GestureDetectorScrollListener = new GestureDetector.ScrollListener()
    {
        @Override
        public boolean onScroll(float displacement, float delta, float velocity)
        {
            //Log.d("foo", "onScroll   " + "displacement:  " + String.valueOf(displacement) + "   delta:  " + String.valueOf(delta));
            GetImageListenerDisplay().OnTouchPadScroll(displacement, delta/1.7f);
            return false;
        }
    };

    private GestureDetector.FingerListener GestureDetectorFingerListener = new GestureDetector.FingerListener()
    {
        @Override
        public void onFingerCountChanged(int previousCount, int currentCount)
        {
            if (currentCount == 0) {OnTouchEnd();}
        }
    };


    private Animator.AnimatorListener HideListener = new Animator.AnimatorListener()
    {
        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationEnd(Animator animator)
        {
            setVisibility(INVISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    };

    private void OnTouchEnd()
    {
        Log.i("foo", "OnTouchEnd");
        this.GetImageListenerDisplay().GetPaletteDisplay().SnapColorCursor();
    }

    ///////////////////////////
    //utilities
    ///////////////////////////

    public void UpdateImageListenerDisplay_onPictureReady(final String filePath_image)
    {
        this.GetBrainActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                GetImageListenerDisplay().UpdateSourceImageImageView(filePath_image);
                GetImageListenerDisplay().invalidate();
            }
        });
    }

    public void UpdateImageListenerDisplay_onPaletteFound(final ArrayList<Integer> colors)
    {
        this.GetBrainActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                GetImageListenerDisplay().GetPaletteDisplay().UpdateColorDisplays(colors);
                GetImageListenerDisplay().GetPaletteDisplay().ResetColorCursor();
                GetImageListenerDisplay().GetPaletteDisplay().PlayPalette();
                GetImageListenerDisplay().invalidate();
            }
        });
    }

    public void UpdateTonePlayer_onPaletteFound(final ArrayList<Integer> colors)
    {
        this.GetBrainActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                GetTonePlayer().ClearTones();

                for (int i=0; i<colors.size(); i++)
                {
                    int frequency = GetImageListenerDisplay().GetPaletteDisplay().MapColorToFrequency(colors.get(i));
                    float duration = .5f;
                    GetTonePlayer().CreateTone(frequency, duration);
                }
            }
        });

    }

    public void ShowPaletteDisplay()
    {
        Log.i("foo", "ShowPaletteDisplay");
        this.GetImageListenerDisplay().ShowPaletteDisplay();
    }

    public void ShowPaletteDisplayAfterDelay(int delay)
    {
        this.GetImageListenerDisplay().ShowPaletteDisplayAfterDelay(delay);
    }

    public void HidePaletteDisplay()
    {
        Log.i("foo", "HidePaletteDisplay");
        this.GetImageListenerDisplay().HidePaletteDisplay();
    }

    public void Show()
    {
        this.GetBrainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setVisibility(VISIBLE);
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(GetSelf(), "y", getY(), 0).setDuration(250);
                objectAnimator.start();
            }
        });
    }

    public void ShowAfterDelay(final int delay)
    {
        this.GetBrainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setVisibility(VISIBLE);

                /*
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(GetSelf(), "y", getY(), 0).setDuration(250);
                objectAnimator.setStartDelay(delay_);
                objectAnimator.start();
                */

                setY(0);
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(GetSelf(), "alpha", 0, 1).setDuration(800);
                objectAnimator.setStartDelay(delay);
                objectAnimator.start();

            }
        });
    }

    public void Hide()
    {
        this.GetBrainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(GetSelf(), "y", getY(), GetBrainActivity().H_SCREEN).setDuration(250);
                objectAnimator.addListener(HideListener);
                objectAnimator.start();
            }
        });
    }







    class ImageListenerDisplay extends RelativeLayout
    {

        private BrainActivity brainActivity;
        private ImageView sourceImageImageView;
        private PaletteDisplay paletteDisplay;
        private ArrayList<PixelDisplay> pixelDisplays;
        private Random random;


        public ImageListenerDisplay(Context context, BrainActivity brainActivity)
        {
            super(context);

            this.brainActivity = brainActivity;

            this.Init();
        }

        private void Init()
        {
            this.random = new Random();

            this.CreateSourceImageImageView();
            this.CreatePixelDisplays();
            this.CreatePaletteDisplay();
        }

        private void CreateSourceImageImageView()
        {
            this.sourceImageImageView = new ImageView(getContext());
            this.addView(this.sourceImageImageView);
            this.sourceImageImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        private void CreatePaletteDisplay()
        {
            this.paletteDisplay = new PaletteDisplay(getContext(), this.brainActivity);
            this.addView(this.paletteDisplay);
            //this.paletteDisplay.setY(340 - this.paletteDisplay.paletteBackground.h/2);
            this.paletteDisplay.setY(340 + this.paletteDisplay.paletteBackground.h/2);
        }

        private void CreatePixelDisplays()
        {
            int numPixelDisplays = 5;
            this.pixelDisplays = new ArrayList<PixelDisplay>();
            for (int i=0; i<numPixelDisplays; i++)
            {
                int pixelSize = 50;
                int strokeSize = 5;
                PixelDisplay pixelDisplay = new PixelDisplay(getContext(), Color.WHITE, pixelSize, pixelSize, strokeSize);
                this.pixelDisplays.add(pixelDisplay);
                this.addView(pixelDisplay);
                pixelDisplay.getLayoutParams().width = pixelSize;
                pixelDisplay.getLayoutParams().height = pixelSize;
                pixelDisplay.setAlpha(0);
            }
        }


        ///////////////////////////
        //accessors
        ///////////////////////////

        private BrainActivity GetBrainActivity() {return this.brainActivity;}
        private ImageView GetSourceImageImageView() {return this.sourceImageImageView;}
        private ImageAnalyzer GetImageAnalyzer() {return this.GetBrainActivity().GetImageAnalyzer();}
        private PaletteDisplay GetPaletteDisplay() {return this.paletteDisplay;}
        private SynesthesiaDisplay GetSynesthesiaDisplay() {return this.GetBrainActivity().GetSynesthesiaDisplay();}
        private TonePlayer GetTonePlayer() {return this.GetSynesthesiaDisplay().GetTonePlayer();}
        private ArrayList<PixelDisplay> GetPixelDisplays() {return this.pixelDisplays;}

        ///////////////////////////
        //callbacks
        ///////////////////////////

        public void OnTouchPadScroll(float displacement, float delta)
        {
            this.GetPaletteDisplay().MoveColorCursor(delta);
            this.GetPaletteDisplay().UpdateCurrentColorDisplay();
        }

        public void OnChange_paletteDisplayCurrentColor(int index_colorDisplay_new)
        {
            this.UpdatePixelDisplay(index_colorDisplay_new);
        }


        ///////////////////////////
        //utilities
        ///////////////////////////

        public void UpdateSourceImageImageView(String filePath_image)
        {
            //float scale_new = .25f;
            //Bitmap bitmap = this.GetImageAnalyzer().ResizeImage(filePath_image, scale_new);
            Bitmap bitmap = BitmapFactory.decodeFile(filePath_image);
            this.GetSourceImageImageView().setImageBitmap(bitmap);
        }

        public void ShowPaletteDisplay()
        {
            this.GetBrainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int y_start = 340 + paletteDisplay.paletteBackground.h/2;
                    int y_new = 340 - paletteDisplay.paletteBackground.h/2;
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(GetPaletteDisplay(), "y", y_start, y_new).setDuration(250);
                    objectAnimator.start();
                }
            });
        }

        public void ShowPaletteDisplayAfterDelay(final int delay)
        {
            this.GetBrainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int y_start = 340 + paletteDisplay.paletteBackground.h/2;
                    int y_new = 340 - paletteDisplay.paletteBackground.h/2;
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(GetPaletteDisplay(), "y", y_start, y_new).setDuration(250);
                    objectAnimator.setStartDelay(delay);
                    objectAnimator.start();
                }
            });
        }


        public void HidePaletteDisplay()
        {
            this.GetBrainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int y_start = 340 - paletteDisplay.paletteBackground.h/2;
                    int y_new = 340 + paletteDisplay.paletteBackground.h/2;
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(GetPaletteDisplay(), "y", y_start, y_new).setDuration(250);
                    objectAnimator.start();
                }
            });
        }

        public void UpdatePixelDisplay(final int index_pixelDisplay)
        {
            this.GetBrainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageAnalyzer.Point point = GetImageAnalyzer().FindRepresentativeClusterPointForGivenClusterIndex(index_pixelDisplay);
                    if (point == null) {return;}
                    int color = Color.rgb(point.x, point.y, point.z);

                    float w_pixelDisplay = (float)GetPixelDisplays().get(index_pixelDisplay).w;
                    float h_pixelDisplay = (float)GetPixelDisplays().get(index_pixelDisplay).h;
                    float x_new = point.x_pixel - w_pixelDisplay/2f;
                    float y_new = point.y_pixel - h_pixelDisplay/2f;
                    GetPixelDisplays().get(index_pixelDisplay).color = color;
                    GetPixelDisplays().get(index_pixelDisplay).setX(x_new);
                    GetPixelDisplays().get(index_pixelDisplay).setY(y_new);
                    GetPixelDisplays().get(index_pixelDisplay).setAlpha(1);
                    GetPixelDisplays().get(index_pixelDisplay).invalidate();

                    //Log.i("foo", "pixel  x:  " + point.x_pixel + "  y  " + point.y_pixel);

                    ObjectAnimator objectAnimator_scaleX = ObjectAnimator.ofFloat(GetPixelDisplays().get(index_pixelDisplay), "scaleX", 0, 1).setDuration(250);
                    ObjectAnimator objectAnimator_scaleY = ObjectAnimator.ofFloat(GetPixelDisplays().get(index_pixelDisplay), "scaleY", 0, 1).setDuration(250);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(objectAnimator_scaleX, objectAnimator_scaleY);
                    animatorSet.start();

                    ObjectAnimator objectAnimator_alpha = ObjectAnimator.ofFloat(GetPixelDisplays().get(index_pixelDisplay), "alpha", 1, 0).setDuration(1200);
                    objectAnimator_alpha.setStartDelay(250);
                    objectAnimator_alpha.start();
                }
            });
        }

        public void UpdatePixelDisplayAfterDelay(final int index_pixelDisplay, final int delay)
        {
            Handler delayHandler= new Handler();
            Runnable r=new Runnable()
            {
                @Override
                public void run()
                {
                    UpdatePixelDisplay(index_pixelDisplay);

                }

            };
            delayHandler.postDelayed(r, delay);
        }

    }


    class PaletteDisplay extends RelativeLayout
    {

        private BrainActivity brainActivity;
        private ArrayList<ColorDisplay> colorDisplays;
        private PaletteBackground paletteBackground;
        private ColorCursor colorCursor;
        private int deltaX_betweenColorDisplays;
        private int index_currentColorDisplay;

        public PaletteDisplay(Context context, BrainActivity brainActivity)
        {
            super(context);

            this.brainActivity = brainActivity;

            this.Init();
        }

        private void Init()
        {
            this.deltaX_betweenColorDisplays = 10;
            this.index_currentColorDisplay = 0;

            this.CreateBackground();
            this.CreateColorDisplays();
            this.CreateColorCursor();
        }

        private void CreateBackground()
        {
            this.paletteBackground = new PaletteBackground(getContext());
            this.addView(paletteBackground);
        }

        private void CreateColorDisplays()
        {
            int paletteSize = 5;
            this.colorDisplays = new ArrayList<ColorDisplay>();
            for (int i=0; i<paletteSize; i++)
            {
                ColorDisplay colorDisplay = new ColorDisplay(getContext());
                this.colorDisplays.add(colorDisplay);
                this.addView(colorDisplay);
                int x = i * (colorDisplay.w + this.deltaX_betweenColorDisplays);
                int y = (int)((this.paletteBackground.h/2f) - colorDisplay.h/2f);
                colorDisplay.setX(x);
                colorDisplay.setY(y);
            }
        }

        private void CreateColorCursor()
        {
            int w = this.GetColorDisplays().get(0).w;
            int h = (int)((this.paletteBackground.h - this.GetColorDisplays().get(0).h)/2f);
            this.colorCursor = new ColorCursor(getContext(), w, h);
            this.addView(this.colorCursor);
            this.colorCursor.setX(0);
            this.colorCursor.setY(this.paletteBackground.h - h);
        }


        ///////////////////////////
        //accessors
        ///////////////////////////

        private BrainActivity GetBrainActivity() {return this.brainActivity;}
        private ArrayList<ColorDisplay> GetColorDisplays() {return this.colorDisplays;}
        private SynesthesiaDisplay GetSynesthesiaDisplay() {return this.GetBrainActivity().GetSynesthesiaDisplay();}
        private TonePlayer GetTonePlayer() {return this.GetSynesthesiaDisplay().GetTonePlayer();}
        private ColorCursor GetColorCursor() {return this.colorCursor;}
        private ImageListenerDisplay GetImageListenerDisplay() {return this.GetSynesthesiaDisplay().GetImageListenerDisplay();}


        ///////////////////////////
        //callbacks
        ///////////////////////////

        private void OnChange_currentColorDisplay(int index_colorDisplay_orig, int index_colorDisplay_new)
        {
            //Log.i("foo", "OnChange_currentColorDisplay:  " + this.index_currentColorDisplay);
            this.PlayPaletteColor(this.index_currentColorDisplay);

            this.ScaleUpColorDisplay(index_colorDisplay_new);
            this.ScaleDownColorDisplay(index_colorDisplay_orig);
            this.GetImageListenerDisplay().OnChange_paletteDisplayCurrentColor(index_colorDisplay_new);
        }

        private void ScaleUpColorDisplay(int index_colorDisplay)
        {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.GetColorDisplays().get(index_colorDisplay), "scaleY", this.GetColorDisplays().get(index_colorDisplay).getScaleY(), 1.1f).setDuration(150);
            objectAnimator.start();
        }

        private void ScaleDownColorDisplay(int index_colorDisplay)
        {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.GetColorDisplays().get(index_colorDisplay), "scaleY", this.GetColorDisplays().get(index_colorDisplay).getScaleY(), 1.0f).setDuration(150);
            objectAnimator.start();
        }



        ///////////////////////////
        //utilities
        ///////////////////////////

        public void UpdateColorDisplays(ArrayList<Integer> colors)
        {
            for (int i=0; i<this.GetColorDisplays().size(); i++)
            {
                int frequency = this.MapColorToFrequency(colors.get(i));
                this.GetColorDisplays().get(i).frequency = frequency;
                this.GetColorDisplays().get(i).color = colors.get(i);
                this.GetColorDisplays().get(i).setScaleY(1);
                this.GetColorDisplays().get(i).invalidate();
            }
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
            for (int i=0; i<this.GetColorDisplays().size(); i++)
            {
                int frequency = this.GetColorDisplays().get(i).frequency;
                //int frequency = this.random.nextInt(4000);
                float duration = .5f;
                int delay = 600*i + 1000;
                this.GetTonePlayer().PlayToneAfterDelay(i, delay);
                this.MoveColorCursorToColorDisplayAfterDelay(i, delay);
                this.AnimateColorDisplaysAfterDelay(i,delay);
                this.GetImageListenerDisplay().UpdatePixelDisplayAfterDelay(i, delay);
            }
        }

        private void PlayPaletteColor(int index_paletteColor)
        {
            this.GetTonePlayer().PlayTone(index_paletteColor);
        }

        public void MoveColorCursor(float delta)
        {
            float x_max = this.GetBrainActivity().W_SCREEN - this.GetColorDisplays().get(0).w - this.deltaX_betweenColorDisplays;
            float x_min = 0;
            float x_new = this.GetColorCursor().getX();
            x_new = x_new + delta;
            if (x_new > x_max) {x_new = x_max;}
            else if (x_new < x_min) {x_new = x_min;}
            this.GetColorCursor().setX(x_new);
        }

        public void UpdateCurrentColorDisplay()
        {
            int index_currentColorDisplay_orig = (int)this.index_currentColorDisplay;

            float x_colorCursor = this.GetColorCursor().getX();
            float minDistance = 1000000;
            int index_closestColorDisplay = -1;

            for (int i=0; i<this.GetColorDisplays().size(); i++)
            {
                float x_colorDisplay = this.GetColorDisplays().get(i).getX();
                float distance = Math.abs(x_colorDisplay - x_colorCursor);
                if (distance < minDistance)
                {
                    minDistance = distance;
                    index_closestColorDisplay = i;
                }
            }

            if (this.index_currentColorDisplay != index_closestColorDisplay)
            {
                this.index_currentColorDisplay = index_closestColorDisplay;
                this.OnChange_currentColorDisplay(index_currentColorDisplay_orig, this.index_currentColorDisplay);
            }

            //Log.i("foo", "index_closestColorDisplay:   " + index_closestColorDisplay);
        }

        public void SnapColorCursor()
        {
            this.MoveColorCursorToColorDisplay(this.index_currentColorDisplay);
        }

        private void MoveColorCursorToColorDisplay(int index_colorDisplay)
        {
            float x_new = this.GetColorDisplays().get(index_colorDisplay).getX();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.GetColorCursor(), "x", this.GetColorCursor().getX(), x_new).setDuration(100);
            objectAnimator.start();
        }

        private void MoveColorCursorToColorDisplayAfterDelay(final int index_colorDisplay, int delay)
        {
            Handler delayHandler= new Handler();
            Runnable r=new Runnable()
            {
                @Override
                public void run()
                {
                    MoveColorCursorToColorDisplay(index_colorDisplay);
                }

            };
            delayHandler.postDelayed(r, delay);
        }

        private void AnimateColorDisplaysAfterDelay(final int index_colorDisplay, int delay)
        {
            Handler delayHandler= new Handler();
            Runnable r=new Runnable()
            {
                @Override
                public void run()
                {
                    int index_colorDisplay_new = index_colorDisplay;
                    int index_colorDisplay_orig = index_colorDisplay - 1;
                    ScaleUpColorDisplay(index_colorDisplay_new);
                    if (index_colorDisplay_orig >= 0) {ScaleDownColorDisplay(index_colorDisplay_orig);}

                }

            };
            delayHandler.postDelayed(r, delay);

        }


        public void ResetColorCursor()
        {
            this.SnapColorCursor();
        }

    }

    class PaletteBackground extends View
    {

        public int h;
        private Paint paint;

        public PaletteBackground(Context context)
        {
            super(context);

            this.Init();
        }

        private void Init()
        {
            this.h = 40;
            this.paint = new Paint();
        }

        @Override
        public void onDraw(Canvas canvas)
        {
            this.paint.setStrokeWidth(0);
            this.paint.setColor(0x99000000);
            canvas.drawRect(0, 0, 640, this.h, this.paint);
        }

    }



    class ColorDisplay extends View
    {

        private Paint paint = new Paint();
        public int color;
        public int w;
        public int h;
        public int frequency;

        public ColorDisplay(Context context)
        {
            super(context);

            this.Init();
        }

        private void Init()
        {
            this.w = (640/5) - 10;
            this.h = 25;
        }

        @Override
        public void onDraw(Canvas canvas)
        {
            this.paint.setStrokeWidth(0);
            //this.paint.setColor(Color.CYAN);
            this.paint.setColor(this.color);
            canvas.drawRect(0, 0, this.w, this.h, this.paint);
        }
    }



    class ColorCursor extends View
    {

        public int w;
        public int h;
        private Paint paint = new Paint();

        public ColorCursor(Context context, int w, int h)
        {
            super(context);

            this.w = w;
            this.h = h;

            this.Init();
        }

        private void Init()
        {

        }

        @Override
        public void onDraw(Canvas canvas)
        {
            this.paint.setStrokeWidth(0);
            this.paint.setColor(Color.WHITE);
            //this.paint.setColor(this.color);
            canvas.drawRect(0, 0, this.w, this.h, this.paint);
        }
    }

    class PixelDisplay extends View
    {

        public int color;
        public int w;
        public int h;
        public int strokeSize;
        private Paint paint = new Paint();

        public PixelDisplay(Context context, int color, int w, int h, int strokeSize)
        {
            super(context);

            this.color = color;
            this.w = w;
            this.h = h;
            this.strokeSize = strokeSize;

            this.Init();

        }

        private void Init()
        {

        }

        @Override
        public void onDraw(Canvas canvas)
        {


            this.paint.setColor(0x33000000);
            //this.paint.setColor(Color.DKGRAY);
            //this.paint.setStrokeWidth(this.strokeSize);
            //this.paint.setStyle(Paint.Style.STROKE);
            //canvas.drawRect(0, 0, this.w, this.h, this.paint);
            RectF rectangle_border = new RectF(0, 0, this.w, this.h);
            canvas.drawRoundRect(rectangle_border, 40, 40, this.paint);

            this.paint.setAlpha(1);
            this.paint.setColor(this.color);
            //this.paint.setColor(Color.CYAN);
            this.paint.setStrokeWidth(0);
            this.paint.setStyle(Paint.Style.FILL);
            //canvas.drawRect((float)this.strokeSize, (float)this.strokeSize, size_interior, size_interior, this.paint);
            RectF rectangle_interior = new RectF(3, 3, 46, 46);
            canvas.drawRoundRect(rectangle_interior, 20, 20, this.paint);


        }


    }

}


