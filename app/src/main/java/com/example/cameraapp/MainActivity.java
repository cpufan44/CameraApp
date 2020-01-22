package com.example.cameraapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button mCamera;
    private Button mGallery;
    private ImageView mThumbail;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int GALLERY_REQUEST_CODE = 2;
    private static String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCamera = findViewById(R.id.button_camera);
        mGallery = findViewById(R.id.button_gallery);
        mThumbail = findViewById(R.id.trml_pic);

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent TakePictureintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (TakePictureintent.resolveActivity(getPackageManager()) != null) {

                    File myImage = null;
                    try {
                        myImage = createImageFile();
                    } catch (Exception ex) {
                        Log.d("camera error", ex.getMessage());
                    }

                    if (myImage != null) {

                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                "com.example.android.fileprovider",
                                myImage);
                        TakePictureintent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(TakePictureintent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Create an Intent with action as ACTION_PICK
                Intent intent = new Intent(Intent.ACTION_PICK);
                // Sets the type as image/*. This ensures only components of type image are selected
                intent.setType("image/*");
                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                // Launching the Intent
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String orientString = null;
            if (exif != null)
                orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            // Rotate Bitmap

            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);


            Bitmap finalImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            mThumbail.setImageBitmap(finalImg);

        } else if (resultCode == RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    //data.getData return the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();
                    //Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //Gets the String value in the column
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    // Set the Image in ImageView after decoding the String

                    currentPhotoPath = imgDecodableString;

                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(currentPhotoPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String orientString = null;
                    if (exif != null)
                        orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                    int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
                    int rotationAngle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
                    // Rotate Bitmap

                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotationAngle);

                    Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
                    Bitmap finalImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    mThumbail.setImageBitmap(finalImg);
                    break;
            }

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
