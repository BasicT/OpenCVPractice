package com.example.opencvpractice;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageSelectUtils{

    private static String TAG = "ImageSelectUtils";
    public static File getSaveFilePath() {
        String status = Environment.getExternalStorageState();
        if(!status.equals(Environment.MEDIA_MOUNTED)) {
            Log.i(TAG, "SD Card is not suitable...");
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String name = df.format(new Date(System.currentTimeMillis()))+ ".jpg";
        File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "myOcrImages");
        filedir.mkdirs();
        String fileName = filedir.getAbsolutePath() + File.separator + name;
        File imageFile = new File(fileName);
        return imageFile;
    }

    public static String getRealPath(Uri uri, Context appContext) {
        String filePath = null;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){//4.4及以上
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String[] column = { MediaStore.Images.Media.DATA };
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = appContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
                    sel, new String[] { id }, null);
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }else{//4.4以下，即4.4以上获取路径的方法
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = appContext.getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(column_index);
        }
        Log.i(TAG, "selected image path : " + filePath);
        return filePath;
    }

    public static void saveImage(Mat image) {
        File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mybook");
        if(!fileDir.exists()) {
            fileDir.mkdirs();
        }
        String name = String.valueOf(System.currentTimeMillis()) + "_book.jpg";
        File tempFile = new File(fileDir.getAbsoluteFile()+File.separator, name);
        Imgcodecs.imwrite(tempFile.getAbsolutePath(), image);
    }

    public static Bitmap getSuitableBitmap(Resources res,int id){
        if (res==null){
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,id,options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSample = 1;
        if (w>1000||h>1000){
            while (Math.max(w/inSample,h/inSample)>1000){
                inSample*=2;
            }
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bm = BitmapFactory.decodeResource(res,id,options);
        return bm;
    }

    public static Bitmap getSuitableBitmap(String path){
        if (path==null){
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSample = 1;
        if (w>1000||h>1000){
            while (Math.max(w/inSample,h/inSample)>1000){
                inSample*=2;
            }
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bm = BitmapFactory.decodeFile(path,options);
        return bm;
    }

    public static String getRealFilePath(Context context,Uri uri){
        String path = null;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            if (DocumentsContract.isDocumentUri(context,uri)){
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    path = getPathFormUri(context,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
                }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                    path = getPathFormUri(context,contentUri,null);
                }
            }else if ("content".equalsIgnoreCase(uri.getScheme())){
                path = getPathFormUri(context,uri,null);
            }else if ("file".equalsIgnoreCase(uri.getScheme())){
                path = uri.getPath();
            }
        }else{
            path = getPathFormUri(context,uri,null);
        }
        return path;
    }

    private static String getPathFormUri(Context context,Uri uri,String selection){
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri,projection,selection,null,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
