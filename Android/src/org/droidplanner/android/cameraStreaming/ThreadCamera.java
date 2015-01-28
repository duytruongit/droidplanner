package org.droidplanner.android.cameraStreaming;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.Calendar;

/**
 * Created by ahcorde on 26/1/15.
 */
public class ThreadCamera extends Thread
{
    enum DeliveryMode
    {
        TWOWAY,
        TWOWAY_SECURE,
        ONEWAY,
        ONEWAY_BATCH,
        ONEWAY_SECURE,
        ONEWAY_SECURE_BATCH,
        DATAGRAM,
        DATAGRAM_BATCH;

        Ice.ObjectPrx apply(Ice.ObjectPrx prx)
        {
            switch (this)
            {
                case TWOWAY:
                    prx = prx.ice_twoway();
                    break;
                case TWOWAY_SECURE:
                    prx = prx.ice_twoway().ice_secure(true);
                    break;
                case ONEWAY:
                    prx = prx.ice_oneway();
                    break;
                case ONEWAY_BATCH:
                    prx = prx.ice_batchOneway();
                    break;
                case ONEWAY_SECURE:
                    prx = prx.ice_oneway().ice_secure(true);
                    break;
                case ONEWAY_SECURE_BATCH:
                    prx = prx.ice_batchOneway().ice_secure(true);
                    break;
                case DATAGRAM:
                    prx = prx.ice_datagram();
                    break;
                case DATAGRAM_BATCH:
                    prx = prx.ice_batchDatagram();
                    break;
            }
            return prx;
        }

        public boolean isBatch()
        {
            return this == ONEWAY_BATCH || this == DATAGRAM_BATCH || this == ONEWAY_SECURE_BATCH;
        }
    }

    private Image.ImageProviderPrx _imagePrx = null;
    private Object o;
    Bitmap bitmap_ice;
    int width;
    int height;


    public ThreadCamera(){

        Ice.Communicator _communicator = null;
        DeliveryMode _deliveryMode;
        _deliveryMode = DeliveryMode.TWOWAY;

        Ice.InitializationData initData = new Ice.InitializationData();
        initData.properties = Ice.Util.createProperties();
        //initData.properties.setProperty("Ice.InitPlugins", "0");
        //initData.properties.setProperty("Ice.Trace.Network", "3");

        _communicator = Ice.Util.initialize(initData);

        String host = new String("192.168.1.36");
        //String host = new String("10.211.55.5");
        String s = "ImageServer:default -h " + host + " -p 10000";
        Ice.ObjectPrx prx = _communicator.stringToProxy(s);
        prx = _deliveryMode.apply(prx);

        prx = prx.ice_timeout(1000);

        _imagePrx = Image.ImageProviderPrxHelper.checkedCast(prx);
        o = new Object();
        Image.ImageDescription imageData = _imagePrx.getImageData();

        width = imageData.height;
        height = imageData.width;
        bitmap_ice = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    }

    long diff_1;

    public void run(){
        try{
            Calendar calendar = Calendar.getInstance();

            int cycle = 25;

            long totala, totalb;
            long diff;

            while(true){
                try
                {
                    totala = Calendar.getInstance().getTimeInMillis() / 1000L;

                    Image.ImageDescription imageData = _imagePrx.getImageData();

                    int size_compress = imageData.sizeCompress;
                    Mat imagen_compress = new Mat(size_compress, 1, CvType.CV_8U);

                    byte buff[] = new byte[size_compress];
                    System.arraycopy(imageData.imageData, 0, buff, 0,size_compress);
                    /*for(int x = 0; x < size_compress; x++){
                        buff[x] = (byte) imageData.imageData[x];
                    }*/
                    imagen_compress.put(0, 0, buff);

                    Mat imagen_descompress = Highgui.imdecode(imagen_compress, Highgui.IMWRITE_JPEG_QUALITY);

                    Imgproc.cvtColor(imagen_descompress, imagen_descompress, Imgproc.COLOR_BGR2RGBA, 4);
                    synchronized (o) {
                        Utils.matToBitmap(imagen_descompress, bitmap_ice);
                    }
                    totalb = Calendar.getInstance().getTimeInMillis() / 1000L;
                    diff = (totalb - totala) / 1000;

                    diff_1 = diff;

                }catch(Ice.ConnectTimeoutException e){
//                    e.printStackTrace();
                }
                /*catch(InterruptedException e){
                    e.printStackTrace();
                }*/
            }
        }
        catch(Ice.SocketException e) {
            e.printStackTrace();
        }
    }
    public long getDiff()
    {
        return diff_1;
    }
    public Bitmap getImage(){
        Bitmap bitmap_result=null;
        synchronized(o){
            bitmap_result = Bitmap.createBitmap(bitmap_ice);
        }
        return bitmap_result;
    }

}
