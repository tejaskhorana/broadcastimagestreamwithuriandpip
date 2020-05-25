package com.example.applicationtwo;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.io.FileNotFoundException;
import java.io.IOException;

/** This Activity receives broadcasted images. */
public class MainActivity extends AppCompatActivity {
  private MyBroadcastReceiver myReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // Register receiver to receive broadcast from external app
    IntentFilter filter = new IntentFilter("com.example.applicationone.SCANNED_IMAGE");

    try {
      filter.addDataType("image/*");
    } catch (MalformedMimeTypeException e) {
      e.printStackTrace();
    }
    myReceiver = new MyBroadcastReceiver();
    registerReceiver(myReceiver, filter);
  }

  public class MyBroadcastReceiver extends BroadcastReceiver
  {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.w("ApplicationTwo", "Broadcast Received.");

      // Get the file's URI from incoming Intent.
      Uri returnUri = intent.getData();

      // Retrieve and log the file's display name and size.
      ContentResolver contentResolver = getContentResolver();
      Cursor returnCursor = contentResolver.query(returnUri, null, null, null, null);
      int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
      returnCursor.moveToFirst();

      String fileName = returnCursor.getString(nameIndex);
      Log.w("ApplicationTwo", "File received of name " + fileName  + " and size " + returnCursor.getLong(sizeIndex) + ". This file is a " + contentResolver.getType(returnUri) + " type.");
      TextView textView = findViewById(R.id.textView);
      textView.setText(fileName);

      Bitmap bitmap;
      try {
        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, returnUri);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return;
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      ImageView imageView = findViewById(R.id.image_view);
      imageView.setImageBitmap(bitmap);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if(myReceiver != null)
      unregisterReceiver(myReceiver);
  }
}
