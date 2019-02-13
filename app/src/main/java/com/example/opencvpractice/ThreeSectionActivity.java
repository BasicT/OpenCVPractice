package com.example.opencvpractice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ThreeSectionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ThreeSectionActivity.class.getName();
    private Uri fileUri;
    private Uri uri;
    Button selectBtn;
    Button processBtn;
    Button logicBtn;
    Button gaussianBtn;
    Bitmap bm;
    Mat mat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_section);
        mat = new Mat();
        selectBtn = (Button)findViewById(R.id.select_image_btn);
        selectBtn.setOnClickListener(this);
        processBtn = (Button)findViewById(R.id.process_btn);
        processBtn.setOnClickListener(this);
        logicBtn = findViewById(R.id.logic_btn);
        logicBtn.setOnClickListener(this);
        gaussianBtn = findViewById(R.id.gaussian_btn);
        gaussianBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.process_btn:
                negationBitmap(bm);
                break;
            case R.id.logic_btn:
                logicMath();
                break;
            case R.id.gaussian_btn:
                gaussian();
                break;
        }
    }

    private void pickUpImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"选择图像"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                uri = data.getData();
                if (uri == null){
                    return;
                }
                check();
        }
    }

    private void check(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(ThreeSectionActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(ThreeSectionActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }else {
                showIv();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    showIv();
                }else {
                    Log.d(TAG,"failed to load...");
                }
                break;
                default:
        }
    }

    private void showIv(){
        try{File file = new File(ImageSelectUtils.getRealFilePath(getApplicationContext(),uri));
            fileUri = Uri.fromFile(file);
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"错误类型为： "+ e);
        }
        bm = ImageSelectUtils.getSuitableBitmap(fileUri.getPath());
        ImageView iv = (ImageView)findViewById(R.id.select_image);
        iv.setImageBitmap(bm);
    }

    private void negationBitmap(Bitmap bm){
        Utils.bitmapToMat(bm,mat);
        if (mat.empty()){
            return;
        }
        Utils.matToBitmap(superPosition(mat),bm);
    }

    //“逐个”像素点取出并处理后放回
    private Mat everyPoint(Mat mat){
        int channels = mat.channels();
        int width = mat.width();
        int height = mat.height();
        byte[] data = new byte[channels];
        int b = 0, g = 0, r = 0;
        for (int row = 0 ;row < height; row++){
            for (int col = 0;col < width; col++){
                //读取
                mat.get(row,col,data);
                b = data[0]&0xff;
                g = data[1]&0xff;
                r = data[2]&0xff;
                //修改
                b = 255 - b;
                g = 255 - g;
                r = 255 - r;
                //写入
                data[0] = (byte)b;
                data[1] = (byte)g;
                data[2] = (byte)r;
                mat.put(row,col,data);
            }
        }
        return mat;
    }

    //“逐排”像素点取出并处理后放回
    private Mat everyRow(Mat mat){
        int channels = mat.channels();
        int width = mat.width();
        int height = mat.height();
        byte[] data = new byte[channels*width];
        int pv = 0;
        for (int row = 0; row < height; row++){
            mat.get(row,0,data);
            for (int col = 0; col < data.length; col++){
                //读取
                pv = data[col]&0xff;
                //修改
                pv = 254 - pv;
                data[col] = (byte)pv;
            }
            //写入
            mat.put(row,0,data);
        }
        return mat;
    }

    //“整个”像素点取出并处理后放回
    private Mat everyMat(Mat mat){
        int channels = mat.channels();
        int width = mat.width();
        int height = mat.height();
        int pv = 0;
        byte[] data = new byte[channels*width*height];
        mat.get(0,0,data);
        for (int i = 0; i < data.length; i++){
            pv = data[i]&0xff;
            pv = 254-pv;
            data[i] = (byte)pv;
        }
        mat.put(0,0,data);
        return mat;
    }

    //“分图层”取出像素点处理后放回
    private Mat splitAndMerge(Mat mat){
        List<Mat> mv = new ArrayList<>();
        Core.split(mat,mv);
        for (Mat m:mv){
            int pv = 0;
            int channels = m.channels();
            int width = m.width();
            int height = m.height();
            byte[] data = new byte[channels*width*height];
            m.get(0,0,data);
            for (int i = 0; i < data.length; i++){
                pv = data[i]&0xff;
                pv = 254-pv;
                data[i]=(byte)pv;
            }
            m.put(0,0,data);
        }
        Core.merge(mv,mat);
        return mat;
    }

    //转灰度图像后二值转换
    private Mat binaryMat(Mat mat){
        //转为灰度图像
        Mat gray = new Mat();
        Imgproc.cvtColor(mat,gray,Imgproc.COLOR_BGR2GRAY);
        //计算均值和标准方差
        MatOfDouble means = new MatOfDouble();
        MatOfDouble stddevs =  new MatOfDouble();
        Core.meanStdDev(gray,means,stddevs);
        //显示均值与标准方差
        double[] mean = means.toArray();
        double[] stddev = stddevs.toArray();
        Log.i(TAG,"gray image means : " + mean[0]);
        Log.i(TAG,"gray image stddev : " + stddev[0]);
        //读取像素数组
        int width = gray.width();
        int height = gray.height();
        byte[] data = new byte[width*height];
        gray.get(0,0,data);
        int pv = 0;
        //根据均值进行二值分割
        int t = (int)mean[0];
        for(int i = 0; i < data.length; i++){
            pv = data[i]&0xff;
            if (pv>t){
                data[i]= (byte)255;
            }else {
                data[i]=(byte)0;
            }
        }
        gray.put(0,0,data);
        return gray;
    }

    //测试四则运算
    private Mat math(Mat mat){
        Mat moon = Mat.zeros(mat.rows(),mat.cols(),mat.type());
        int cx = mat.cols() - 60;
        int cy = 60;
        Imgproc.circle(moon,new Point(cx,cy),50,new Scalar(95,95,234),-1,8,0);

        Mat dst = new Mat();
        Core.add(mat,moon,dst);
        return dst;
    }

    //测试调整亮度和对比度
    private Mat lighter(Mat mat){
        //加法调整亮度取值范围0-255
        Mat dst1 = new Mat();
        Core.add(mat,new Scalar(0,0,0),dst1);
        //乘法调整对比度取值范围0-3，小于1降低，大于1升高
        Mat dst2 = new Mat();
        Core.multiply(dst1,new Scalar(3,3,3),dst2);

        return dst2;
    }

    //基于权重的图像叠加
    private Mat superPosition(Mat mat){
        Mat black = Mat.zeros(mat.size(),mat.type());
        Mat dst = new Mat();
        double alpha = 1.5;
        int gamma = 30;
        Core.addWeighted(mat,alpha,black,1.0-alpha,gamma,dst);
        return dst;
    }

    //逻辑运算演示
    private void logicMath(){
        Mat src1 = Mat.zeros(400,400,CvType.CV_8UC3);
        Mat src2 = new Mat(400,400,CvType.CV_8UC3);
        src2.setTo(new Scalar(255,255,255));

        Rect rect = new Rect();
        rect.x = 100;
        rect.y = 100;
        rect.width = 200;
        rect.height = 200;

        Imgproc.rectangle(src1,rect.tl(),rect.br(),new Scalar(0,255,0),-1);
        rect.x = 10;
        rect.y = 10;
        Imgproc.rectangle(src2,rect.tl(),rect.br(),new Scalar(255,255,0),-1);

        Mat dst1 = new Mat();
        Mat dst2 = new Mat();
        Mat dst3 = new Mat();
        Core.bitwise_and(src1,src2,dst1);
        Core.bitwise_or(src1,src2,dst2);
        Core.bitwise_xor(src1,src2,dst3);

        Mat dst = Mat.zeros(800,1200,CvType.CV_8UC3);
        rect.x = 0;
        rect.y = 0;
        rect.width = 400;
        rect.height = 400;
        dst1.copyTo(dst.submat(rect));
        rect.x = 400;
        dst2.copyTo(dst.submat(rect));
        rect.x = 800;
        dst3.copyTo(dst.submat(rect));
        rect.x = 0;
        rect.y = 400;
        src2.copyTo(dst.submat(rect));
        rect.x = 400;
        src1.copyTo(dst.submat(rect));


        dst1.release();
        dst2.release();
        dst3.release();

        Bitmap bm1 = Bitmap.createBitmap(dst.cols(),dst.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm1);
        ImageView iv = (ImageView)findViewById(R.id.select_image);
        iv.setImageBitmap(bm1);
    }

    //归一化演示创建高斯噪声图像
    private void gaussian(){
        Mat src = Mat.zeros(400,400,CvType.CV_32FC3);
        float[] data = new float[400*400*3];
        Random random = new Random();
        for (int i = 0; i < data.length; i++){
            data[i] = (float)random.nextGaussian();
        }
        src.put(0,0,data);

        Mat dst = new Mat();
        Core.normalize(src,dst,0,255,Core.NORM_MINMAX,-1,new Mat());

        Mat dst8u = new Mat();
        dst.convertTo(dst8u,CvType.CV_8UC3);

        Bitmap bm1 = Bitmap.createBitmap(dst.cols(),dst.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst8u,bm1);
        ImageView iv = (ImageView)findViewById(R.id.select_image);
        iv.setImageBitmap(bm1);
    }
}
