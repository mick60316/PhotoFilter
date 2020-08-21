package com.example.photofilter;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.photo.Photo.edgePreservingFilter;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {


    private RadioGroup filerGroup;
    private static  String MikeTAG ="Mike";
    private ImageView sourceImage;
    private Mat srcImg;
    private Mat roiImage;
    private Rect roi =new Rect(700,400,500,500);
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
        BitmapDrawable bitmapDrawable = (BitmapDrawable) sourceImage.getDrawable();
        Bitmap bitmap =bitmapDrawable.getBitmap();
        Utils.bitmapToMat(bitmap,srcImg);
        //Imgproc.cvtColor(srcImg,srcImg,Imgproc.COLOR_BGR2RGB);

        roiImage =new Mat(srcImg,roi);
        updateImageview(roiImage);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void  linkUserInterface ()
    {

        filerGroup= findViewById(R.id.filer_group);
        sourceImage =findViewById(R.id.src_imageview);
        filerGroup.setOnCheckedChangeListener(this);


    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i)
            {
                case R.id.normal_filter_radio:
                    MikeLog("normal");
                    updateImageview(roiImage);
                    break;
                case R.id.cartoon_filter_radio:
                    MikeLog("cartoon");
                    cartoonFiler(roiImage);
                    //updateImageview(index);
                    break;
                case R.id.warm_filter_radio:
                    MikeLog("warm");
                    warmFilter(roiImage);
                    break;
                default:
                    break;
            }
    }
    private void MikeLog (String msg)
    {
        Log.i(MikeTAG,msg);
    }

    private void warmFilter (Mat inputMat)
    {

        float [] originalValue =new  float[] { 0,50,100,150,200,255 };
        //Changed points on Y-axis for red and blue channels
        float [] redValue = new float []{ 0,80,150,190,220,255 };
        float [] blueValue = new float[]{ 0,20,40,75,150,255 };
        Mat result = new Mat();
        List<Mat> lab_list = new ArrayList(3);
        Core.split(inputMat,lab_list);

        Mat lookupTable=new Mat(1, 256, CV_8U);

        //uchar* lut = lookupTable.ptr();

        for (int i = 0; i < 256; i++) {
            int j = 0;
            float xval = (float)i;
            while (xval > originalValue[j]) {
                j++;
            }
            if (xval == originalValue[j]) {
                lookupTable.put(0,i,redValue[j]);

                continue;
            }
            float slope = ((float)(redValue[j] - redValue[j - 1])) / ((float)(originalValue[j] - originalValue[j - 1]));
            float constant = redValue[j] - slope * originalValue[j];

            lookupTable.put(0,i,slope * xval + constant);
            //cout << i << " " << slope << " " << constant << " " << lut[i] << endl;
        }


        Mat maxIndex=lab_list.get(2).clone().setTo(new Scalar(255));
        Mat minIndex=lab_list.get(2).clone().setTo(new Scalar(0));


        Core.LUT(lab_list.get(0),lookupTable,lab_list.get(0));
        Core.min(lab_list.get(0),maxIndex,lab_list.get(0));
        Core.max(lab_list.get(0),minIndex,lab_list.get(0));

        Mat lookupTable2=new Mat(1, 256, CV_8U);


        //Linear Interpolation applied to get values for all the points on X-Axis
        for (int i = 0; i < 256; i++) {
            int j = 0;
            float xval = (float)i;
            while (xval > originalValue[j]) {
                j++;
            }
            if (xval == originalValue[j]) {
                lookupTable2.put(0,i,blueValue[j]);
                continue;
            }
            float slope = ((float)(blueValue[j] - blueValue[j - 1])) / ((float)(originalValue[j] - originalValue[j - 1]));
            float constant = blueValue[j] - slope * originalValue[j];
            lookupTable2.put(0,i,slope * xval + constant);
        }

        Core.LUT(lab_list.get(2),lookupTable2,lab_list.get(2));

        Core.min(lab_list.get(2),maxIndex,lab_list.get(2));
        Core.min(lab_list.get(2),minIndex,lab_list.get(2));

        //Merge the channels
        Core.merge(lab_list,result);
        updateImageview(result);
        //merge(channels, result);



    }
    private void cartoonFiler (Mat inputMat)
    {
        Mat img=inputMat.clone();
        Imgproc.cvtColor(img,img,Imgproc.COLOR_BGRA2BGR);
        Mat imgGray=new Mat();
        Imgproc.cvtColor(img,imgGray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgGray,imgGray, new Size(3,3),0);
        Imgproc.Laplacian(imgGray,imgGray, CV_8U,5);
        Mat index =imgGray.clone();
        index.setTo(new Scalar(255));
        Core.subtract(index,imgGray,imgGray);
        Mat edge_Mask =new Mat (imgGray.size(),imgGray.type());
        Imgproc.threshold(imgGray,edge_Mask,150,255,Imgproc.THRESH_BINARY);
        MikeLog(" "+CvType.CV_8UC3+" "+img.type());
        Mat out=new Mat();
        Imgproc.bilateralFilter(img,out,10,50,0);

        //edgePreservingFilter(img,img,0,20,0.4f);
        Mat output = Mat.zeros(imgGray.size(), CV_8UC3);
        Core.bitwise_and(out,out,output,edge_Mask);
        updateImageview(output);



    }

    private void updateImageview (Mat inputMat )
    {
        Bitmap newBp =Bitmap.createBitmap(inputMat.width(),inputMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inputMat,newBp);
        sourceImage.setImageBitmap(newBp);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
