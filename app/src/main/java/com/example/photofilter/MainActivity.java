package com.example.photofilter;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.warpPerspective;
import static org.opencv.photo.Photo.edgePreservingFilter;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {


    private RadioGroup filerGroup;
    private SeekBar [] seekBars=new SeekBar[7];
    private ScaleGestureDetector mScaleGestureDetector;

    private static  String MikeTAG ="Mike";
    private ImageView sourceImage;
    private Mat srcImg,filerImage,processImage;
    private Mat brightnessMask;


    private Rect roi =new Rect(0,0,700,700);

    private float mScaleFactor = 1.0f;

    private static PointF touchScreenStartPtArr[] = new PointF[10];
    private static PointF touchScreenStopPtArr[] = new PointF[10];
    private static PointF touchScreenCurrPtArr[] = new PointF[10];

    private Button defalutButton;

    private int xAngle =90;
    private int yAngle =90;
    private int zAngle =90;
    private int [] seekBarInitValue = new int [seekBars.length];


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linkUserInterface();

        // Example of a call to a native method

    }
    private void setDefault ()
    {

        for (int i =0;i<seekBars.length ;i++) {
            seekBars[i].setProgress(seekBarInitValue[i]);

        }

        if (filerGroup.getCheckedRadioButtonId()!=R.id.normal_filter_radio)
        {
            filerGroup.check(R.id.normal_filter_radio);
            filerImage =null;
            updateImageview(processImage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug())
        {

            MikeLog("Opencv load successful");
        }
        else {
            MikeLog("Opencv load error");

        }

        srcImg=new Mat();

        processImage =new Mat();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) sourceImage.getDrawable();
        Bitmap bitmap =bitmapDrawable.getBitmap();
        Utils.bitmapToMat(bitmap,srcImg);
        processImage =srcImg.clone();

        //Imgproc.cvtColor(srcImg,srcImg,Imgproc.COLOR_BGR2RGB);
        Mat A1=new Mat(new Size(4,3),CV_32F);
        A1.put(0,0,new double[]{1.0,2.0});
        A1.put(0,1,1);


        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        ImageProcess.getRotationImage(srcImg,xAngle,yAngle,zAngle);
      //  roiImage =new Mat(srcImg,roi);
        updateImageview(processImage);


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void  linkUserInterface ()
    {

        SeekBarListener seekBarListener =new SeekBarListener();
        ImageViewTouchListener imageViewTouchListener =new ImageViewTouchListener();

        filerGroup= findViewById(R.id.filer_group);
        sourceImage =findViewById(R.id.src_imageview);
        sourceImage.setOnTouchListener(imageViewTouchListener);


        seekBars[0] =findViewById(R.id.contrast_bar);
        seekBars[1] =findViewById(R.id.brightness_bar);
        seekBars[2] =findViewById(R.id.rotate_x_bar);
        seekBars[3] =findViewById(R.id.rotate_y_bar);
        seekBars[4] =findViewById(R.id.rotate_z_bar);
        seekBars[5] =findViewById(R.id.saturation_bar);
        seekBars[6] =findViewById(R.id.sharp_bar);


        for (int i =0;i<seekBars.length ;i++)
        {
            seekBars[i].setOnSeekBarChangeListener(seekBarListener);
            seekBarInitValue[i] =seekBars[i].getProgress();

        }
        filerGroup.setOnCheckedChangeListener(this);

        defalutButton =findViewById(R.id.testbt);
        defalutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
            }
        });


    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i)
            {
                case R.id.normal_filter_radio:
                    MikeLog("normal");
                    filerImage =null;
                    updateImageview(processImage);
                    break;
                case R.id.cartoon_filter_radio:
                    MikeLog("cartoon");
                    filerImage =ImageProcess.CartoonFilter(processImage);
                    updateImageview(filerImage);
                    //updateImageview(index);
                    break;
                case R.id.warm_filter_radio:
                    MikeLog("warm");
                    filerImage =ImageProcess.WarmFilter(processImage);
                    updateImageview(filerImage);
                    break;
                default:
                    break;
            }
    }
    private void MikeLog (String msg)
    {
        Log.i(MikeTAG,msg);
    }



    private void updateImageview (Mat inputMat)
    {
        //inputMat =new Mat (inputMat,roi);
        Bitmap newBp =Bitmap.createBitmap(inputMat.width(),inputMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inputMat,newBp);
        sourceImage.setImageBitmap(newBp);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener
    {


        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            MikeLog("onProgressChanged");
            if (filerGroup.getCheckedRadioButtonId()!=R.id.normal_filter_radio)
            {
                filerGroup.check(R.id.normal_filter_radio);
                filerImage =null;
                updateImageview(processImage);
            }
            switch(seekBar.getId())
            {
                case R.id.sharp_bar:
                    processImage=ImageProcess.getSharpImage(srcImg,i);

                    break;
                case R.id.brightness_bar:
                    processImage=ImageProcess.getBrightnessImage(srcImg,i-100);

                    break;
                case R.id.contrast_bar:
                    processImage=ImageProcess.getContrastImage(srcImg,i-100);

                    break;

                case R.id.saturation_bar:
                    processImage=(ImageProcess.getSaturationImage(srcImg,i-100));
                    break;

                case R.id.rotate_x_bar:
                    xAngle=90+i-12;
                    processImage=(ImageProcess.getRotationImage(srcImg,xAngle,yAngle,zAngle));

                    break;
                case R.id.rotate_y_bar:
                    yAngle=90+i-12;
                    processImage=(ImageProcess.getRotationImage(srcImg,xAngle,yAngle,zAngle));

                    break;
                case R.id.rotate_z_bar:
                    zAngle=90+i;
                    processImage=(ImageProcess.getRotationImage(srcImg,xAngle,yAngle,zAngle));
                    break;

            }
            updateImageview(processImage);


        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class ImageViewTouchListener implements View.OnTouchListener
    {
        float pointStartX=0,pointStartY=0;
        float pointCurrentX=0,pointCurrentY=0;
        float roiStartX =0,roiStartY= 0;


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //mScaleGestureDetector.onTouchEvent(motionEvent);

            int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
            int pointerIndex = (motionEvent.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            int fingerId = motionEvent.getPointerId(pointerIndex);

            MikeLog("pointerIndex "+pointerIndex +" "+motionEvent.getX(pointerIndex)+" "+motionEvent.getY(pointerIndex));



            switch (motionEvent.getActionMasked())
            {

                case  MotionEvent.ACTION_DOWN:

                    pointStartX =motionEvent.getRawX();
                    pointStartY=motionEvent.getRawY();
                    roiStartX =roi.x;
                    roiStartY=roi.y;
                    //MikeLog("DOWN DOWN");
                    updateImageview(ImageProcess.getOnlyRotatedImage());
                    break;
                case MotionEvent.ACTION_MOVE:

                    pointCurrentX=motionEvent.getRawX();
                    pointCurrentY=motionEvent.getRawY();

                    int newPosX=(int)(roiStartX-(pointCurrentX-pointStartX));
                    int newPosY =(int)(roiStartY -(pointCurrentY-pointStartY));
                    if (newPosX >0 && newPosX <srcImg.width()-700*mScaleFactor) roi.x=newPosX;
                    if (newPosY >0 && newPosY <srcImg.height()-700*mScaleFactor) roi.y=newPosY;

                    MikeLog(" "+(newPosX)+" "+(newPosY)+" "+srcImg.width() +" "+srcImg.height());
                    updateImageview(ImageProcess.getOnlyRotatedImage());

                    break;
                case MotionEvent.ACTION_UP:
                    //MikeLog("UPUPUP");
                    if (filerImage ==null) updateImageview(processImage);
                    else updateImageview(filerImage);
                    break;

            }
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();

            float newFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 2.0f));

            if (roi.x+500*mScaleFactor <srcImg.width()-10 && roi.y+500*mScaleFactor<srcImg.height()-10) {
                mScaleFactor = newFactor;
                roi.height = (int) (500 * mScaleFactor);
                roi.width = (int) (500 * mScaleFactor);
            }



            return true;
        }
    }


}
