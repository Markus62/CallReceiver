package de.emri.CallNotifier;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Projekt: PhoneCallReceiver
 * Package: com.example.PhoneCallReceiver
 * Autor: Markus Embacher
 * Date: 14.02.13
 * Time: 10:40
 * Klasse zum versenden von SMS.
 */
public class SendSms extends BroadcastReceiver {

  private static String DEBUG_TAG="CallReceiverSendSMS";
  private static final String PREFS_NAME = "CallNotifierData";
  private static final String PREF_CHECK_INCOMING_CALLS = "check_incoming_calls";
  private static final String PREF_CHECK_BATTERY = "check_battery";
  private static String outgoingNumber=null;
  private static boolean checkBattery=false;

  /**
   * Sendet eine SMS, wenn Broadcast-Intent stimmt.
   * @param context aktueller Context
   * @param intent  Intent
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(DEBUG_TAG, "SendSms started");
    restorePreferences(context);
    SmsManager manager = SmsManager.getDefault();
    if(outgoingNumber!=null) {
      if(intent.getAction().equals(CallReceiver.CUSTOM_INTENT_SEND_SMS)) {
        Log.i(DEBUG_TAG, "Custom intent: Send SMS.");
        manager.sendTextMessage(outgoingNumber, null, getSMSMsg(context, intent), null, null);
      }else if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW) && checkBattery){
        Log.i(DEBUG_TAG, "Battery_LOW signaled.");
        manager.sendTextMessage(outgoingNumber, null, "Akku von Sony Ericsson muss geladen werden.", null, null);
      }else if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY) && checkBattery){
        Log.i(DEBUG_TAG, "Battery_OK signaled.");
        manager.sendTextMessage(outgoingNumber, null, "Akku von Sony Ericsson ist wieder OK.", null, null);
      }
    }
  }

  /**
   * Liest die in den Shared Preferences gespeicherten Daten.
   */
  private void restorePreferences(Context context){
    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

    if(settings!=null) {
      outgoingNumber = settings.getString("outgoing_number", "");
      checkBattery=settings.getBoolean(PREF_CHECK_BATTERY, false);
      Log.i(DEBUG_TAG, "Outgoing number: " + outgoingNumber);
    } else {
      Log.i(DEBUG_TAG, "no settings!");
    }
  }

  /**
   * Generiert den Text der SMS.
   * @param context aktueller Kontext
   * @param intent Intent
   * @return
   */
  private String getSMSMsg(Context context, Intent intent) {
    String number=intent.getStringExtra("incoming_number");
    String msg="Anruf von ";
    String name = getContactDisplayNameByNumber(context, number);
    if(name!=null){
      msg+=name+"\n\nNummer: "+number ;
    } else {
      msg+=number;
    }
    return msg;
  }

  /**
   * Ermittelt den Namen des Kontaktes anhand der Telefonnummer.
   * @param context aktueller Kontext
   * @param number Telefonnummer
   * @return Name des Kontakts, sofern vorhanden, sonst null
   */
  public String getContactDisplayNameByNumber(Context context, String number) {
    String CONTACT_COLUMNS=ContactsContract.PhoneLookup.DISPLAY_NAME;
    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
    String name = null;
    String[] columns=new String[]{CONTACT_COLUMNS};
    ContentResolver contentResolver;
    contentResolver = context.getContentResolver();
    Cursor contactLookup = contentResolver.query(uri, columns, null, null, null);

    try {
      if (contactLookup != null && contactLookup.getCount() > 0) {
        contactLookup.moveToNext();
        name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
      }
    } finally {
      if (contactLookup != null) {
        contactLookup.close();
      }
    }

    return name;
  }
}
