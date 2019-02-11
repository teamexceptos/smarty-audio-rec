package app.com.smartrec.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import app.com.smartrec.Models.recAudioModel;

public class SmartRecBackend {
    private static final String TAG = SmartRecBackend.class.getSimpleName();

    Context context;
    private MediaRecorder mRecorder;
    private String mFileName = null;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private MediaPlayer mediaPlayer;

    SharedPrefManager sharedPrefManager;

    public SmartRecBackend(Context setcontext){
        this.context = setcontext;
        mAuth = FirebaseAuth.getInstance();
        this.sharedPrefManager = new SharedPrefManager(setcontext);
    }

    public void startRecording(){

        long time = new Date().getTime();
        String mFileName = "smart-rec" + time + "." + "3gp";

        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, "SmartRec" + "/" + mFileName);
        file.getParentFile().mkdirs();

        mFileName = file.toString();

        mediaPlayer = new MediaPlayer();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //Get Source
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //Set Format
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //Set Encoder
        mRecorder.setOutputFile(mFileName);

        setRecFile(file);
        String mEncrypted = "encrypted" + mFileName;
        setrecfilename(mEncrypted);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException ise) {
            // make something ...
        } catch (IOException ioe) {
            Log.v("TEST", "Could not prepare Recorder: ");
        }
    }

    public void stopRecording(String Uid){

        try {
            mRecorder.stop();

            if(Uid.equals(Utils.noUidString)){
                Toast.makeText(context, "Recording saved!", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Sign In or Log in to upload recording", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "Recording saved!", Toast.LENGTH_SHORT).show();
                uploadAudio(getRecFile());
            }

        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    private void uploadAudio(final File recfilename){

        final long time = new Date().getTime();

        //final String mFileStoragePath = "ensmart-rec" + time;
        final String mFileStoragePath = "smart-rec" + time;

        StorageReference mRecStorage = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();

        Utils.setDialog(context, true);

        final StorageReference filepath = mRecStorage.child("Recs").child(mFileStoragePath); //Create Firebase Reference
        final Uri uri = Uri.fromFile(new File(String.valueOf(recfilename)));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Utils.setDialog(context, false);
                recAudioModel recAudioModel = new recAudioModel();

                String postkey = firebaseDatabase.getReference().child("Recs").push().getKey();
                String uploadDBpath = taskSnapshot.getDownloadUrl().toString();

                recAudioModel.setFullname(sharedPrefManager.getName());
                recAudioModel.setUid(sharedPrefManager.getUserUiD());
                recAudioModel.setPhonenumber(sharedPrefManager.getPhonenumber());
                recAudioModel.setCreatedDate(time);
                recAudioModel.setRecUploadpath(uploadDBpath);

                firebaseDatabase.getReference().child("Recs").child(postkey).setValue(recAudioModel);
                SendingtoContactCircles(postkey, sharedPrefManager.getUserUiD(), uploadDBpath, time);

                Toast.makeText(context, "Done uploading", Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Utils.setDialog(context, true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.setDialog(context, false);
                Toast.makeText(context, "Sorry, Couldn't upload to cloud. Try to login in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void SendingtoContactCircles(final String postKey, final String Uid, final String Recpath, final long sendDate){
        FirebaseUser user = mAuth.getCurrentUser();
        firebaseDatabase.getReference("smartrec-users").child(user.getUid()).child("ContactCircle").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String Contacts = postSnapshot.getValue().toString();
                    Log.i("Contacts", Contacts);
                    getUsertoSendRec(postKey, Uid, Recpath, sendDate, Contacts);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUsertoSendRec(final String postKey, final String Uid, final String Recpath, final long sendDate, String phonenumber) {
        DatabaseReference databaseReference = firebaseDatabase.getReference("smartrec-users");
        databaseReference.orderByChild("phonenumber").equalTo(phonenumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    String ContactsUid = childDataSnapshot.child("uid").getValue().toString();
                    Log.i("ContactsUid", ContactsUid);

                    HashMap<String, Object> RecivedEnRec = new HashMap<>();
                    RecivedEnRec.put("senderUid", Uid);
                    RecivedEnRec.put("SentRec", Recpath);
                    RecivedEnRec.put("RecpathKey", postKey);
                    RecivedEnRec.put("sentDate", sendDate);
                    RecivedEnRec.put("senderLongitude", sharedPrefManager.getLon());
                    RecivedEnRec.put("senderLatitude", sharedPrefManager.getLat());

                    String SendKey = firebaseDatabase.getReference("smartrec-users").child(ContactsUid).push().getKey();
                    firebaseDatabase.getReference("smartrec-users").child(ContactsUid).child("recievedEnRec").child(SendKey).updateChildren(RecivedEnRec);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void incrementWatchersCount(String userId) {
        DatabaseReference postRef = firebaseDatabase.getReference("smartrec-users/" + userId + "/recievedRecWatcher");
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.i(TAG, "Updating Watchers count transaction is completed.");
            }
        });
    }

    public void StreamRec(String recUrl){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try{
            mediaPlayer.setDataSource(recUrl);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepare();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private byte[] readFile(File file) {

        byte[] contents = null;
        int size = (int) file.length();
        contents = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            try {
                buf.read(contents);
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private byte[] getAudioFile(File file) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = fis.read(b)) != -1;) {
            bos.write(b, 0, readNum);
        }
        return bos.toByteArray();
    }

    private void playMp3(byte[] mp3SoundByteArray) {

        try {
            // create temp file that will hold byte array
            File tempFile = File.createTempFile("recTempRecFile", "3gp", context.getCacheDir());
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(mp3SoundByteArray);
            fos.close();
            MediaPlayer mediaPlayer = new MediaPlayer();
            FileInputStream fis = new FileInputStream(tempFile);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    File recfile;
    String recfilename;

    public void setRecFile(File file){
        this.recfile = file;
    }

    public File getRecFile(){
        return recfile;
    }

    public void setrecfilename(String file){
        this.recfilename = file;
    }

    public String getrecfilename(){
        return recfilename;
    }
}
