package com.example.dicomandroid;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class DicomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dicom);
        final String fileName = getIntent().getStringExtra("fileName");
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream is = getAssets().open(fileName);
                    String filePath = getCacheDir().getAbsolutePath() + "/" + fileName;
                    File destFile = new File(filePath);
                    copyFile(is, destFile);
                    readFile(destFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DicomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    private void copyFile(InputStream is, File dstFile) {
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstFile), 1024);
            byte[] buf = new byte[1024];
            int c = bis.read(buf);
            while (c > 0) {
                bos.write(buf, 0, c);
                c = bis.read(buf);
            }
            bis.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void readFile(File dstFile) {
        DicomImageReader dr = new DicomImageReader();
        try {
            DicomInputStream dicomInputStream = new DicomInputStream(dstFile);
            String transferSyntax = dicomInputStream.getTransferSyntax();
            Log.e("Dicom", "压缩协议: " + transferSyntax);

            Attributes attributes = dicomInputStream.readDataset(-1, -1);
            Log.e("Dicom", "所有Dicom属性信息: " + attributes);
            int row = attributes.getInt(Tag.Rows, 1);
            int columns = attributes.getInt(Tag.Columns, 1);
            float windowCenter = attributes.getFloat(Tag.WindowCenter, 1);
            float windowWidth = attributes.getFloat(Tag.WindowWidth, 1);
            Log.e("Dicom", "windowCenter: " + windowCenter + " windowWidth: " + windowWidth);
            byte[] b = attributes.getSafeBytes(Tag.PixelData);
            if (b != null) {
                Log.e("Dicom", "b.length= " + b.length);
            } else {
                Log.e("Dicom", "b.length=null");
            }
            attributes.setString(Tag.SpecificCharacterSet, VR.CS, "GB18030");
            Log.e("Dicom", "所有Dicom属性信息: " + attributes);

            String patientName = attributes.getString(Tag.PatientName, "");
            String patientBirthDate = attributes.getString(Tag.PatientBirthDate, "");
            String institution = attributes.getString(Tag.InstitutionName, "");
            String station = attributes.getString(Tag.StationName, "");
            String manufacturer = attributes.getString(Tag.Manufacturer, "");
            String manufacturerModelName = attributes.getString(Tag.ManufacturerModelName, "");
            String description = attributes.getString(Tag.StudyDescription, "");
            String seriesDescription = attributes.getString(Tag.SeriesDescription, "");
            String studyData = attributes.getString(Tag.StudyDate, "");

            Log.e("Dicom", "patientName: " + patientName);
            Log.e("Dicom", "patientBirthDate: " + patientBirthDate);
            Log.e("Dicom", "institution: " + institution);
            Log.e("Dicom", "station: " + station);
            Log.e("Dicom", "manufacturer: " + manufacturer);
            Log.e("Dicom", "manufacturerModelName: " + manufacturerModelName);
            Log.e("Dicom", "description: " + description);
            Log.e("Dicom", "seriesDescription: " + seriesDescription);
            Log.e("Dicom", "studyData: " + studyData);

            dr.open(dstFile);
            Attributes ds = dr.getAttributes();
            String wc = ds.getString(Tag.WindowCenter);
            String ww = ds.getString(Tag.WindowWidth);
            Log.e("Dicom", "windowCenter: " + wc + " windowWidth: " + ww);
            Raster raster = dr.applyWindowCenter(0, (int) windowWidth, (int) windowCenter);
            Log.e("Dicom", "raster.getWidth(): " + raster.getWidth() + " raster.getHeight(): " + raster.getHeight());
            Log.e("Dicom", "raster.getByteDate().length=: " + raster.getByteData().length);
            Bitmap bitmap = RasterUtil.gray8ToBitmap(columns, row, raster.getByteData());
            runOnUiThread(() -> {
                PhotoView photoView = findViewById(R.id.photo);
                photoView.setImageBitmap(bitmap);
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
