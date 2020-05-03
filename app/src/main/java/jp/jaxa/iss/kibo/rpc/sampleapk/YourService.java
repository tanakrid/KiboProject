package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;

import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_imgproc.CvMoments;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

import gov.nasa.arc.astrobee.android.gs.MessageType;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.Dimension;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_core.cvCreateImage;
import static org.bytedeco.opencv.global.opencv_core.cvGetSize;
import static org.bytedeco.opencv.global.opencv_core.cvInRangeS;
import static org.bytedeco.opencv.global.opencv_core.cvReleaseImage;
import static org.bytedeco.opencv.global.opencv_core.cvScalar;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_MEDIAN;
import static org.bytedeco.opencv.global.opencv_imgproc.cvCvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.cvGetCentralMoment;
import static org.bytedeco.opencv.global.opencv_imgproc.cvGetSpatialMoment;
import static org.bytedeco.opencv.global.opencv_imgproc.cvMoments;
import static org.bytedeco.opencv.global.opencv_imgproc.cvSmooth;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    @Override
    protected void runPlan1(){

        api.judgeSendStart();

        String pos_x = "";
        String pos_y = "";
        String pos_z = "";
        String qua_x = "";
        String qua_y = "";
        String qua_z = "";
        String qua_w = "";

        moveToWrapper(11, -5.5, 4.33, 0, 0.7071068, 0, 0.7071068);
        pos_z = saveToReadQRCode(2);

        if (!pos_z.equals("")) {
            moveToWrapper(11.5, -5.7, 4.5, 0, 0, 0, 1);
            pos_x = saveToReadQRCode(0);
        }

        if (!pos_x.equals("")) {
            moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
            pos_y = saveToReadQRCode(1);
        }
//        moveToWrapper(11, -5.5, 4.33, 0, 0.7071068, 0, 0.7071068);
//        moveToWrapper(11.5, -5.7, 4.5, 0, 0, 0, 1);
//        moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
        findTarget(api.getBitmapNavCam());
        api.judgeSendFinishSimulation();
    }

    @Override
    protected void runPlan2(){
        // write here your plan 2
    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    // You can add your method
    private void moveToWrapper(double pos_x, double pos_y, double pos_z,
                               double qua_x, double qua_y, double qua_z,
                               double qua_w){

//        final int LOOP_MAX = 3;
        final int LOOP_MAX = 20;

        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float)qua_x, (float)qua_y,
                                                     (float)qua_z, (float)qua_w);

        Result result = api.moveTo(point, quaternion, true);

        int loopCounter = 0;
        while(!result.hasSucceeded() || loopCounter < LOOP_MAX){
            result = api.moveTo(point, quaternion, true);
            ++loopCounter;
        }
    }

    private String saveToReadQRCode(int qrNumber){

        String result = "";
        final int MAX_LOOP = 30;
        int count = 0;
        result = readQRCode(qrNumber);
        while( (result.equals("")) && (count < MAX_LOOP)){
            result = readQRCode(qrNumber);
            count++;
        }

        return result;
    }

    private void relativeMoveToWrapper(double pos_x, double pos_y, double pos_z,
                               double qua_x, double qua_y, double qua_z,
                               double qua_w){

        final int LOOP_MAX = 3;
        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float)qua_x, (float)qua_y,
                (float)qua_z, (float)qua_w);

        Result result = api.relativeMoveTo(point, quaternion, true);

        int loopCounter = 0;
        while(!result.hasSucceeded() || loopCounter < LOOP_MAX){
            result = api.relativeMoveTo(point, quaternion, true);
            ++loopCounter;
        }
    }

    public String readQRCode(int qrNumber){

        Mat snapshot = api.getMatNavCam();

        Bitmap bMap = Bitmap.createBitmap(snapshot.width(), snapshot.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(snapshot, bMap);

        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        com.google.zxing.Result result = null;
        Reader reader = new QRCodeReader();
        try {
            result = reader.decode(bitmap);
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        api.judgeSendDiscoveredQR(qrNumber, result.getText());
        return result.getText();
    }


    private void testQRCodeReaderFunction(int qrNumber){
        String result = "";
        result = readQRCode(qrNumber);
        if (result.equals("")){
            moveToWrapper(11, -5.7, 5, 0, 0, -0.7071068, 0.7071068);
            moveToWrapper(11, -4.3, 5, 0, 0, -0.7071068, 0.7071068);
            moveToWrapper(10.6, -4.3, 5, 0, 0, -0.7071068, 0.7071068);

            api.judgeSendDiscoveredQR(5, result);
        }else{
            // success to decode qr code
            moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
            api.judgeSendDiscoveredQR(qrNumber, result);
        }
    }
    static Dimension getCoordinates(IplImage thresholdImage) {
        int posX = 0;
        int posY = 0;
        CvMoments moments = new CvMoments();
        cvMoments(thresholdImage, moments, 1);
        // cv Spatial moment : Mji=sumx,y(I(x,y)•xj•yi)
        // where I(x,y) is the intensity of the pixel (x, y).
        double momX10 = cvGetSpatialMoment(moments, 1, 0); // (x,y)
        double momY01 = cvGetSpatialMoment(moments, 0, 1);// (x,y)
        double area = cvGetCentralMoment(moments, 0, 0);
        posX = (int) (momX10 / area);
        posY = (int) (momY01 / area);
        return new Dimension(posX, posY);
    }
    //
//    static IplImage hsvThreshold(IplImage orgImg) {
//        int hueLowerR = 160;
//        int hueUpperR = 180;
//        // 8-bit, 3- color =(RGB)
//        IplImage imgHSV = cvCreateImage(cvGetSize(orgImg), 8, 3);
//        System.out.println(cvGetSize(orgImg));
//        cvCvtColor(orgImg, imgHSV, CV_BGR2HSV);
//        // 8-bit 1- color = monochrome
//        IplImage imgThreshold = cvCreateImage(cvGetSize(orgImg), 8, 1);
//        // cvScalar : ( H , S , V, A)
//        cvInRangeS(imgHSV, cvScalar(hueLowerR, 100, 100, 0), cvScalar(hueUpperR, 255, 255, 0), imgThreshold);
//        cvReleaseImage(imgHSV);
////        cvSmooth();
//        cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);
//        // save
//        return imgThreshold;
//    }
    static void findTarget(Bitmap img){
        IplImage img1,imghsv,imgbin;
        int w = img.getHeight();
        int h = img.getWidth();
        IplImage iplImage = IplImage.create(w,h,8,4);
        img.copyPixelsToBuffer(iplImage.getByteBuffer());
        IplImage iplImageDest = IplImage.create(w, h, 8, 1);

        imghsv  = cvCreateImage(cvGetSize( iplImageDest), 8, 3);
        imgbin = cvCreateImage(cvGetSize( iplImageDest), 8, 1);
        cvCvtColor( iplImageDest, imghsv, CV_BGR2HSV);

        cvInRangeS(imghsv, cvScalar( 40,150, 750, 0), cvScalar(80, 255, 255, 0), imgbin);
        cvReleaseImage(imghsv);
        cvReleaseImage(imgbin);

        getCoordinates(imgbin);

    }

}

