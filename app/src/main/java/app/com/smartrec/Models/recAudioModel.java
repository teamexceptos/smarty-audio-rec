package app.com.smartrec.Models;

import android.annotation.SuppressLint;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by ${cosmic} on 8/7/18.
 */
public class recAudioModel {

    private String uid;
    private String phonenumber;
    private String fullname;
    private String recUploadpath;
    private long createdDate;
    private String Long;
    private String Lat;
    public static String firebaseDBDate = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRecUploadpath() {
        return recUploadpath;
    }

    public void setRecUploadpath(String recUploadpath) {
        this.recUploadpath = recUploadpath;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getLong() {
        return Long;
    }

    public void setLong(String aLong) {
        Long = aLong;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("uid", uid);
        result.put("fullname", fullname);
        result.put("phonenumber", phonenumber);
        result.put("recUploadpath", recUploadpath);
        result.put("createdDateText", getFirebaseDateFormat().format(new Date(createdDate)));

        return result;
    }

    public static SimpleDateFormat getFirebaseDateFormat() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat cbDateFormat = new SimpleDateFormat(firebaseDBDate);
        cbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cbDateFormat;
    }

    private static final recAudioModel recAudioModel = new recAudioModel();

    public static recAudioModel getIntance(){
        return recAudioModel;
    }
}
