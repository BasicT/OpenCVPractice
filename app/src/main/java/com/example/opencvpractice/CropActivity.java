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

import com.example.opencvpractice.extra.MyCropView;

import java.io.File;

public class CropActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = CropActivity.class.getName();
    private Uri fileUri,uri;
    private Bitmap bitmap;
    MyCropView cropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        cropView = findViewById(R.id.myCropView);
        iniButton();
    }

    private void iniButton(){
        Button takePicBtn,cropPicBtn;
        takePicBtn = findViewById(R.id.take_pic_btn);
        takePicBtn.setOnClickListener(this);
        cropPicBtn = findViewById(R.id.crop_pic_btn);
        cropPicBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_pic_btn:
                takePic();
                break;
            case R.id.crop_pic_btn:

                break;
                default:
                    break;
        }
    }

    private void takePic(){
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
                break;
                default:
                    break;
        }
    }

    private void check(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(CropActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(CropActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    showIv();
                }else {
                    Log.d(TAG,"failed to loading...");
                }
                break;
                default:
                    break;
        }
    }

    private void showIv(){
        try {
            File file = new File(ImageSelectUtils.getRealFilePath(getApplicationContext(),uri));
            fileUri = Uri.fromFile(file);
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"错误类型为： " + e);
        }

        Bitmap bmp = ImageSelectUtils.getSuitableBitmap(fileUri.getPath());
        cropView.setBmpPath(fileUri.getPath());
        //ImageView iv = findViewById(R.id.select_image);
        //iv.setImageBitmap(bmp);
    }

    private void cropPic(){
        cropView.setBmpPath(fileUri.getPath());
    }
}
