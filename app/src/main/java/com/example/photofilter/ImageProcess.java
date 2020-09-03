package com.example.photofilter;


import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static org.opencv.core.Core.LUT;
import static org.opencv.core.Core.merge;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2Lab;
import static org.opencv.imgproc.Imgproc.COLOR_HSV2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_Lab2BGR;
import static org.opencv.imgproc.Imgproc.INTER_LANCZOS4;
import static org.opencv.imgproc.Imgproc.INTER_LINEAR;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.warpPerspective;

public class ImageProcess {
    private static final String TAG = "MarkImageProcess1";
    private static final float INITIAL_CONTRAST = 1.0f;
    private static final float INITIAL_BRIGHTNESS = 0.0f;
    private static final int INITIAL_SATURATION = 0;
    private static final int INITIAL_SHARP = 0;
    private static float ContrastValue = INITIAL_CONTRAST;
    private static float BrightnessValue = INITIAL_BRIGHTNESS;
    private static int SaturationValue = INITIAL_SATURATION;
    private static int sharpValue = INITIAL_SHARP;
    private static double xAngle,yAngle,zAngle;
    private static double distance;
    private static Mat rotatedImage;


    public static Mat getRotationImage(Mat _src, double _xAngle, double _yAngle, double _zAngle,double dz)
    {
        /*
            set image rotation
            argv:
                _src: input image which have three channel
                xAngle :angle for x axis
                yAngle :angle for y axis
                zAngle :angle for z axis
             return :
                a rotated image

        */

        double maxValue = max(abs( 90- _xAngle),abs(90-_yAngle));
        maxValue=max(maxValue,abs(90- _zAngle));

        distance=maxValue*-10+200;
        Log.i("test",""+_xAngle+" "+_yAngle+" "+_zAngle);

        xAngle=_xAngle;
        yAngle=_yAngle;
        zAngle=_zAngle;
        //distance =dz;
        return ProcessImage(_src);
    }
    public static  Mat getOnlyRotatedImage()
    {
        /*
        get image which only rotated
         */
        return rotatedImage;
    }


    public static Mat getSaturationImage(Mat _src, int _value) {
        /*
         set image saturation
         Argv:
            _src : input image which is three channel
            _value: input saturation value
         */
        SaturationValue = 2 * _value;
        Log.e(TAG, "Saturation = " + SaturationValue);
        return ProcessImage(_src);
    }

    public static Mat getContrastImage(Mat src, float _value) {
        /*
         set image contrast
         Argv:
            _src : input image which is three channel
            _value: input contrast value
         */


        ContrastValue = 1.0f * (1.8f * _value + 180) / 200 + 0.2f;
        Log.e(TAG, "ImageContrast = " + ContrastValue);
        return ProcessImage(src);
    }

    public static Mat getBrightnessImage(Mat src, int _value) {
            /*
         set image brightness
         Argv:
            _src : input image which is three channel
            _value: input brightness value
         */


        BrightnessValue = _value * 2.5f;
        Log.e(TAG, "ImageBrightness = " + BrightnessValue);
        return ProcessImage(src);
    }

    public static Mat getSharpImage(Mat _src, int value) {

        /*
         set image sharp
         Argv:
            _src : input image which is three channel
            _value: input sharp value
         */

        sharpValue = value;
        return ProcessImage(_src);
    }




    public static Mat CartoonFilter (Mat src)
    {
        /*
        Argv:
            inputMat: A three channel image

        return:
            a three channel image after cartoon filer

        */
        Mat img=src.clone();
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

        Mat out=new Mat();
        Imgproc.bilateralFilter(img,out,10,50,0);

        //edgePreservingFilter(img,img,0,20,0.4f);
        Mat output = Mat.zeros(imgGray.size(), CV_8UC3);
        Core.bitwise_and(out,out,output,edge_Mask);
        return output;
    }

    public static  Mat MoonFilter (Mat inputMat)
    {

        float [] originValues={0, 15, 30, 50, 70, 90, 120, 160, 180, 210, 255 };
        float [] values=      {0, 0, 5, 15, 60, 110, 150, 190, 210, 230, 255 };
        Mat lookupTable=new Mat(1, 256, CV_8U);

        for(int i=0; i<256; i++){
            int j=0;
            float a = i;
            while (a>originValues[j]){
                j++;
            }
            if(a == originValues[j]){

                lookupTable.put(0,i,values[j]);


                continue;
            }
            float slope = ((float)(values[j] - values[j-1]))/((float)(originValues[j] - originValues[j-1]));
            float constant = values[j] - slope * originValues[j];

            lookupTable.put(0,i,slope * a + constant);
        }
        Mat labMat =new Mat ();
        cvtColor(inputMat,labMat, COLOR_BGR2Lab);

        List<Mat> lab_list = new ArrayList(3);
        Core.split(labMat,lab_list);
        LUT(lab_list.get(0),lookupTable,lab_list.get(0));
        merge(lab_list,labMat);
        Mat hsvMat=new Mat();
        cvtColor(labMat,hsvMat,COLOR_Lab2BGR);
        cvtColor(hsvMat,hsvMat,COLOR_BGR2HSV);
        Core.split(hsvMat,lab_list);
        Core.multiply(lab_list.get(1),new Scalar(0.01f),lab_list.get(1));
        Core.min(lab_list.get(1), new Scalar(255), lab_list.get(1));
        Core.max(lab_list.get(1), new Scalar(0), lab_list.get(1));
        Mat output=new Mat();
        merge(lab_list,output);
        cvtColor(output, output, COLOR_HSV2BGR);
        return output;




    }

    public static Mat WarmFilter (Mat inputMat)
    {
        /*
        Argv:
            inputMat: A three channel image

        return:
            a three channel image after warm filer

        */
        float [] originalValue =new  float[] { 0,50,100,150,200,255 };
        //Changed points on Y-axis for red and blue channels
        float [] redValue = new float []{ 0,80,150,190,220,255 };
        float [] blueValue = new float[]{ 0,20,40,75,150,255 };
        Mat result = new Mat();
        List<Mat> lab_list = new ArrayList(3);
        Core.split(inputMat,lab_list);

        Mat lookupTable=new Mat(1, 256, CV_8U);

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

        }
        Mat maxIndex=lab_list.get(2).clone().setTo(new Scalar(255));
        Mat minIndex=lab_list.get(2).clone().setTo(new Scalar(0));
        LUT(lab_list.get(0),lookupTable,lab_list.get(0));
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
        LUT(lab_list.get(2),lookupTable2,lab_list.get(2));
        Core.min(lab_list.get(2),maxIndex,lab_list.get(2));
        Core.min(lab_list.get(2),minIndex,lab_list.get(2));
        Core.merge(lab_list,result);
        return result;
    }

    private static Mat sharpImage(Mat src, int value) {
        //value*=1.5f;
        if (value == INITIAL_SHARP) return src;
        double sigma = 3;
        int threshold = 2;
        float amount = value / 100.0f;

        Mat imgBlurred = new Mat();
        Imgproc.GaussianBlur(src, imgBlurred, new Size(), sigma, sigma);
        Mat lowContrastMask = new Mat();
        Core.absdiff(src, imgBlurred, lowContrastMask);

        List<Mat> mat_list = new ArrayList<>(3);
        Core.split(lowContrastMask, mat_list);

        for (int i = 0; i < mat_list.size(); i++) {
            Imgproc.threshold(mat_list.get(i), mat_list.get(i), threshold, 255, Imgproc.THRESH_BINARY_INV);
        }
        Core.merge(mat_list, lowContrastMask);
        //return lowContrastMask;
        Mat dst = new Mat();
        Core.addWeighted(src, 1 + amount, imgBlurred, -amount, 0, dst);

        src.copyTo(dst, lowContrastMask);
        return dst;
    }

    private static Mat ContrastAndBrightness(Mat _src, float _value1, float _value2) {
        if (_value1 == INITIAL_CONTRAST && _value2 == INITIAL_BRIGHTNESS) return _src;
        Mat dst = Mat.zeros(_src.size(), _src.type());
        _src.convertTo(dst, -1, ContrastValue, BrightnessValue);
        return dst;
    }

    private static Mat Saturation(Mat _src, int _value) {
        if (_value == INITIAL_SATURATION) return _src;
        Mat hsv = Mat.zeros(_src.size(), _src.type());
        Imgproc.cvtColor(_src, hsv, Imgproc.COLOR_RGB2HSV);
        Mat index = hsv.clone();
        index.setTo(new Scalar(0, Math.abs(SaturationValue), 0));
        Mat output = new Mat();
        if (_value >= 0)
            Core.add(hsv, index, output);
        else
            Core.subtract(hsv, index, output);
        Mat rgb = Mat.zeros(_src.size(), _src.type());
        Imgproc.cvtColor(output, rgb, Imgproc.COLOR_HSV2RGB);
        return rgb;
    }



    private static Mat RotateImage (Mat src,double xAngle,double yAngle,double zAngle,double dz)
    {

        double dx =0,dy=0,f=200.;
        xAngle = (xAngle - 90.)*Math.PI / 180.;
        yAngle = (yAngle - 90.)*Math.PI / 180.;
        zAngle = (zAngle - 90.)*Math.PI  / 180.;
        // get width and height for ease of use in matrices
        double w = (double)src.cols();
        double h = (double)src.rows();

        double []A1Data=new double[]{
                1.,0,-w/2.,
                0,1.,-h/2.,
                0,0,0,
                0,0,1.};
        double [] RXData =new double[]{
                1., 0, 0, 0,
                0, cos(xAngle), -sin(xAngle), 0,
                0, sin(xAngle), cos(xAngle), 0,
                0, 0, 0, 1.};
        double [] RYData =new double[]{
                cos(yAngle), 0, -sin(yAngle), 0,
                0, 1., 0, 0,
                sin(yAngle), 0, cos(yAngle), 0,
                0, 0, 0, 1.};
        double [] RZData =new double[]{
                cos(zAngle), -sin(zAngle), 0, 0,
                sin(zAngle), cos(zAngle), 0, 0,
                0, 0, 1., 0,
                0, 0, 0, 1.};
        double [] TData =new double[]{
                1., 0, 0, dx,
                0, 1., 0, dy,
                0, 0, 1., dz,
                0, 0, 0, 1.};
        double [] A2Data=new double[]{
                f, 0, w / 2., 0,
                0, f, h / 2., 0,
                0, 0, 1., 0};

        Mat A1=setMatValue(3,4,A1Data);
        Mat RX =setMatValue(4,4,RXData);
        Mat RY=setMatValue(4,4,RYData);
        Mat RZ =setMatValue(4,4,RZData);
        Mat A2 =setMatValue(4,3,A2Data);
        Mat T=setMatValue(4,4,TData);

        Mat R=new Mat(new Size (4,4),CV_64F);
        Mat trans=new Mat();
        Core.gemm(RY,RZ,1,new Mat(),0,R,0);
        Core.gemm(RX,R,1,new Mat(),0,R,0);
        Core.gemm(R,A1,1,new Mat(),0,trans,0);
        Core.gemm(T,trans,1,new Mat(),0,trans,0);

        Core.gemm(A2,trans,1,new Mat(),0,trans,0);
        Mat output =new Mat();
        warpPerspective(src, output, trans, src.size(),INTER_LINEAR);
        return output;

    }

    private static Mat setMatValue (int width,int height,double []data )
    {
        Mat out=new Mat(new Size(width,height),CV_64F);
        for (int y=0;y<height;y++)
        {
            for (int x=0;x<width;x++)
            {
                int index =y*width+x;
                out.put(y,x,data[index]);

            }
        }
        return out;
    }
    private static Mat ProcessImage(Mat _src) {
        _src =RotateImage (_src,xAngle,yAngle,zAngle,distance);
        rotatedImage =_src.clone();
        Mat dst = Mat.zeros(_src.size(), _src.type());
        _src = Saturation(_src, SaturationValue);
        _src = sharpImage(_src, sharpValue);
        dst = ContrastAndBrightness(_src, ContrastValue, BrightnessValue);
        return dst;
    }




}
