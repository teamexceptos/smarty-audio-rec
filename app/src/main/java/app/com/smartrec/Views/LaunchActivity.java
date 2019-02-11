package app.com.smartrec.Views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import app.com.smartrec.Auth.LoginAuth;
import app.com.smartrec.BuildConfig;
import app.com.smartrec.Listeners.DialogFragmentInterface;
import app.com.smartrec.R;
import app.com.smartrec.Utils.ContactUtils;
import app.com.smartrec.Utils.FirebaseUtils;
import app.com.smartrec.Utils.RequestPermissionHandler;
import app.com.smartrec.Utils.SharedPrefManager;
import app.com.smartrec.Utils.SmartRecBackend;
import app.com.smartrec.Utils.Utils;

/**
 * Created by Big-Nosed Developer on the Edge of Infinity.
 */
public class LaunchActivity extends AppCompatActivity implements DialogFragmentInterface, View.OnClickListener {

    private AutoRecordDialogFragment dialogFragment;
    private RequestPermissionHandler mRequestPermissionHandler;
    private SmartRecBackend smartRecBackend ;
    private TextView contactNumberTextView;

    private Boolean isRecording = false;
    private Boolean alreadyRecordedOnStart = false;
    private FloatingActionButton fab_record;

    private static final int RECORDER_SAMPLE_RATE = 44100;
    private static final int RECORDER_CHANNELS = 1;
    private static final int RECORDER_ENCODING_BIT = 16;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int MAX_DECIBELS = 120;

    private AudioRecord audioRecord;
    private BarVisualizer mVisualizer;
    AudioManager audioManager;
    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private SharedPrefManager sharedPrefManager;
    Context context = this;

    private Thread recordingThread;
    private byte[] buffer;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_activity);

        //Init-ing
        mRequestPermissionHandler = new RequestPermissionHandler();

        //First of all - Handling Permissions
        handleButtonClicked();

        //Init-ing continua
        smartRecBackend = new SmartRecBackend(LaunchActivity.this);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sharedPrefManager = new SharedPrefManager(this);

        initRecorder();
        getLastLocation();
        updateContactNumber();

        //It had to be member-level
        fab_record = findViewById(R.id.launch_record);
        contactNumberTextView = findViewById(R.id.contactNumberTV);

        FloatingActionButton fab_auth = findViewById(R.id.launch_signup);
        FloatingActionButton fab_add_contact = findViewById(R.id.launch_add_contact);
        LinearLayout ll_auth = findViewById(R.id.ll_auth);

        fab_record.setOnClickListener(this);
        fab_auth.setOnClickListener(this);
        fab_add_contact.setOnClickListener(this);
        mVisualizer = findViewById(R.id.Blob);

        if(FirebaseUtils.isUserAvailable() != null){
            ll_auth.setVisibility(View.GONE);
        }

        //Checking the auto recording state
        String autoRecordingState = Utils.getAutoRecordingState(this);

        if (autoRecordingState.equals("")) {

            //Custom Dialog Fragment to be shown
            dialogFragment = new AutoRecordDialogFragment();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), Utils.dialogFragmentTag);

        } else if (autoRecordingState.equals(Utils.autoRecordEnabled)) {

            if (!alreadyRecordedOnStart){

                //it already recorded on start
                alreadyRecordedOnStart = true;

                //Start recording on activity start..
                fab_record.setImageDrawable(ContextCompat.getDrawable(LaunchActivity.this, R.drawable.ic_stop));
                Toast.makeText(this, "Recording On Application Start..", Toast.LENGTH_SHORT).show();
                smartRecBackend.startRecording();
                mVisualizer.setAudioSessionId(audioManager.generateAudioSessionId());

                //Stop recording function
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fab_record.setImageDrawable(ContextCompat.getDrawable(LaunchActivity.this, R.drawable.ic_record_pause));
                        smartRecBackend.stopRecording(Utils.getCurrentUserUID());
                        isRecording = false;
                    }
                }, Utils.recLimitType);
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Utils.getCurrentUserUID().equals(Utils.noUidString)){
            Utils.showSnackbar(findViewById(android.R.id.content), R.string.not_logged_in_instros, R.string.log_in, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(LaunchActivity.this, LoginAuth.class));
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //If the activity is minimized
        if(isRecording){
            smartRecBackend.stopRecording(Utils.getCurrentUserUID());
            isRecording = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //If the activity suddenly closes
        if(isRecording){
            smartRecBackend.stopRecording(Utils.getCurrentUserUID());
            isRecording = false;
        }

        if (mVisualizer != null)
            mVisualizer.release();

    }

    @Override
    public void isAutoRecordEnabled(String autoRecordingState) {
        Utils.putAutoRecordingState(this, autoRecordingState);
        dialogFragment.dismiss();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.launch_record) {

            final FloatingActionButton fab = (FloatingActionButton) v;

            if (!isRecording) {
                isRecording = true;

                Toast.makeText(LaunchActivity.this, "Recording started..", Toast.LENGTH_SHORT).show();
                fab.setImageDrawable(ContextCompat.getDrawable(LaunchActivity.this, R.drawable.ic_stop));

                smartRecBackend.startRecording();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mVisualizer.setAudioSessionId(audioManager.generateAudioSessionId());
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //Stop recording after 1 min
                       smartRecBackend.stopRecording(Utils.getCurrentUserUID());
                       isRecording = false;

                       //Indicator
                       fab.setImageDrawable(ContextCompat.getDrawable(LaunchActivity.this, R.drawable.ic_record_pause));

                    }
                }, Utils.recLimitType);

            } else {

                isRecording = false;
                smartRecBackend.stopRecording(Utils.getCurrentUserUID());

                //Indicator
                fab.setImageDrawable(ContextCompat.getDrawable(LaunchActivity.this, R.drawable.ic_record_pause));
            }

        } else if (v.getId() == R.id.launch_signup) {

            startActivity(new Intent(this, LoginAuth.class));

        } else if (v.getId() == R.id.launch_add_contact) {

            //Intent to pick contacts
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), Utils.contactPickerRequestCode);
        }
    }

    private void handleButtonClicked() {
        mRequestPermissionHandler.requestPermission(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, Utils.permissionRequestCode, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() { }

            @Override
            public void onFailed() {
                Toast.makeText(LaunchActivity.this, "Request permission failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.contactPickerRequestCode && resultCode == RESULT_OK) {
            Log.d(LaunchActivity.class.getSimpleName(), "Response: " + data.toString());
            Uri uriContact = data.getData();

            //Checking if the user uid is actually there
            if (!Utils.getCurrentUserUID().equals(Utils.noUidString)) {
                String mContactName = ContactUtils.retrieveContactName(this, uriContact);
                String mContactNumber = ContactUtils.retrieveContactNumber(this, uriContact);

                //For testing purpose, i think..
                Log.d(LaunchActivity.class.getSimpleName(), "contact_details: " + mContactName + " " + mContactNumber);

                HashMap<String, Object> contactCircles = new HashMap<>();
                contactCircles.put(mContactName, /* Parsing the contact's number */ContactUtils.contactNumUtil(mContactNumber));

                //Updating the contact circle online
                FirebaseUtils.updateContactCircle(contactCircles).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //For safety purposes
                        if (task.isSuccessful()){
                            Toast.makeText(LaunchActivity.this, "Contact circle updated successfully!", Toast.LENGTH_LONG).show();

                            //Update contact number
                            updateContactNumber();
                        }else{
                            Toast.makeText(LaunchActivity.this, "Contact circle update failed. Please check your network connection and try again", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
            } else {

                Toast.makeText(this, "Please sign in or login first to add contact", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void updateContactNumber(){
        if (!Utils.getCurrentUserUID().equals(Utils.noUidString)){
            FirebaseUtils.getUserContactCirclePath().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Updating the contact number count
                    int contactdbCounts = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount()));
                    String contactString = getString(R.string.online_contacts_preamble) +String.valueOf(contactdbCounts);

                    contactNumberTextView.setText(contactString);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {

                    mLastLocation = task.getResult();

                    sharedPrefManager.saveLat(context, String.valueOf(mLastLocation.getLatitude()));
                    sharedPrefManager.saveLon(context, String.valueOf(mLastLocation.getLongitude()));

                } else {
//                            Log.w(TAG, "getLastLocation:exception", task.getException());
                    //showSnackbar(getString(R.string.no_location_detected));
                }
            }
        });
    }

    private void initRecorder() {

        final int bufferSize = 2 * AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

//        recordingThread = new Thread("recorder") {
//            @Override
//            public void run() {
//                super.run();
//                buffer = new byte[bufferSize];
//                Looper.prepare();
//                audioRecord.setRecordPositionUpdateListener(recordPositionUpdateListener, new Handler(Looper.myLooper()));
//                int bytePerSample = RECORDER_ENCODING_BIT / 8;
//                float samplesToDraw = bufferSize / bytePerSample;
//                audioRecord.setPositionNotificationPeriod((int) samplesToDraw);
//                //We need to read first chunk to motivate recordPositionUpdateListener.
//                //Mostly, for lower versions - https://code.google.com/p/android/issues/detail?id=53996
//                audioRecord.read(buffer, 0, bufferSize);
//                Looper.loop();
//            }
//        };
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(LaunchActivity.class.getSimpleName(), "onRequestPermissionResult");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Utils.permissionRequestCode) {
            if (grantResults.length <= 0) {
                Log.i(LaunchActivity.class.getSimpleName(), "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {

                Utils.showSnackbar(findViewById(android.R.id.content), R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.menu_contacts){

            startActivity(new Intent(this, CloudContacts.class));

        } else if(id == R.id.menu_recordings){

            startActivity(new Intent(this, CircleRecordings.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
