package com.example.opencvpractice;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;;
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
import android.widget.Toast;

import com.example.opencvpractice.extra.MyCropView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = CropActivity.class.getName();
    private Uri fileUri,uri;
    private Bitmap bitmap;
    MyCropView cropView;

    String mCurrentPhotoPath;
    boolean targetChose = false;
    ProgressDialog dlg;
    private Bitmap originalBitmap;
    private ImageView iv;
    private boolean hasCut =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        iv = findViewById(R.id.select_image);
        cropView = findViewById(R.id.myCropView);
        dlg = new ProgressDialog(this);
        iniButton();
    }

    private void iniButton(){
        Button takePicBtn,cutPicBtn,modifyBtn,saveImageBtn;
        takePicBtn = findViewById(R.id.take_pic_btn);
        takePicBtn.setOnClickListener(this);
        modifyBtn = findViewById(R.id.modify_btn);
        modifyBtn.setOnClickListener(this);
        cutPicBtn = findViewById(R.id.cut_pic_btn);
        cutPicBtn.setOnClickListener(this);
        saveImageBtn = findViewById(R.id.save_image_btn);
        saveImageBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_pic_btn:
                takePic();
                break;
            case R.id.modify_btn:
                selectImageCut();//剪切图像
                break;
            case R.id.cut_pic_btn:
                if (targetChose){
                    dlg.show();
                    dlg.setMessage("正在抠图...");
                    final RectF croppedBitmapData = cropView.getCroppedBitmapData();
                    final int croppedBitmapWidth = cropView.getCroppedBitmapWidth();
                    final int croppedBitmapHeight = cropView.getCroppedBitmapHeight();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap bitmap = cupBitmap(originalBitmap, (int) croppedBitmapData.left, (int) croppedBitmapData.top, croppedBitmapWidth, croppedBitmapHeight);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dlg.dismiss();
                                    hasCut = true;
                                    iv.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }).start();

                }
                break;
            case R.id.save_image_btn:
                if (hasCut){
                    String s = saveImageToGalleryString(this, ((BitmapDrawable) (iv).getDrawable()).getBitmap());
                    Toast.makeText(this, "保存在"+s, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "请先扣图", Toast.LENGTH_SHORT).show();
                }
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

        mCurrentPhotoPath = fileUri.getPath();
        Bitmap bmp = ImageSelectUtils.getSuitableBitmap(fileUri.getPath());
        originalBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        cropView.setBmpPath(fileUri.getPath());

        //iv.setImageBitmap(bmp);
    }

    private void cropPic(){
        cropView.setBmpPath(fileUri.getPath());
    }

    private Bitmap cupBitmap(Bitmap bitmap,int x,int y,int width,int height){
        Mat img = new Mat();
//缩小图片尺寸
        //Bitmap bm = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
//bitmap->mat
        Utils.bitmapToMat(bitmap, img);
//转成CV_8UC3格式
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);
//设置抠图范围的左上角和右下角
        Rect rect = new Rect(x,y,width,height);
//生成遮板
        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel,5, Imgproc.GC_INIT_WITH_RECT);
        Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

//抠图
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        img.copyTo(foreground, firstMask);

//mat->bitmap
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(foreground,bitmap1);
        return bitmap1;
    }

    //选择剪切区域
    private void selectImageCut(){
        targetChose = true;
        try{
            Bitmap cropBitmap = cropView.getCroppedImage();
            iv.setImageBitmap(cropBitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //保存在系统图库
    public static String saveImageToGalleryString(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dlg != null) {
            dlg.dismiss();
        }
    }
}
