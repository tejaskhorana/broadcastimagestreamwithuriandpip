package com.example.applicationone;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** This Activity sends the broadcast. */
public class MainActivity extends AppCompatActivity {
  private static final String ACTION_MEDIA_CONTROL = "media_control";
  private static final String EXTRA_CONTROL_TYPE = "control_type";
  private static final int CONTROL_TYPE_RECORD = 1;

  private int count = 0;
  private ImageView imageView;
  private TextView textView;

  /** A {@link BroadcastReceiver} to receive action item events from Picture-in-Picture mode. */
  private BroadcastReceiver mReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setUpFullScreen();
  }

  private void setUpFullScreen() {
    setContentView(R.layout.activity_main);

    // Send a broadcast with a provided text.
    Button sendBroadcastButton = findViewById(R.id.sendBroadcast);
    imageView = findViewById(R.id.image_view);
    textView = findViewById(R.id.text_view);
    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (count++ < 200) {
          int subIteration = count % 3;
          int imageId = subIteration == 0 ? R.drawable.moonhighres_icon : subIteration == 1 ? R.drawable.cloudhighres_icon : R.drawable.sunhighres_icon;

          // Create Image and Text representing file that is going to be broadcasted
          imageView.setImageResource(imageId);
          textView.setText("File" + count + ".jpg");

          // Save Images and broadcast Content URIs
          createAndSendIntent(imageId, count);

          // Waits 100 ms between each iteration.
          handler.postDelayed(this, 100);
        }
      }
    };
    sendBroadcastButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        handler.post(runnable);
      }
    });

    if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
      Log.w("ApplicationOne", "System does not have the picture in picture feature.");
    }

    Button pipButton = findViewById(R.id.pipButton);
    pipButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        enterPictureInPictureMode();
        return;
      }
    });
  }

  private void setUpPictureInPicture() {
    setContentView(R.layout.window_main);
    imageView = findViewById(R.id.image_view_window);
    mReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            if (intent == null
                || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
              return;
            }

            // This is where we are called back from Picture-in-Picture action
            // items.
            final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
            switch (controlType) {
              case CONTROL_TYPE_RECORD:
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                  @Override
                  public void run() {
                    if (count++ < 200) {
                      int subIteration = count % 3;
                      int imageId = subIteration == 0 ? R.drawable.moonhighres_icon : subIteration == 1 ? R.drawable.cloudhighres_icon : R.drawable.sunhighres_icon;

                      // Create Image and Text representing file that is going to be broadcasted
                      imageView.setImageResource(imageId);

                      // Save Images and broadcast Content URIs
                      createAndSendIntent(imageId, count);

                      // Waits 100 ms between each iteration.
                      handler.postDelayed(this, 100);
                    }
                  }
                };
                handler.post(runnable);
                break;
            }
          }
        };
    registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
  }

  @Override
  public void onUserLeaveHint() {
    enterPictureInPictureMode(getPictureInPictureParams());
  }

  private PictureInPictureParams getPictureInPictureParams() {
    PictureInPictureParams.Builder mPictureInPictureParamsBuilder =
        new PictureInPictureParams.Builder();
    return mPictureInPictureParamsBuilder.setActions(getRemoteActions()).build();
  }

  private List<RemoteAction> getRemoteActions() {
    ArrayList<RemoteAction> remoteActions = new ArrayList<>();

    PendingIntent intent =
        PendingIntent.getBroadcast(
            MainActivity.this,
            1,
            new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, CONTROL_TYPE_RECORD),
            0);
    final Icon icon = Icon.createWithResource(MainActivity.this, R.drawable.record);
    remoteActions.add(new RemoteAction(icon, "Record", "Record", intent));
    return remoteActions;
  }

  @Override
  public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
    if (isInPictureInPictureMode) {
      setUpPictureInPicture();


    } else {
      setUpFullScreen();
    }
  }

  private void createAndSendIntent(int imageId, int iteration) {
    Intent intent = new Intent();
    intent.setAction("com.example.applicationone.SCANNED_IMAGE");

    // Path of root directory of this application's internal storage.
    File rootDirectory = getFilesDir();

    // Get the files/images subdirectory.
    File imagesDirectory = new File(rootDirectory, "images");
    if(!imagesDirectory.exists()) {
      imagesDirectory.mkdir();
    }

    // Create file name.
    String fileName = "File" + iteration + ".jpg"; // Change depending on file type.
    File imageFilePath = new File(imagesDirectory, fileName);

    // Save file to internal file system.
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(imageFilePath);
      Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId);
      bitmap.compress(CompressFormat.JPEG, 100, fos); // Change depending on file type
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      try {
        fos.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Get File we want to send.
    File requestFile = new File(imageFilePath.getAbsolutePath());

    Uri fileUri = null;

    // Get URI of this File.
    try {
      fileUri = FileProvider.getUriForFile(
          MainActivity.this,
          "com.example.applicationone.fileprovider",
          requestFile
      );
    } catch (IllegalArgumentException e) {
      Log.w("ApplicationOne", "IllegalArgumentException when retrieving Uri. Selected file cannot be shared.");
      return;
    }

    // If URI retrieved, attach permissions and data type. Send broadcast.
    if (fileUri != null) {
      grantUriPermission("com.example.applicationtwo", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
      Log.w("ApplicationOne", "Successfully sent broadcast");
      sendBroadcast(intent);
    } else {
      Log.w("ApplicationOne", "Failed to send broadcast. fileUri is null.");
    }
  }
}