package app.com.smartrec.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.smartrec.Listeners.OnObjectChangedListener;
import app.com.smartrec.Listeners.OnProfileCreatedListener;
import app.com.smartrec.Views.LaunchActivity;
import app.com.smartrec.R;
import app.com.smartrec.Utils.SharedPrefManager;
import app.com.smartrec.Models.recUserModel;

/**
 * Created by ${cosmic} on 7/13/18.
 */
public class LoginAuth extends AppCompatActivity implements OnProfileCreatedListener {
    public static final String TAG = LoginAuth.class.getSimpleName();

    FirebaseAuth mAuth;
    EditText email, pw;
    Button loginUser, signupUser;
    ProgressBar progressBar;
    FirebaseDatabase database;
    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle("Login");
            bar.setDisplayHomeAsUpEnabled(true);
        }

        mAuth =  FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress_bar);

        email = findViewById(R.id.et_loginemail);
        pw = findViewById(R.id.et_loginpassword);

        sharedPrefManager = new SharedPrefManager(this);
        database = FirebaseDatabase.getInstance();

        loginUser = findViewById(R.id.login_btn);
        signupUser = findViewById(R.id.back_to_signup);

        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Userlogin(v);
            }
        });

        signupUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginAuth.this, SignupAuth.class));
                finish();
            }
        });

    }

    public void Userlogin(View v) {

        String l_email = email.getText().toString().trim();
        final String l_password = pw.getText().toString().trim();

        if (TextUtils.isEmpty(l_email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(l_password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(l_email, l_password)
                .addOnCompleteListener(LoginAuth.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            if (l_password.length() < 6) {
                                pw.setError("Password is too short, Minimum Password is 6");
                            } else {
                                Toast.makeText(LoginAuth.this, "Wrong Password or Email", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            onProfileCreated(task.isSuccessful());
                            Intent intent = new Intent(LoginAuth.this, LaunchActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onProfileCreated(boolean success) {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        loadProfile(mUser.getUid());
    }

    private void loadProfile(String currentUserId) {
        getProfile(currentUserId, createOnProfileChangedListener());
    }

    private OnObjectChangedListener<recUserModel> createOnProfileChangedListener() {
        return new OnObjectChangedListener<recUserModel>() {
            @Override
            public void onObjectChanged(recUserModel obj) {
                SavePersist(obj);
            }
        };
    }

    private void SavePersist(recUserModel profile) {

        if (profile != null) {
            sharedPrefManager.saveName(this, profile.getFullname());
            sharedPrefManager.saveUiD(this, profile.getUid());
            sharedPrefManager.saveEmail(this, profile.getEmail());
        }
    }

    public void getProfile(String id, final OnObjectChangedListener<recUserModel> listener) {
        DatabaseReference databaseReference = database.getReference().child("smartrec-users").child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recUserModel profile = dataSnapshot.getValue(recUserModel.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getProfile(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
        //activeListeners.put(valueEventListener, databaseReference);
    }
}
