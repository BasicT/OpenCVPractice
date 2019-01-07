package com.example.opencvpractice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.opencvpractice.datamodel.AppConstants;
import com.example.opencvpractice.datamodel.ChapterUtils;
import com.example.opencvpractice.datamodel.ItemDto;
import com.example.opencvpractice.datamodel.SectionsListViewAdaptor;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity{

    private static final String CV_TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniLoadOpenCV();
        initListView();
    }

    private void iniLoadOpenCV(){
        boolean success = OpenCVLoader.initDebug();
        if (success){
            Log.i(CV_TAG,"OpenCV Libraries loaded...");
        }else {
            Toast.makeText(this.getApplicationContext(),"WARNING: Could not load OpenCV" +
                    "Libraries!",Toast.LENGTH_SHORT).show();
        }
    }

    private void initListView(){
        ListView listView = (ListView)findViewById(R.id.chapter_listView);
        final SectionsListViewAdaptor commandAadptor = new SectionsListViewAdaptor(this);
        listView.setAdapter(commandAadptor);
        commandAadptor.getDataModel().addAll(ChapterUtils.getChapters());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemDto dto = commandAadptor.getDataModel().get(position);
                goSectionList(dto);
            }
        });
        commandAadptor.notifyDataSetChanged();
    }

    private void goSectionList(ItemDto dto){
        Intent intent = new Intent(this.getApplicationContext(),SectionsActivity.class);
        intent.putExtra(AppConstants.ITEM_KEY,dto);
        startActivity(intent);
    }
}
