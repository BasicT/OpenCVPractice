package com.example.opencvpractice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;


public class TwoSectionActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = TwoSectionActivity.class.getName();
    Mat m8;
    Mat comat;
    Bitmap bmp;
    ImageView iv;
    Button saveBtn;
    Button negationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_section);
        saveBtn = (Button)findViewById(R.id.save_image);
        saveBtn.setOnClickListener(this);
        negationBtn = (Button)findViewById(R.id.negation_btn);
        negationBtn.setOnClickListener(this);
        m8 = new Mat(500,500,CvType.CV_8UC3);
        m8.setTo(new Scalar(127,127,127));
        comat = new Mat();
        m8.copyTo(comat);
        bmp = ImageSelectUtils.getSuitableBitmap(this.getResources(),R.drawable.lena);
        iv = (ImageView)findViewById(R.id.image);
        iv.setImageBitmap(bmp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save_image:
                check();
                break;
            case R.id.negation_btn:
                negationBm(bmp);
                break;
        }
    }

    private void check(){
        if (ContextCompat.checkSelfPermission(TwoSectionActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(TwoSectionActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            negationBm(bmp);
        }
    }

    private void negationBm(Bitmap bm){
        bm = bm.copy(Bitmap.Config.ARGB_8888, true);
        int width = bm.getWidth();
        int height = bm.getHeight();
        Bitmap.Config config = bm.getConfig();
        int[] pixels = new int[width*height];
        bm.getPixels(pixels, 0, width, 0, 0, width, height);
        int a=0, r=0, g=0, b=0;
        int index = 0;
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                // 读取像素
                index = width*row + col;
                a=(pixels[index]>>24)&0xff;
                r=(pixels[index]>>16)&0xff;
                g=(pixels[index]>>8)&0xff;
                b=pixels[index]&0xff;
                // 修改像素
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                // 保存到Bitmap中
                pixels[index] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        bm.setPixels(pixels, 0, width, 0, 0, width, height);
        iv.setImageBitmap(bm);
    }

    private void printGragh(){
        Mat src = Mat.ones(500,500,CvType.CV_8UC3);
        //椭圆或者弧长
        Imgproc.ellipse(src,new Point(250,250),new Size(100,50),
                360,0,360,new Scalar(0,0,255),2,8,0);

        //文本
        Imgproc.putText(src,"我是一个图形",new Point(20,20), Core.FONT_HERSHEY_PLAIN,
                1.0,new Scalar(255,0,0),2);
        //设定矩形框
        Rect rect = new Rect();
        rect.x = 50;
        rect.y = 50;
        rect.width = 100;
        rect.height = 100;
        //画矩形
        Imgproc.rectangle(src,rect.tl(),rect.br(),new Scalar(255,0,0),2,8,0);
        //圆形
        Imgproc.circle(src,new Point(400,400),50,new Scalar(0,255,0),2,8,0);
        //线
        Imgproc.line(src,new Point(10,10),new Point(490,490),new Scalar(0,255,0),
                2,8,0);
        Imgproc.line(src,new Point(10,490),new Point(490,10),new Scalar(255,0,0),
                2,8,0);
        //装换为Bitmap对象用于iv显示
        //首先取Mat的高和宽
        int width = src.cols();
        int height = src.rows();
        //根据高和宽来定制一个空的Bitmap对象
        Bitmap bm = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        //cvtColor用于装换Mat对象至RGBA四通道，以防颜色失真
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(dst,bm);
        iv.setImageBitmap(bm);
        dst.release();
        src.release();
    }

    private void matProcess(){
        bmp = bmp.copy(Bitmap.Config.ARGB_8888,true);

        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(Color.GREEN);
        p.setStyle(Paint.Style.STROKE);

        canvas.drawLine(10,10,490,490,p);
        canvas.drawLine(10,490,490,10,p);

        android.graphics.Rect rect = new android.graphics.Rect();
        rect.set(50,50,150,150);
        canvas.drawRect(rect,p);

        p.setColor(Color.BLUE);
        canvas.drawCircle(400,400,50,p);

        p.setColor(Color.RED);
        canvas.drawText("我是一个图形",40,40,p);

        Mat src = new Mat();

        Utils.bitmapToMat(bmp,src);
        saveImage(src);
        src.release();
    }

    private void saveImage(Mat mat){
        try{
            File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "mybook");
            if (!fileDir.exists()){
                boolean i = fileDir.mkdirs();
                Log.d(TAG,"是否创建文件夹 " +  i);
            }
            String name = String.valueOf(System.currentTimeMillis() + "_book.jpg");
            File tempFile = new File(fileDir.getAbsoluteFile()+File.separator,name);
            Imgcodecs.imwrite(tempFile.getAbsolutePath(),mat);
        }catch (Exception e){
            Log.d(TAG,""+e);
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    negationBm(bmp);
                }else {
                    Log.d(TAG,"failed to save...");
                }
                break;
                default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m8.release();
        comat.release();
        bmp.recycle();
    }
}
