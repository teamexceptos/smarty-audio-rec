package app.com.smartrec.Views;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import app.com.smartrec.Adapters.CloudContactRecAdapter;
import app.com.smartrec.Listeners.OnDataChangedListener;
import app.com.smartrec.Models.cloudcontactModel;
import app.com.smartrec.R;
import app.com.smartrec.Utils.FirebaseUtils;
import app.com.smartrec.Utils.Utils;

/**
 * Created by ${cosmic} on 2/7/19.
 */
public class CloudContacts extends AppCompatActivity {

    CloudContactRecAdapter cloudContactRecAdapter;
    RecyclerView cloudcontact_rv;
    Context context = this;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_contacts);

        cloudcontact_rv = findViewById(R.id.cloudcontacts_rv);
        LinearLayout cc_lyt = findViewById(R.id.cloudcontacts_lyt);
        TextView cc_tv = findViewById(R.id.no_internet_or_list);

        //Setting up the home button to go back
        ActionBar bar = getSupportActionBar();
        if (bar != null){
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("Cloud Contacts");
        }

        if (Utils.getCurrentUserUID().equals(Utils.noUidString)){

            //If user does not exist
            cloudcontact_rv.setVisibility(View.GONE);
            cc_lyt.setVisibility(View.VISIBLE);
            cc_tv.setText("Sign up to add Contacts to cloud");

        } else {

            cc_lyt.setVisibility(View.GONE);
            cloudcontact_rv.setVisibility(View.VISIBLE);
            initCloudContactViews();
        }

    }

    public void initCloudContactViews(){

        cloudContactRecAdapter = new CloudContactRecAdapter();
        cloudContactRecAdapter.setCallback(new CloudContactRecAdapter.Callback() {
            @Override
            public void onItemClick(View view, cloudcontactModel cloudcontactM) {


            }
        });

        //Init-ing recycler
        cloudcontact_rv.setLayoutManager(new LinearLayoutManager(CloudContacts.this));
        cloudcontact_rv.setAdapter(cloudContactRecAdapter);
        cloudcontact_rv.setHasFixedSize(true);
        cloudcontact_rv.setNestedScrollingEnabled(false);
        getCloudContactlist(ChangedDataListener());
    }


    public void getCloudContactlist(final OnDataChangedListener<cloudcontactModel> onDataChangedListener){

        if (!Utils.getCurrentUserUID().equals(Utils.noUidString)){
            FirebaseUtils.getUserContactCirclePath().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    List<cloudcontactModel> cloudlist = new ArrayList<>();

                    for (DataSnapshot contacts_snaps : dataSnapshot.getChildren()){

                        cloudcontactModel cloudcontactM = new cloudcontactModel();
                        cloudcontactM.setContact_name(contacts_snaps.getKey());
                        cloudcontactM.setContact_number(contacts_snaps.getValue().toString());

                        cloudlist.add(cloudcontactM);
                    }

                    onDataChangedListener.onListChanged(cloudlist);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private OnDataChangedListener<cloudcontactModel> ChangedDataListener() {
        Utils.setDialog(context, true);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.setDialog(context, false);
            }
        }, 30000);

        return new OnDataChangedListener<cloudcontactModel>() {
            @Override
            public void onListChanged(List<cloudcontactModel> list) {

                Utils.setDialog(context, false);
                cloudContactRecAdapter.setList(list);
            }
        };
    }

}
