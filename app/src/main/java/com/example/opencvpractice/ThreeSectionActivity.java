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

import java.io.File;

public class ThreeSectionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ThreeSectionActivity.class.getName();
    private Uri fileUri;
    private Uri uri;
    Button selectBtn;
    Button processBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_section);
        selectBtn = (Button)findViewById(R.id.select_image_btn);
        selectBtn.setOnClickListener(this);
        processBtn = (Button)findViewById(R.id.process_btn);
        processBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.process_btn:
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
        Bitmap bm = ImageSelectUtils.getSuitableBitmap(fileUri.getPath());
        ImageView iv = (ImageView)findViewById(R.id.select_image);
        iv.setImageBitmap(bm);
    }
}
