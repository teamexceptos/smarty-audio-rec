package app.com.smartrec.Utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by Big-Nosed Developer on the Edge of Infinity.
 */
public class FirebaseUtils {

    private static final String smartRecUsersDBPath = "smartrec-users";
    private static final String contactCirclePath = "ContactCircle";
    private static final String smartRecRecordingsPath = "recievedEnRec";


    private static FirebaseDatabase getFirebaseDatabaseInstance(){
        return FirebaseDatabase.getInstance();
    }

    private static DatabaseReference getDBReference(){
        return getFirebaseDatabaseInstance().getReference(smartRecUsersDBPath);
    }

    public static Task<Void> updateContactCircle(HashMap<String, Object> contactCircleMap){
        return getDBReference().child(Utils.getCurrentUserUID()).child(contactCirclePath).updateChildren(contactCircleMap);
    }

    public static DatabaseReference getUserContactCirclePath(){
        return getDBReference().child(Utils.getCurrentUserUID()).child(contactCirclePath);
    }

    public static DatabaseReference getReceivedRecordings(){
        return getDBReference().child(Utils.getCurrentUserUID()).child(smartRecRecordingsPath);
    }

    public static FirebaseUser isUserAvailable(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user;
    }

    public static void addRegistrationToken(String token, String userId) {
        Task<Void> task = getDBReference().child(userId).child("notificationTokens").child(token).setValue(true);
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("Token", "Successful");
            }
        });
    }
}
