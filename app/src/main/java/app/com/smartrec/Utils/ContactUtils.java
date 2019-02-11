package app.com.smartrec.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by Big-Nosed Developer on the Edge of Infinity.
 */
public class ContactUtils {

    public static String retrieveContactNumber(Context context, Uri uriContact){

        String contactNumber = null;
        String contactID = null;
        // getting contacts ID
        Cursor cursorID = context.getContentResolver().query(uriContact, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE +
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK +
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME +
                        ContactsContract.CommonDataKinds.Phone.TYPE_PAGER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK +
                        ContactsContract.CommonDataKinds.Phone.TYPE_CAR +
                        ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN +
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX +
                        ContactsContract.CommonDataKinds.Phone.TYPE_RADIO +
                        ContactsContract.CommonDataKinds.Phone.TYPE_TELEX +
                        ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD +
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE +
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MMS,

                new String[]{contactID},
                null);

        while (cursorPhone.moveToNext()){
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));

        }

        cursorPhone.close();

        return contactNumber;

    }

    public static String contactNumUtil(String contact){
        String newContact = "";
        if(!contact.contains("+234")){
            char[] contactchar = contact.toCharArray();
            contactchar[0] = ' ';
            newContact = "+234" + String.valueOf(contactchar).replace(" ", "");
        }
        else{
            return  contact;
        }
        return newContact;
    }


    public static String retrieveContactName(Context context, Uri uriContact){
        String contactName = null;
        // querying contact data store
        Cursor cursor = context.getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        return contactName;
    }

}
