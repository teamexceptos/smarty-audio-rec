package app.com.smartrec.Views;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.com.smartrec.Adapters.RecievedRecAdapter;
import app.com.smartrec.Listeners.OnDataChangedListener;
import app.com.smartrec.Models.recRecievedModel;
import app.com.smartrec.R;
import app.com.smartrec.Utils.FirebaseUtils;
import app.com.smartrec.Utils.SharedPrefManager;
import app.com.smartrec.Utils.SmartRecBackend;
import app.com.smartrec.Utils.Utils;

public class CircleRecordings extends AppCompatActivity {

    private RecyclerView circleRecordingRecycler;
    private RecievedRecAdapter recievedRecAdapter;
    private SharedPrefManager sharedPrefManager;

    private boolean attemptToLoadProfiles = false;
    private boolean mAddressRequested;
    private boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_recordings);

        //Setting up the home button to go back
        ActionBar bar = getSupportActionBar();
        if (bar != null){
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(R.string.circle_recordings_title);
        }

        circleRecordingRecycler = findViewById(R.id.circle_recordings_recycler);
        LinearLayout circleRecordingLinear = findViewById(R.id.circle_recordings_error_layout);
        LinearLayout locLinear = findViewById(R.id.loc_layout);
        TextView lat = findViewById(R.id.latitude_text);
        TextView lon = findViewById(R.id.longitude_text);
        TextView error_textView = circleRecordingLinear.findViewById(R.id.circle_recordings_error_message_tv);

        sharedPrefManager = new SharedPrefManager(this);

        if (Utils.getCurrentUserUID().equals(Utils.noUidString)){

            //If user does not exist
            circleRecordingRecycler.setVisibility(View.GONE);
            circleRecordingLinear.setVisibility(View.VISIBLE);
            error_textView.setText(R.string.circle_recording_no_user_message);

        } else {

            //Just in case
            lon.setText(sharedPrefManager.getLon());
            lat.setText(sharedPrefManager.getLat());

            locLinear.setVisibility(View.VISIBLE);
            circleRecordingLinear.setVisibility(View.GONE);
            circleRecordingRecycler.setVisibility(View.VISIBLE);
            initRecyclerView();
        }
    }

    private void initRecyclerView() {

        recievedRecAdapter = new RecievedRecAdapter();
        recievedRecAdapter.setCallback(new RecievedRecAdapter.Callback() {
            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void recStream(final String recIDtoStream, final String recStreamKey, View view) {
                if (recIDtoStream != null){
                    SmartRecBackend smartRecBackend = new SmartRecBackend(CircleRecordings.this);

                    //One exception sha has to be caught
                    try{
                        smartRecBackend.StreamRec(recIDtoStream);
                    }catch (Exception exception){
                        Log.e(CircleRecordings.class.getSimpleName(), exception.getLocalizedMessage());
                    }

                }
            }
        });

        //Init-ing recycler
        circleRecordingRecycler.setLayoutManager(new LinearLayoutManager(CircleRecordings.this));
        circleRecordingRecycler.setAdapter(recievedRecAdapter);
        circleRecordingRecycler.setHasFixedSize(true);
        circleRecordingRecycler.setNestedScrollingEnabled(false);

        getRecRecievedList(createOnProfilesChangedDataListener());
    }

    private OnDataChangedListener<recRecievedModel> createOnProfilesChangedDataListener() {
        Utils.setDialog(this, true);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.setDialog(getBaseContext(), false);
            }
        }, 30000);

        return new OnDataChangedListener<recRecievedModel>() {
            @Override
            public void onListChanged(List<recRecievedModel> list) {
                Utils.setDialog(getBaseContext(), false);
                recievedRecAdapter.setList(list);
            }
        };
    }

    public void getRecRecievedList(final OnDataChangedListener<recRecievedModel> onDataChangedListener) {
        FirebaseUtils.getReceivedRecordings().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recRecievedModel recRecievedModel = new recRecievedModel();
                List<recRecievedModel> list = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    recRecievedModel.setSentRec(snapshot.child("SentRec").getValue().toString());
                    recRecievedModel.setSenderUid(snapshot.child("senderUid").getValue().toString());
                    recRecievedModel.setSendDate(Long.parseLong(snapshot.child("sentDate").getValue().toString()));
                    recRecievedModel.setSenderLongitude(snapshot.child("senderLongitude").getValue().toString());
                    recRecievedModel.setSenderLatitude(snapshot.child("senderLatitude").getValue().toString());
                    list.add(recRecievedModel);
                }

                Collections.sort(list, new Comparator<recRecievedModel>() {
                    @Override
                    public int compare(recRecievedModel lhs, recRecievedModel rhs) {
                        return (rhs.getSendDate()).compareTo(lhs.getSendDate());
                    }
                });

                onDataChangedListener.onListChanged(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(CircleRecordings.class.getSimpleName(), "getCommentsList(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }
}
