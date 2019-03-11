package com.example.opencvpractice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.opencvpractice.datamodel.AppConstants;
import com.example.opencvpractice.datamodel.ChapterUtils;
import com.example.opencvpractice.datamodel.ItemDto;
import com.example.opencvpractice.datamodel.SectionsListViewAdaptor;
import com.example.opencvpractice.extra.MyCropView;

public class SectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);
        ItemDto dto = (ItemDto)getIntent().getSerializableExtra(AppConstants.ITEM_KEY);
        initListView(dto);
    }

    private void initListView(ItemDto dto){
        ListView listView = (ListView)findViewById(R.id.secction_listView);
        final SectionsListViewAdaptor commandAdaptor = new SectionsListViewAdaptor(this);
        listView.setAdapter(commandAdaptor);
        commandAdaptor.getDataModel().addAll(ChapterUtils.getSections((int)dto.getId()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String command = commandAdaptor.getDataModel().get(position).getName();
                goDemoView(command);
            }
        });
        commandAdaptor.notifyDataSetChanged();
    }

    private void goDemoView(String command){
        if (command.equals(AppConstants.CHAPTER_1TH_PGM_01)){
            Intent intent = new Intent(this.getApplicationContext(),ChapterFirst1Activity.class);
            startActivity(intent);
        }
        if (command.equals(AppConstants.CHAPTER_2TH_PGM_01)){
            Intent intent = new Intent(this.getApplicationContext(),TwoSectionActivity.class);
            startActivity(intent);
        }
        if (command.equals(AppConstants.CHAPTER_3TH_PGM)){
            Intent intent = new Intent(this.getApplicationContext(),ThreeSectionActivity.class);
            startActivity(intent);
        }
        if (command.equals(AppConstants.CHAPTER_4TH_PGM)){
            Intent intent = new Intent(this.getApplicationContext(),FourSectionActivity.class);
            startActivity(intent);
        }
        if (command.equals(AppConstants.CHAPTER_5TH_PGM)){
            Intent intent = new Intent(this.getApplicationContext(),FiveSectionActivity.class);
            startActivity(intent);
        }
        if (command.equals(AppConstants.CHAPTER_6TH_PGM)){
            Intent intent = new Intent(this.getApplicationContext(), CropActivity.class);
            startActivity(intent);
        }
    }
}
