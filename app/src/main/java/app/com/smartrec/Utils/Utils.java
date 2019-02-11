package app.com.smartrec.Utils;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import app.com.smartrec.R;

/**
 * Created by Big-Nosed Developer on the Edge of Infinity.
 */
public class Utils {

    private static SharedPreferences sharedPreferences;
    private static String recordSharedPreferencesPath = "RECORDINGS";

    //The various recording states..
    public static String isRecordingKeyState = "IS_RECORDING";
    public static String isNotRecordingKeyState = "IS_NOT_RECORDING";
    public static String autoRecordEnabled = "AUTO_RECORD_ENABLED";
    public static String autoRecordDisabled = "AUTO_RECORD_DISABLED";

    //The various keys
    private static String recordingStateKey = "RECORDING_KEY";
    private static String autoRecordingKey = "AUTO_RECORDING_KEY";

    //DialogFragment Tag
    public static String dialogFragmentTag = "AUTO_RECORD_DIALOG_TAG";

    //No Uid here
    public static String noUidString = "NO_UID_HERE";

    public static final int contactPickerRequestCode = 346;

    //Recording variables
    public static final int recordSampleRate = 16000;
    public static final int recLimitType = 60000;

    public static final int permissionRequestCode = 223;

    public static FirebaseUser getCurrentUser(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return  auth.getCurrentUser();
    }

    public static String getCurrentUserUID(){
        if(getCurrentUser() != null){
            return  getCurrentUser().getUid();
        }else {
            return noUidString;
        }
    }

    private static SharedPreferences getRecordingSharedPreferences(Context context){
        sharedPreferences = context.getSharedPreferences(recordSharedPreferencesPath, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public static void putAutoRecordingState(Context context, String autoRecordingState){
        SharedPreferences.Editor editor = getRecordingSharedPreferences(context).edit();
        editor.putString(autoRecordingKey, autoRecordingState);

        //Mandatory main thread commit!
        editor.commit();
    }

    public static String getAutoRecordingState(Context context){
        return getRecordingSharedPreferences(context).getString(autoRecordingKey, "");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void showSnackbar(View view, final int mainTextStringId, final int actionStringId,
                                    View.OnClickListener listener) {
        Snackbar.make(view.findViewById(android.R.id.content), view.getContext().getString(mainTextStringId), Snackbar.LENGTH_LONG)
                .setAction(view.getContext().getString(actionStringId), listener)
                .setActionTextColor(view.getContext().getColor(R.color.colorAccent))
                .show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setDialog(Context context, boolean show){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //View view = getLayoutInflater().inflate(R.layout.progress);
        builder.setView(R.layout.progresslayout);
        Dialog dialog = builder.create();

        if (show){
            dialog.show();

        } else
            dialog.dismiss();
    }
}
