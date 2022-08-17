package com.app.test1;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.test1.api.ApiClient;
import com.app.test1.api.ApiInterface;
import com.app.test1.model.Data;
import com.app.test1.model.height.Height;

import java.io.File;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    TextView textViewPulse,textViewOxygen,textViewTemperature,textViewHeight;
    private ApiInterface apiInterface;
    ProgressBar progressBar;
    ConstraintLayout layout;
    SwipeRefreshLayout mSwipeRefreshLayout;
    double pulse,oxygen,temperature;
    MediaPlayer mediaPlayer;
    boolean prepared=false;
    boolean started=false;
    Button buttonPlay;
    String stream="http://192.168.88.72/";

    // Initializing all variables..
    private TextView startTV, stopTV, playTV, stopplayTV, statusTV;

    // creating a variable for medi recorder object class.
    private MediaRecorder mRecorder;

    // creating a variable for mediaplayer class
    private MediaPlayer mPlayer;

    // string variable is created for storing a file name
    private static String mFileName = null;

    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewPulse = findViewById(R.id.textViewPulse);
        textViewOxygen = findViewById(R.id.textViewOxygen);
        textViewHeight = findViewById(R.id.textViewHeight);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        progressBar= findViewById(R.id.progressBar);
        buttonPlay=findViewById(R.id.buttonPlay);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        layout = findViewById(R.id.layout);

        // initialize all variables with their layout items.
        statusTV = findViewById(R.id.idTVstatus);
        startTV = findViewById(R.id.btnRecord);
        stopTV = findViewById(R.id.btnStop);
        playTV = findViewById(R.id.btnPlay);
        stopplayTV = findViewById(R.id.btnStopPlay);
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        playTV.setBackgroundColor(getResources().getColor(R.color.gray));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.gray));

        startTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start recording method will
                // start the recording of audio.
                startRecording();
            }
        });
        stopTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause Recording method will
                // pause the recording of audio.
                pauseRecording();

            }
        });
        playTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // play audio method will play
                // the audio which we have recorded
                playAudio();
            }
        });
        stopplayTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause play method will
                // pause the play of audio
                pausePlaying();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.purple_700);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                getHeightData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        buttonPlay.setEnabled(false);
        buttonPlay.setText("Loading..");
        mediaPlayer= new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new PlayerTask().execute(stream);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(started){
                    mediaPlayer.pause();
                    buttonPlay.setText("Play");
                    started=false;
                }else{
                    mediaPlayer.start();
                    buttonPlay.setText("Pause");
                    started=true;
                }
            }
        });
        getData();
        getHeightData();
    }

    private void getData() {
        layout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Data> call = apiInterface.getData("CRJVGC27S9JP8HHI");
        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                if (response.code()==200) {
                    progressBar.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                    Data body=response.body();
                    textViewPulse.setText(body.getFeeds().get(body.getFeeds().size()-1).getField1());
                    textViewOxygen.setText(body.getFeeds().get(body.getFeeds().size()-1).getField2());
                    textViewTemperature.setText(body.getFeeds().get(body.getFeeds().size()-1).getField3());
                    pulse=Double.parseDouble(body.getFeeds().get(body.getFeeds().size()-1).getField1());
                    oxygen=Double.parseDouble(body.getFeeds().get(body.getFeeds().size()-1).getField2());
                    temperature=Double.parseDouble(body.getFeeds().get(body.getFeeds().size()-1).getField3());

                    if (pulse<60 && oxygen<94 && temperature<97) {
                        sendNotification("Your Pulse rate, Oxygen level & Temperature is Low!");
                    }
                     if(pulse>100 && oxygen>100 && temperature>99)
                    {
                        sendNotification("Your Pulse rate, Oxygen level & Temperature is High!");
                    }
                     if(pulse>100 && oxygen>100)
                    {
                        sendNotification("Your Pulse rate & Oxygen level is High!");
                    }
                     if(pulse>100 && temperature>99)
                    {
                        sendNotification("Your Pulse rate & Temperature is High!");
                    }
                     if(oxygen>100 && temperature>99)
                    {
                        sendNotification("Your Oxygen level & Temperature is High!");
                    }
                     if(pulse<60 && oxygen<94)
                    {
                        sendNotification("Your Pulse rate & Oxygen level is Low!");
                    }
                     if(pulse<60 && temperature<97)
                    {
                        sendNotification("Your Pulse rate & Temperature is Low!");
                    }
                     if(oxygen<94 && temperature<97)
                    {
                        sendNotification("Your Oxygen level & Temperature is Low!");
                    }
                     if(pulse>100)
                    {
                        sendNotification("Your Pulse rate is High!");
                    }
                     if(oxygen>100)
                    {
                        sendNotification("Your Oxygen level is High!");
                    }
                     if(temperature>99)
                    {
                        sendNotification("Your Temperature is High!");
                    }
                     if(pulse<60)
                    {
                        sendNotification("Your Pulse rate is Low!");
                    }
                     if(oxygen<94)
                    {
                        sendNotification("Your Oxygen level is Low!");
                    }
                     if(temperature<97)
                    {
                        sendNotification("Your Temperature is Low!");
                    }
                }
            }
            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
                t.printStackTrace();
            }
        });
    }

    private void getHeightData() {
        layout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Height> call = apiInterface.getHeightData("GEAWZO7Y24UC8F58");
        call.enqueue(new Callback<Height>() {
            @Override
            public void onResponse(Call<Height> call, Response<Height> response) {
                if (response.code()==200) {
                    progressBar.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                    Height body=response.body();
                    textViewHeight.setText(body.getFeeds().get(body.getFeeds().size()-1).getField1());
                }
            }
            @Override
            public void onFailure(Call<Height> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
                t.printStackTrace();
            }
        });
    }

    private void sendNotification(String details) {
        Uri urinotification = null;
        try {
            urinotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), urinotification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "Notification")
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(getString(R.string.alert))
                        .setSound(urinotification)
                        .setContentText(details);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

// Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    class PlayerTask extends AsyncTask<String,Void,Boolean>{
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.prepare();
                prepared = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return prepared;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            buttonPlay.setEnabled(true);
            buttonPlay.setText("Play");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (started)
        {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (started)
        {
            mediaPlayer.start();
        }
    }

    private void startRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record nd store the audio.
        if (CheckPermissions()) {

            // setbackgroundcolor method will change
            // the background color of text view.
            stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
            startTV.setBackgroundColor(getResources().getColor(R.color.gray));
            playTV.setBackgroundColor(getResources().getColor(R.color.gray));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.gray));

            // we are here initializing our filename variable
            // with the path of the recorded audio file.
            mFileName = Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator;
            mFileName += "/StethoRecording.mp3";

            // below method is used to initialize
            // the media recorder class
            mRecorder = new MediaRecorder();

            // below method is used to set the audio
            // source which we are using a mic.
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // below method is used to set
            // the output format of the audio.
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            // below method is used to set the
            // audio encoder for our recorded audio.
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            // below method is used to set the
            // output file location for our recorded audio
            mRecorder.setOutputFile(mFileName);
            try {
                // below method will prepare
                // our audio recorder class
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            // start method will start
            // the audio recording.
            mRecorder.start();
            statusTV.setText("Recording Started");
        } else {
            // if audio recording permissions are
            // not granted by user below method will
            // ask for runtime permission for mic and storage.
            RequestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }


    public void playAudio() {
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        playTV.setBackgroundColor(getResources().getColor(R.color.gray));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200));

        // for playing our recorded audio
        // we are using media player class.
        mPlayer = new MediaPlayer();
        try {
            // below method is used to set the
            // data source which will be our file name
            mPlayer.setDataSource(mFileName);

            // below method will prepare our media player
            mPlayer.prepare();

            // below method will start our media player.
            mPlayer.start();
            statusTV.setText("Recording Started Playing");
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
    }

    public void pauseRecording() {
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        playTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200));

        // below method will stop
        // the audio recording.
        mRecorder.stop();

        // below method will release
        // the media recorder class.
        mRecorder.release();
        mRecorder = null;
        statusTV.setText("Recording Stopped");
    }

    public void pausePlaying() {
        // this method will release the media player
        // class and pause the playing of our recorded audio.
        mPlayer.release();
        mPlayer = null;
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        playTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.gray));
        statusTV.setText("Recording Play Stopped");
    }
}
