package app.com.smartrec.Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ${cosmic} on 7/2/18.
 */
public class recUserModel {

    public String fullname;
    public String email;
    public String phonenumber;
    public String uid;
    public String longlat;
    private String registrationToken;

  public recUserModel(){}

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLonglat() {
        return longlat;
    }

    public void setLonglat(String longlat) {
        this.longlat = longlat;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("uid", uid);
        result.put("fullname", fullname);
        result.put("email", email);
        result.put("phonenumber", phonenumber);
        result.put("longlat", longlat);
        result.put("registrationToken", registrationToken);

        return result;
    }
}
