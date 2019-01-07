package com.example.opencvpractice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ChapterFirst1Activity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = ChapterFirst1Activity.class.getName();
    private int REQUEST_CAPTURE_IMAGE = 1;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_first1);
        Button processBtn = (Button)findViewById(R.id.process_btn);
        processBtn.setOnClickListener(this);
        Button selectPicBtn =(Button)findViewById(R.id.select_pic_btn);
        selectPicBtn.setOnClickListener(this);
        Button takePicBtn = (Button)findViewById(R.id.take_pic_btn);
        takePicBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.process_btn:
                processImage();
                break;
            case R.id.select_pic_btn:
                selectPic();
                break;
            case R.id.take_pic_btn:

                break;
        }
    }

    private void processImage(){
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.lena);
        Mat src = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(bitmap,src);
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGRA2GRAY);
        Utils.matToBitmap(dst,bitmap);
        ImageView iv = (ImageView)this.findViewById(R.id.sample_img);
        iv.setImageBitmap(bitmap);
        src.release();
        dst.release();
    }

    private void selectPic(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"图像选择..."),REQUEST_CAPTURE_IMAGE);
    }

}
