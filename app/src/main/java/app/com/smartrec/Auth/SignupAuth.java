package app.com.smartrec.Auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import app.com.smartrec.Listeners.OnProfileCreatedListener;
import app.com.smartrec.Utils.ContactUtils;
import app.com.smartrec.Views.LaunchActivity;
import app.com.smartrec.R;
import app.com.smartrec.Utils.SharedPrefManager;
import app.com.smartrec.Models.recUserModel;

import static app.com.smartrec.Utils.FirebaseUtils.addRegistrationToken;

/**
 * Created by ${cosmic} on 7/3/18.
 */
public class SignupAuth extends AppCompatActivity implements OnProfileCreatedListener {

    private SharedPrefManager sharedPrefManager;
    Context context = this;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    EditText et_fullname, et_email, et_phonenumber, et_password, et_secpassword;
    TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_signup);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle("Sign Up");
            bar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPrefManager = new SharedPrefManager(context);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth =  FirebaseAuth.getInstance();

        et_fullname = findViewById(R.id.et_fullname);
        et_email = findViewById(R.id.et_loginemail);
        et_phonenumber = findViewById(R.id.et_number);
        et_password = findViewById(R.id.et_loginpassword);
        et_secpassword = findViewById(R.id.et_secpassword);
        signup = findViewById(R.id.signup);
        progressBar = findViewById(R.id.signup_progress_bar);


        //Changes -- loginactivity is now a button
        Button loginactivity = findViewById(R.id.login_activity);
        loginactivity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupAuth.this, LoginAuth.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fullname = et_fullname.getText().toString().trim();
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String password2 = et_secpassword.getText().toString().trim();

                if (TextUtils.isEmpty(fullname)) {
                    Toast.makeText(getApplicationContext(), "Enter fullname!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Objects.equals(password, password2)){
                    Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                sharedPrefManager.saveName(context, fullname);
                sharedPrefManager.saveEmail(context, email);
                sharedPrefManager.savePhonenumber(context, et_phonenumber.getText().toString());


                //create user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupAuth.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignupAuth.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.VISIBLE);
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    onProfileCreated(task.isSuccessful());
                                } else {
                                    Toast.makeText(SignupAuth.this, "Sign Up Failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    @Override
    public void onProfileCreated(boolean success) {

        final recUserModel recusermodel = new recUserModel();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        recusermodel.setFullname(sharedPrefManager.getName());
        recusermodel.setEmail(sharedPrefManager.getUserEmail());
        recusermodel.setPhonenumber(ContactUtils.contactNumUtil(sharedPrefManager.getPhonenumber()));
        recusermodel.setUid(mUser.getUid());

        Map<String, Object> postValues = recusermodel.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/smartrec-users/" + mUser.getUid(), postValues);
        databaseReference.updateChildren(childUpdates);
        addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), mUser.getUid());

        Toast.makeText(SignupAuth.this, "Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignupAuth.this, LaunchActivity.class));
        finish();
    }
}
