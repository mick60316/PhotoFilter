package com.example.photofilter;


import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.List;

public class ImageProcess {
    private static final String TAG = "MarkImageProcess";

    public static Mat HSV(Mat _src, int _value) {
        float value = 2*_value;
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
        float value = 1.0f * (1.8f * _value + 180) / 200 + 0.2f;
        Log.e(TAG, "ImageContrast = " + value);
        //Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
        Mat mat1 = Mat.zeros(src.size(), src.type());
        src.convertTo(mat1, -1, value, 1);
        return mat1;
    }

    public static Mat ImageBrightness(Mat src,int _value) {
        float value = _value * 2.5f;
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
        Mat affineTrans = Imgproc.getRotationMatrix2D(center, value, scale);
        Imgproc.warpAffine(src, dst, affineTrans, src.size(), Imgproc.INTER_NEAREST);
        return dst;
    }
}
