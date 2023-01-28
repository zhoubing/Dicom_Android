package com.example.dicomandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dicomandroid.photoview.PhotoView;

import org.dcm4che3.android.Raster;
import org.dcm4che3.android.RasterUtil;
import org.dcm4che3.android.imageio.dicom.DicomImageReader;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            List<String> list = Arrays.stream(getAssets().list("")).filter(s -> s.endsWith(".dcm")).collect(Collectors.toList());
            ListView listView = findViewById(R.id.list_view);
            listView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return list.size();
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View listItem = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, null);
                    TextView textView = listItem.findViewById(R.id.text);
                    textView.setText(list.get(position));
                    return listItem;
                }
            });
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(MainActivity.this, DicomActivity.class);
                intent.putExtra("fileName", list.get(position));
                startActivity(intent);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}