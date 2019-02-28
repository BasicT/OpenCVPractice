package com.example.opencvpractice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FiveSectionActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = FiveSectionActivity.class.getName();
    private Uri fileUri,uri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_section);
        iniButton();
    }

    private void iniButton(){
        Button takePicBtn,restoreBtn,saveBtn,sobelBtn,scharrBtn,laplacianBtn
                ,cannyWholeBtn,cannyXYBtn,houghLinesBtn,houghLinesPBtn,houghCirclesBtn,
        contourBtn,calchistBtn;
        takePicBtn = findViewById(R.id.take_pic_btn);
        takePicBtn.setOnClickListener(this);
        restoreBtn = findViewById(R.id.restore_btn);
        restoreBtn.setOnClickListener(this);
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
        sobelBtn = findViewById(R.id.sobel_btn);
        sobelBtn.setOnClickListener(this);
        scharrBtn = findViewById(R.id.scharr_btn);
        scharrBtn.setOnClickListener(this);
        laplacianBtn = findViewById(R.id.laplacian_btn);
        laplacianBtn.setOnClickListener(this);
        cannyWholeBtn = findViewById(R.id.canny_whole_btn);
        cannyWholeBtn.setOnClickListener(this);
        cannyXYBtn = findViewById(R.id.canny_xy_btn);
        cannyXYBtn.setOnClickListener(this);
        houghLinesBtn = findViewById(R.id.houghLines_btn);
        houghLinesBtn.setOnClickListener(this);
        houghLinesPBtn = findViewById(R.id.houghLinesP_btn);
        houghLinesPBtn.setOnClickListener(this);
        houghCirclesBtn = findViewById(R.id.houghCircles_btn);
        houghCirclesBtn.setOnClickListener(this);
        contourBtn = findViewById(R.id.contours_btn);
        contourBtn.setOnClickListener(this);
        calchistBtn = findViewById(R.id.calchist_btn);
        calchistBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_pic_btn:
                pickUpImage();
                break;
            case R.id.restore_btn:
                restore();
                break;
            case R.id.save_image:
                break;
            case R.id.sobel_btn:
                sobel();
                break;
            case R.id.scharr_btn:
                scharr();
                break;
            case R.id.laplacian_btn:
                laplacian();
                break;
            case R.id.canny_whole_btn:
                cannyWhole();
                break;
            case R.id.canny_xy_btn:
                cannyXY();
                break;
            case R.id.houghLines_btn:
                houghLines();
                break;
            case R.id.houghLinesP_btn:
                houghLinesP();
                break;
            case R.id.houghCircles_btn:
                houghCircles();
                break;
            case R.id.contours_btn:
                contours();
                break;
            case R.id.calchist_btn:
                calchist();
                break;
        }
    }

    private void pickUpImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"选取图像"),1);
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

    private void check() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(FiveSectionActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(FiveSectionActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
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
                    Log.d(TAG,"Failed to loading...");
                }
                break;
                default:
                    break;
        }
    }

    private void showIv(){
        try{
            File file = new File(ImageSelectUtils.getRealFilePath(getApplicationContext(),uri));
            fileUri = Uri.fromFile(file);
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"错误类型为： " + e);
        }
        bitmap = ImageSelectUtils.getSuitableBitmap(fileUri.getPath());

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bitmap);
    }

    private void restore(){
        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bitmap);
    }

    private void save(Mat dst){
        try{
            File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "mybook");
            if (fileDir.exists()){
                boolean i = fileDir.mkdirs();
                Log.d(TAG,"是否成功创建文件夹" + i);
            }
            String name = String.valueOf(System.currentTimeMillis() + "_book.jpg");
            File tempFile = new File(fileDir.getAbsoluteFile()+File.separator,name);
            Imgcodecs.imwrite(tempFile.getAbsolutePath(),dst);

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"错误类型为：" + e);
        }
    }

    private void sobel(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Mat gradx = new Mat();
        Imgproc.Sobel(mat,gradx, CvType.CV_32F,1,0);
        Core.convertScaleAbs(gradx,gradx);
        Log.i("OpenCV","XGradient...");

        Mat grady = new Mat();
        Imgproc.Sobel(mat,grady,CvType.CV_32F,0,1);
        Core.convertScaleAbs(grady,grady);
        Log.i("OpenCV","YGradient...");

        Core.addWeighted(gradx,0.5,grady,0.5,0,dst);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
        gradx.release();
        grady.release();
        Log.i("OpenCV","Gradient...");
    }

    private void scharr(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Mat gradx = new Mat();
        Imgproc.Scharr(mat,gradx,CvType.CV_32F,1,0);
        Core.convertScaleAbs(gradx,gradx);
        Log.i("OpenCV","XGradient...");

        Mat grady = new Mat();
        Imgproc.Scharr(mat,grady,CvType.CV_32F,0,1);
        Core.convertScaleAbs(grady,grady);
        Log.i("OpenCV","YGradient");

        Core.addWeighted(gradx,0.5,grady,0.5,0,dst);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        gradx.release();
        grady.release();
        result.release();
        Log.i("OpenCV","Gradient");
    }

    private void laplacian(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Imgproc.Laplacian(mat,dst,CvType.CV_32F,3,1.0,0);
        Core.convertScaleAbs(dst,dst);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }

    private void cannyWhole(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Mat edges = new Mat();

        Imgproc.GaussianBlur(mat,mat,new Size(3,3),0);
        //求灰度图像，意义不明
        Mat gray = new Mat();
        Imgproc.cvtColor(mat,gray,Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(mat,edges,50,150,3,true);
        Core.bitwise_and(mat,mat,dst,edges);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        edges.release();
        gray.release();
        result.release();
    }

    private void cannyXY(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Mat gradx = new Mat();
        Imgproc.Sobel(mat,gradx,CvType.CV_16S,1,0);

        Mat grady = new Mat();
        Imgproc.Sobel(mat,grady,CvType.CV_16S,0,1);

        Mat edges = new Mat();
        Imgproc.Canny(gradx,grady,edges,50,150);
        Core.bitwise_and(mat,mat,dst,edges);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        gradx.release();
        grady.release();
        edges.release();
        result.release();
    }

    private void houghLines(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();

        Mat edges = new Mat();
        Imgproc.Canny(mat,edges,50,150,3,true);

        Mat lines = new Mat();
        Imgproc.HoughLines(edges,lines,1,Math.PI/180.0,200);

        Mat out = Mat.zeros(mat.size(),mat.type());

        float[] data = new float[2];
        for (int i = 0; i<lines.rows(); i++){
            lines.get(i,0,data);
            float rho = data[0],theta = data[1];
            double a = Math.cos(theta),b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            Point pt1 = new Point();
            Point pt2 = new Point();
            pt1.x = Math.round(x0 + 1000*(-b));
            pt1.y = Math.round(y0 + 1000*(a));
            pt2.x = Math.round(x0 - 1000*(-b));
            pt2.y = Math.round(y0 - 1000*(a));
            Imgproc.line(out,pt1,pt2,new Scalar(0,0,255),3,Imgproc.LINE_AA,0);
        }
        out.copyTo(dst);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        edges.release();
        lines.release();
        result.release();
        out.release();
    }

    private void houghLinesP(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Mat edges = new Mat();

        Imgproc.Canny(mat,edges,50,150,3,true);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges,lines,1,Math.PI/180.0,100,50,10);
        Mat out = Mat.zeros(mat.size(),mat.type());
        for (int i = 0; i < lines.rows(); i++){
            int[] oneline = new int[4];
            lines.get(i,0,oneline);
            Imgproc.line(out,new Point(oneline[0],oneline[1]),new Point(oneline[2],oneline[3]),
                    new Scalar(0,0,255),2,8,0);
        }
        out.copyTo(dst);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        edges.release();
        lines.release();
        result.release();
        out.release();
    }

    private void houghCircles() {
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();

        Mat gray = new Mat();
        Imgproc.pyrMeanShiftFiltering(mat, gray, 15, 80);
        Imgproc.cvtColor(gray, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(gray,gray,new Size(3,3),0);

        Mat circles = new Mat();
        dst.create(mat.size(), mat.type());
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, 20, 100, 30,
                10, 200);
        for (int i = 0; i < circles.cols(); i++) {
            float[] info = new float[3];
            circles.get(0,i,info);
            Imgproc.circle(dst,new Point((int)info[0],(int)info[1]),(int)info[2],
                    new Scalar(0,255,0),2,8,0);
        }

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        gray.release();
        circles.release();
        result.release();
    }

    private void contours(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Mat gray = new Mat();
        Mat binary = new Mat();
        Imgproc.cvtColor(mat,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray,binary,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE,new Point(0,0));

        dst.create(mat.size(),mat.type());
        for (int i = 0; i < contours.size(); i++){
            Rect rect = Imgproc.boundingRect(contours.get(i));
            double w = rect.width;
            double h = rect.height;
            double rate = Math.min(w,h)/Math.max(w,h);
            Log.i("Bound Rect","rate: "+ rate);

            RotatedRect minRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            w = minRect.size.width;
            h = minRect.size.height;
            rate = Math.min(w,h)/Math.max(w,h);
            Log.i("Min Bound Rect","rate: " + rate);

            double area = Imgproc.contourArea(contours.get(i));
            double arclen = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()),true);
            Log.i("ContourArea","area: "+ area);
            Log.i("arcLength","arclength: "+ arclen);

            Imgproc.drawContours(dst,contours,i,new Scalar(0,0,255),2);
        }

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        gray.release();
        binary.release();
        result.release();
        hierarchy.release();
    }

    private void calchist(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());
        Mat dst = new Mat();
        Mat gray = new Mat();
        Imgproc.cvtColor(mat,gray,Imgproc.COLOR_BGR2GRAY);

        List<Mat> images = new ArrayList<Mat>();
        images.add(gray);
        Mat mask = Mat.ones(dst.size(),CvType.CV_8UC1);
        Mat hist = new Mat();
        Imgproc.calcHist(images,new MatOfInt(0),mask,hist,new MatOfInt(256),new MatOfFloat(0,255));
        Core.normalize(hist,hist,0,255,Core.NORM_MINMAX);
        int height = hist.rows();

        dst.create(400,400,mat.type());
        dst.setTo(new Scalar(200,200,200));
        int offsetx = 50;
        int offsety = 350;
        float[] histdata = new float[256];
        hist.get(0,0,histdata);
        Imgproc.line(dst,new Point(offsetx,0),new Point(offsetx,offsety),new Scalar(0,0,0));
        Imgproc.line(dst,new Point(offsetx,offsety),new Point(400,offsety),new Scalar(0,0,0));

        for (int i = 0; i < height-1;i++){
            int y1 = (int)histdata[i];
            int y2 = (int)histdata[i+1];
            Rect rect = new Rect();
            rect.x = offsetx + i;
            rect.y = offsety - y1;
            rect.width = 1;
            rect.height = y1;
            Imgproc.rectangle(dst,rect.tl(),rect.br(),new Scalar(15,15,15));
        }

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        gray.release();
        mask.release();
        hist.release();
        result.release();
    }
}
