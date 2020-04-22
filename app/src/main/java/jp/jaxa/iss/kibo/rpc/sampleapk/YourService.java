package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;

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

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
//    private String[] pos_p3;
    @Override
    protected void runPlan1(){

//        pos_p3 = new String[7];

        api.judgeSendStart();

//        moveToWrapper(11.5, -5.7, 4.5, 0, 0, 0, 1);
//        readQRCodeVersion2(0);

//
//        moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
//        readQRCode(1);
//
//        moveToWrapper(11, -5.5, 4.33, 0, 0.7071068, 0, 0.7071068);
//        readQRCode(2);
//
//        moveToWrapper(10.30, -7.5, 4.7, 0, 0, 1, 0);
//        readQRCode(3);
//
//        moveToWrapper(11.5, -8, 5, 0, 0, 0, 1);
//        readQRCode(4);
//
//        moveToWrapper(11, -7.7, 5.55, 0, -0.7071068, 0, 0.7071068);
//        readQRCode(5);
//
//        api.laserControl(true);
//        moveToWrapper(Double.parseDouble(pos_p3[0]),
//                Double.parseDouble(pos_p3[1]),
//                Double.parseDouble(pos_p3[2]),
//                Double.parseDouble(pos_p3[3]),
//                Double.parseDouble(pos_p3[4]),
//                Double.parseDouble(pos_p3[5]),
//                Double.parseDouble(pos_p3[6]));

        moveToWrapper(11.5, -3.75, 4.5, 0, 0, 0.707, -0.707);
//        moveToWrapper(10.6, -4.3, 5, 0, 0, -0.7071068, 0.7071068);
//        moveToWrapper(11, -4.3, 5, 0, 0, -0.7071068, 0.7071068);
//        moveToWrapper(11, -5.7, 5, 0, 0, -0.7071068, 0.7071068);
//        moveToWrapper(11.5, -5.7, 4.5, 0, 0, 0, 1);
        moveToWrapper(11.51, -5.7, 4.5, 0, 0, 0, 1); // adjust pos_x by increase 0.01 for closely with qr code

        testQRCodeReaderFunction(0);
//        moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);

//        api.laserControl(true);
//        moveToWrapper(11.1, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
//        readQRCode(1);

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

        final int LOOP_MAX = 3;
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

//    private void readQRCode(int qrNumber){
//        Bitmap snapshot = api.getBitmapNavCam();
//
////        qrCodeDetector = new QRCodeDetector();
////        pos_p3[qrNumber] = qrCodeDetector.detectAndDecode(mat);
////        api.judgeSendDiscoveredQR(qrNumber,pos_p3[qrNumber]);
//        com.google.zxing.Result result;
//    }

    public String readQRCode(int qrNumber){

//        Bitmap snapshot = api.getBitmapNavCam();
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

//        BinaryBitmap binBit = new BinaryBitmap(new HybridBinarizer(
//                new BufferedImageLuminanceSource(ImageIO.read(new File(file)))));
//        com.google.zxing.Result result = new MultiFormatReader().decode(binBit, map);

//        assert result != null;
//        pos_p3[qrNumber] = result.getText();
//        api.judgeSendDiscoveredQR(qrNumber, result.getText());
        return result.getText();
    }

    private void readQRCodeVersion2(int qrNumber){
        QRCodeDetector qrCodeDetector = new QRCodeDetector();
        Mat input = api.getMatNavCam();
        Mat point = new Mat();
        String result = "";
        if(qrCodeDetector.detect(input, point) == true){
            result = qrCodeDetector.decode(input, point);
            if (result.equals("")){
                // stop when point variable don't have some value from detection
                api.judgeSendDiscoveredQR(3, result);
                moveToWrapper(11.5, -5.7, 5.5, 0, 0, 0, 1); // down on floor

            }else {
                // success to decode qr code
                moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
    //            moveToWrapper(11.1, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
                api.judgeSendDiscoveredQR(qrNumber, result);
            }
        }
        if (qrCodeDetector.detect(input, point) == false){
            // go to start point if can not detect

//            moveToWrapper(10.6, -4.3, 5, 0, 0, -0.7071068, 0.7071068);
//            moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
//            moveToWrapper(11.5, -5.7, 4.5, 0, 0, 0, 1);

            moveToWrapper(11.4, -5.7, 4.5, 0, 0, 0, 1); // adjust pos_x by increase 0.11 for closely with qr code
            input = api.getMatNavCam();
            if(qrCodeDetector.detect(input, point) == true){
                result = qrCodeDetector.decode(input, point);
                if (result.equals("")){
                    // stop when point variable don't have some value from detection
                    api.judgeSendDiscoveredQR(3, result);
                    moveToWrapper(11.5, -5.7, 5.5, 0, 0, 0, 1); // adjust pos_x by increase 0.11 for closely with qr code

                }else {
                    // success to decode qr code
                    moveToWrapper(11, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
                    //            moveToWrapper(11.1, -6, 5.55, 0, -0.7071068, 0, 0.7071068);
                    api.judgeSendDiscoveredQR(qrNumber, result);
                }
            }


            moveToWrapper(11, -5.7, 5, 0, 0, -0.7071068, 0.7071068);
            moveToWrapper(11, -4.3, 5, 0, 0, -0.7071068, 0.7071068);
            moveToWrapper(10.6, -4.3, 5, 0, 0, -0.7071068, 0.7071068);

            api.judgeSendDiscoveredQR(5, result);
        }
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

}

