package de.emri.CallNotifier;

import android.content.*;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Projekt: PhoneCallReceiver
 * Package: com.example.PhoneCallReceiver
 * Autor: Markus Embacher
 * Date: 14.02.13
 * Time: 10:35
 * Klasse, die ankommende Calls behandelt.
 */
public class CallReceiver extends BroadcastReceiver {

  public static final String CUSTOM_INTENT_SEND_SMS = "de.emri.action_sendsms";
  private static String DEBUG_TAG="CallReceiver";
  private static final String PREFS_NAME = "CallNotifierData";
  private static final String PREF_CHECK_INCOMING_CALLS = "check_incoming_calls";
  private static final String PREF_CHECK_BATTERY = "check_battery";
  private static boolean checkIncomingCalls=false;
  private com.android.internal.telephony.ITelephony telephonyService=null;


  /**
   * Receiver.
   * @param context aktueller Context
   * @param intent Intent des Broadcasts
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    Bundle extras = intent.getExtras();
    String incomingNumber;

    Log.i(DEBUG_TAG, "Incoming Call signaled");
    Log.i(DEBUG_TAG, "Action:"+intent.getAction());
    restorePreferences(context);
    if(checkIncomingCalls){
      if (extras != null) {
        String state = extras.getString(TelephonyManager.EXTRA_STATE);
        Log.i(DEBUG_TAG, "State: "+state);
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
          Log.i(DEBUG_TAG, "Ringing...");
          if(telephonyService==null) {
            getTeleService(context);
          }
          Intent i = new Intent();
          i.setAction(CUSTOM_INTENT_SEND_SMS);
          incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
          i.putExtra("incoming_number", incomingNumber);
          Log.i(DEBUG_TAG, "Incoming number: "+ incomingNumber);
          context.sendBroadcast(i);
          try {
            telephonyService.endCall();
          } catch (RemoteException e) {
            Log.e(DEBUG_TAG, Log.getStackTraceString(e));
          }
        }
      }
    }else{
      Log.i(DEBUG_TAG, "CallReceiver: SMS-Versand inaktiv.");
    }
  }


  /**
   * Liest die in den Shared Preferences gespeicherten Daten.
   */
  private void restorePreferences(Context context){
    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

    if(settings!=null) {
      checkIncomingCalls = settings.getBoolean(PREF_CHECK_INCOMING_CALLS, false);
    } else {
      Log.i(DEBUG_TAG, "no Settings.");
    }
  }


  /**
   * instanziiert den Telefon - Service.
   * @param context aktueller Context
   */
  public void getTeleService(Context context)  {
    Log.i(DEBUG_TAG, "Trying to get SystemService");
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if(tm!=null) {
      Log.i(DEBUG_TAG, "Telephone Manager found.");
    } else {
      Log.i(DEBUG_TAG, "Telephone Manager not found.");
    }
    com.android.internal.telephony.ITelephony ts = null;

    try {
      Class c = Class.forName(tm.getClass().getName());
      if(c!=null) {
        Method m = c.getDeclaredMethod("getITelephony");
        if(m!=null) {
          m.setAccessible(true);
          telephonyService = (ITelephony) m.invoke(tm);
          Log.i(DEBUG_TAG, "TeleService invoked..");
        } else {
          Log.e(DEBUG_TAG, "ITelefphony not found.");
        }
      } else{
        Log.e(DEBUG_TAG, "TelephonyManager not available.");
      }
    } catch (Exception e) {
      Log.e(DEBUG_TAG, Log.getStackTraceString(e));
    }

  }

}
