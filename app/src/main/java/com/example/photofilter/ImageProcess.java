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

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC3;

public class ImageProcess {
    private static final String TAG = "MarkImageProcess";

    public static Mat HSV(Mat _src, int _value) {

        float value = _value;
        Mat hsv = Mat.zeros(_src.size(), _src.type());
        Imgproc.cvtColor(_src, hsv, Imgproc.COLOR_RGB2HSV);
        Mat index = hsv.clone();
        index.setTo(new Scalar(0, Math.abs(value), 0));
        Mat output = new Mat();
        if(_value>=0)
            Core.add(hsv,index,output);
        else
            Core.subtract(hsv, index, output);
        Mat rgb = Mat.zeros(_src.size(), _src.type());
        Imgproc.cvtColor(output, rgb, Imgproc.COLOR_HSV2RGB);
        return rgb;
    }

    public static Mat ImageContrast(Mat src, float _value) {
        _value/=2.0f;
        float value = 1.0f * (1.8f * _value + 180) / 200 + 0.2f;

        Log.e(TAG, "ImageContrast = " + value);
        //Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
        Mat mat1 = Mat.zeros(src.size(), src.type());
        src.convertTo(mat1, -1, value, 1);
        return mat1;
    }

    public static Mat ImageBrightness(Mat src,int _value) {
        float value = _value * 2.5f/2.0f;
        Log.e(TAG, "ImageBrightness = "+value );
        //Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
        Mat mat1 = Mat.zeros(src.size(), src.type());
        src.convertTo(mat1, -1, 1, value);
        return mat1;
    }

    public static Mat Rotate_X(Mat src,int _value) {
        float value = 1.0f * _value;
        Mat dst = new Mat();
        Mat perspectiveMmat;
        float xMargin, yMargin;
        int x0 = 0;
        int x1 = src.cols();
        int y0 = 0;
        int y1 = src.rows();
        List<Point> listSrcs = java.util.Arrays.asList(new Point(x0, y0), new Point(x0, y1), new Point(x1, y1), new Point(x1, y0));
        Mat srcPoints = Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);

        float parameter_x = (Math.abs(value) - 1) / 24 * (20 - (src.cols() / 6)) + (src.cols() / 6);
        float parameter_y = (Math.abs(value) - 1) / 24 * (20 - (src.rows() / 6)) + (src.rows() / 6);
        xMargin = src.cols() / parameter_x;
        yMargin = src.rows() / parameter_y;

        if (value >= 1) {
            List<Point> listDsts = java.util.Arrays.asList(new Point(x0 + xMargin, y0 + yMargin),
                    listSrcs.get(1), listSrcs.get(2), new Point(x1 - xMargin, y0 + yMargin));
            Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
            perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        } else if (value <= -1) {
            List<Point> listDsts = java.util.Arrays.asList(listSrcs.get(0),
                    new Point(x0 + xMargin, y1 - yMargin),
                    new Point(x1 - xMargin, y1 - yMargin),
                    listSrcs.get(3));
            Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
            perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        } else {
            List<Point> listDsts = java.util.Arrays.asList(listSrcs.get(0), listSrcs.get(1), listSrcs.get(2), listSrcs.get(3));

            Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);

            perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        }
        Imgproc.warpPerspective(src, dst, perspectiveMmat, src.size(), Imgproc.INTER_LINEAR);

        return dst;
    }

    public static Mat Rotate_Y(Mat src, int _value) {
        float value = 1.0f * _value;
        Mat dst = new Mat();
        Mat perspectiveMmat;
        float xMargin, yMargin;
        int x0 = 0;
        int x1 = src.cols();
        int y0 = 0;
        int y1 = src.rows();
        List<Point> listSrcs = java.util.Arrays.asList(new Point(x0, y0), new Point(x0, y1), new Point(x1, y1), new Point(x1, y0));
        Mat srcPoints = Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);

        float parameter_x = (Math.abs(value) - 1) / 24 * (20 - (src.cols() / 6)) + (src.cols() / 6);
        float parameter_y = (Math.abs(value) - 1) / 24 * (20 - (src.rows() / 6)) + (src.rows() / 6);
        xMargin = src.cols() / parameter_x;
        yMargin = src.rows() / parameter_y;
        if (value >= 1) {
            List<Point> listDsts = java.util.Arrays.asList(listSrcs.get(0), listSrcs.get(1),
                    new Point(x1 - xMargin, y1 - yMargin),
                    new Point(x1 - xMargin, y0 + yMargin));

            Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
            perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        } else if (value <= -1) {

            List<Point> listDsts = java.util.Arrays.asList(new Point(x0 + xMargin, y0 + yMargin),
                    new Point(x0 + xMargin, y1 - yMargin), listSrcs.get(2), listSrcs.get(3));

            Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
            perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        } else {
            List<Point> listDsts = java.util.Arrays.asList(listSrcs.get(0), listSrcs.get(1),
                    listSrcs.get(2), listSrcs.get(3));

            Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
            perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        }
        Imgproc.warpPerspective(src, dst, perspectiveMmat, src.size(), Imgproc.INTER_LINEAR);
        return dst;
    }

    public static Mat Rotate_XY(Mat src,int _value) {
        float value = 1.0f * _value;
        Mat dst = new Mat();
        Point center = new Point(src.width() / 2.0, src.height() / 2.0);
        float scale = value != 0 ? 1.5f : 1;
        Mat affineTrans = Imgproc.getRotationMatrix2D(center, value, 1);
        Imgproc.warpAffine(src, dst, affineTrans, src.size(), Imgproc.INTER_NEAREST);
        return dst;
    }

    public static Mat CartoonFilter (Mat src)
    {

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
    public static Mat sharpImage (Mat src,int value)
    {
        double sigma = 3;
        int threshold = 1;
        float amount = value / 100.0f;

        Mat imgBlurred=new Mat();
        Imgproc.GaussianBlur(src, imgBlurred, new Size(), sigma, sigma);
        //Mat lowContrastMask  =Core.ab src - imgBlurred;
//        Mat lowContrastMask = Core.abs(inputImage - imgBlurred) < threshold;
//        Mat dst = inputImage * (1 + amount) + imgBlurred * (-amount);
//        inputImage.copyTo(dst, lowContrastMask);
//        imshow("sharpImage", dst);

        return new Mat();
    }
    public static Mat WarmFilter (Mat inputMat)
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
        return result;
        //merge(channels, result);



    }


}
