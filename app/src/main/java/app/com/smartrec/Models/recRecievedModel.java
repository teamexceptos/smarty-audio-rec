package app.com.smartrec.Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ${cosmic} on 8/13/18.
 */
public class recRecievedModel {

    private String SentRec;
    private String senderUid;
    private Long sendDate;
    private String senderPhone;
    private String senderLongitude;
    private String senderLatitude;
    private String RecpathKey;

    public String getSentRec() {
        return SentRec;
    }

    public void setSentRec(String sentRec) {
        this.SentRec = sentRec;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public Long getSendDate() {
        return sendDate;
    }

    public void setSendDate(Long sendDate) {
        this.sendDate = sendDate;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderLongitude() {
        return senderLongitude;
    }

    public void setSenderLongitude(String senderLongitude) {
        this.senderLongitude = senderLongitude;
    }

    public String getSenderLatitude() {
        return senderLatitude;
    }

    public void setSenderLatitude(String senderLatitude) {
        this.senderLatitude = senderLatitude;
    }

    public String getRecpathKey() {
        return RecpathKey;
    }

    public void setRecpathKey(String recKey) {
        this.RecpathKey = recKey;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("SentRec", SentRec);
        result.put("senderUid", senderUid);
        result.put("senderPhone", senderPhone);
        result.put("senderLong", senderLongitude);
        result.put("senderLat", senderLatitude);
        result.put("sendDate", sendDate);

        return result;
    }
}
