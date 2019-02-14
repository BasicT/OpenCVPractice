package com.example.opencvpractice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.GuardedBy;
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
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class FourSectionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = FourSectionActivity.class.getName();
    private Uri fileUri;
    private Uri uri;
    Bitmap bitmap;
    Button takePicBtn,blurBtn,gaussianBlurBtn,medianBlurBtn,dilateBtn,erodeBtn
            ,restoreBtn,bilateralFilterBtn,pyrMeanShiftFilteringBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four_section);
        takePicBtn = findViewById(R.id.take_pic_btn);
        takePicBtn.setOnClickListener(this);
        blurBtn = findViewById(R.id.blur_btn);
        blurBtn.setOnClickListener(this);
        gaussianBlurBtn = findViewById(R.id.gaussian_blur_btn);
        gaussianBlurBtn.setOnClickListener(this);
        medianBlurBtn = findViewById(R.id.medianBlur_btn);
        medianBlurBtn.setOnClickListener(this);
        dilateBtn = findViewById(R.id.dilate_btn);
        dilateBtn.setOnClickListener(this);
        erodeBtn = findViewById(R.id.erode_btn);
        erodeBtn.setOnClickListener(this);
        restoreBtn = findViewById(R.id.restore_btn);
        restoreBtn.setOnClickListener(this);
        bilateralFilterBtn = findViewById(R.id.bilateralFilter_btn);
        bilateralFilterBtn.setOnClickListener(this);
        pyrMeanShiftFilteringBtn = findViewById(R.id.pyrMeanShiftFiltering_btn);
        pyrMeanShiftFilteringBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.restore_btn:
                restore();
                break;
            case R.id.take_pic_btn:
                pickUpImage();
                break;
            case R.id.blur_btn:
                meanBlur();
                break;
            case R.id.gaussian_blur_btn:
                gaussianBlur();
                break;
            case R.id.medianBlur_btn:
                medianBlur();
                break;
            case R.id.dilate_btn:
                dilate();
                break;
            case R.id.erode_btn:
                erode();
                break;
            case R.id.bilateralFilter_btn:
                bilateralFilter();
                break;
            case R.id.pyrMeanShiftFiltering_btn:
                pyrMeanShiftFiltering();
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

    private void check(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(FourSectionActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(FourSectionActivity.this,
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
                    Log.d(TAG,"Failed to load...");
                }
                break;
                default:
        }
    }

    private void showIv(){
        try {File file = new File(ImageSelectUtils.getRealFilePath(getApplicationContext(),uri));
            fileUri=Uri.fromFile(file);
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

    //均值模糊
    private void meanBlur(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());

        Mat dst = new Mat();
        Imgproc.blur(mat,dst,new Size(5,5),new Point(-1,-1), Core.BORDER_DEFAULT);

        Bitmap bm = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }

    //高斯模糊
    private void gaussianBlur(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());

        Mat dst = new Mat();
        Imgproc.GaussianBlur(mat,dst,new Size(15,15),0);

        Bitmap bm = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }

    //中值滤波
    private void medianBlur(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());

        Mat dst = new Mat();
        Imgproc.medianBlur(mat,dst,5);

        Bitmap bm = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }

    //膨胀
    private void dilate(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());

        Mat dst = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));
        Imgproc.dilate(mat,dst,kernel);

        Bitmap bm = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }

    //腐蚀
    private void erode(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());

        Mat dst = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));
        Imgproc.erode(mat,dst,kernel);

        Bitmap bm = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }

    //高斯双边滤波
    private void bilateralFilter(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());

        Mat dst = new Mat();
        //d一般取0，意思从sigmaColor参数自动计算。
        // sigmaColor取值范围100-150，sigmaSpace取值范围在10-25
        Imgproc.bilateralFilter(mat,dst,0,150,15);

        Bitmap bm = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }

    //均值迁移滤波
    private void pyrMeanShiftFiltering(){
        Mat mat = Imgcodecs.imread(fileUri.getPath());

        Mat dst = new Mat();
        Imgproc.pyrMeanShiftFiltering(mat,dst,10,50);

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(),Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result,bm);

        ImageView iv = findViewById(R.id.select_image);
        iv.setImageBitmap(bm);

        mat.release();
        dst.release();
        result.release();
    }
}
