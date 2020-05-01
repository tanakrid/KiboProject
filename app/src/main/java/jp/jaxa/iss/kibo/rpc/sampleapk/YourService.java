package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

import gov.nasa.arc.astrobee.Kinematics;
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
    @Override
    protected void runPlan1(){

        api.judgeSendStart();

        moveToWrapper(11, -5.5, 4.4, 0, 0.7071068, 0, 0.7071068, 1);

        moveToWrapper(11.5, -5.65, 4.55, 0, 0, 0, 1, 2);

        moveToWrapper(11, -6, 5.45, 0, -0.7071068, 0, 0.7071068, 3);


        moveToWrapper(10.5, -6.2, 5.45, 0, 0, 0.7071068, -0.7071068, 4);
        moveToWrapper(10.5, -6.8, 5.45, 0, 0, 0.7071068, -0.7071068, 5);
        moveToWrapper(11, -6.8, 5.45, 0, 0, 0.7071068, -0.7071068, 6);


        moveToWrapper(11, -7.7, 5.4, 0, -0.7071068, 0, 0.7071068, 7);

        moveToWrapper(10.5, -7.5, 4.7, 0, 0, 1, 0, 8);

        moveToWrapper(11.5, -8, 5, 0, 0, 0, 1, 9);


        moveToWrapper(10.95, -9.3, 5.25, 0,0,0.707, -0.707, 10);

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
                               double qua_w, int number){
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

        Kinematics kinematics = api.getTrustedRobotKinematics();
        Point pos = kinematics.getPosition();

        Log.i("dolphin", number + ". pos_x : " + pos.getX() + " pos_y : " + pos.getY() + " pos_z : " + pos.getZ());
    }

    private String saveToReadQRCode(int qrNumber){

        String result = "";
        final int MAX_LOOP = 3;
        int count = 0;
        result = readQRCode(qrNumber);
        while( (result.equals("")) && (count < MAX_LOOP)){
            result = readQRCode(qrNumber);
            count++;
        }

        return result;
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

}

