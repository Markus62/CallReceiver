package de.emri.CallNotifier;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.text.*;
import android.util.*;
import android.view.View;
import android.widget.*;
import com.android.internal.telephony.*;

/**
 * Projekt: PhoneCallReceiver
 * Package: com.example.PhoneCallReceiver
 * Autor: Markus Embacher
 * Date: 14.02.13
 * Time: 10:35
 * Version: 1.0
 * Main Activity-Klasse
 */
public class MainActivity extends Activity {
  private static String DEBUG_TAG="CallReceiverMain";
  private static final String CALL_ACTION = "call_action";
  private static final String PREFS_NAME = "CallNotifierData";
  private static final String PREF_CHECK_INCOMING_CALLS = "check_incoming_calls";
  private static final String PREF_CHECK_BATTERY = "check_battery";
  private Context context;
  private com.android.internal.telephony.ITelephony telephonyService;
  private TextView tv;
  private ToggleButton toggleButtonIncomingCalls, toggleButtonCheckBattery;
  private static String outgoingNumber=null;
  private static boolean checkIncomingCalls =false, checkBattery=false;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(DEBUG_TAG, "MyActivity started");
    super.onCreate(savedInstanceState);
    restorePreferences();
    setContentView(R.layout.main);
    refreshUI();
    addListeners();
  }

  /*--------------------------------------------------------------------------------
   * Überschriebene Methoden der Klasse Activity.
   *--------------------------------------------------------------------------------/


  /*---------------------------------------------------------------
   * Methoden, die die UI mit gespeicherten Werten versorgen.
   *---------------------------------------------------------------/

  /**
   * Liest die in den Shared Preferences gespeicherten Daten.
   */
  private void restorePreferences(){
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

    if(settings!=null) {
      outgoingNumber = settings.getString("outgoing_number", "");
      checkIncomingCalls = settings.getBoolean(PREF_CHECK_INCOMING_CALLS, false);
      checkBattery=settings.getBoolean(PREF_CHECK_BATTERY, false);
    }
  }

  private void savePreferences() {
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString("outgoing_number", outgoingNumber);
    editor.putBoolean(PREF_CHECK_INCOMING_CALLS, checkIncomingCalls);
    editor.putBoolean(PREF_CHECK_BATTERY, checkBattery);
    editor.commit();
  }

  /**
   * Ruft alle Methoden auf, die die UI mit den richtigen Werten versorgt.
   */
  private void refreshUI(){
    setOutgoing();
    setActiveToggleButtons();
  }

  /**
   * Fuellt das Textfeld mit der Ziel-Telefonnummer.
   */
  private void setOutgoing() {
    TextView tv = (TextView)findViewById(R.id.id_text_targetnumber);
    if(outgoingNumber!=null){
      tv.setText(outgoingNumber);
    } else{
      outgoingNumber = tv.getText().toString();
    }
  }

  /**
   * Setzt den Status des ToggleButtons "SMS and Zielnummer senden".
   */
  private void setActiveToggleButtons()
  {
    ToggleButton tb = (ToggleButton)findViewById(R.id.toggleButton);
    ToggleButton tb1 = (ToggleButton)findViewById(R.id.toggleButton1);
    tb.setChecked(checkIncomingCalls);
    tb1.setChecked(checkBattery);
  }




  /**
   * Registriert den BroadcastReceiver, der die Anzeige bei eingehendem Ruf aktualisiert.
   */
  private void registerReceiver() {
    android.content.IntentFilter filter = new IntentFilter();
    filter.addAction(CALL_ACTION);
    Log.i(DEBUG_TAG, "registering Receiver in Activity.");
    registerReceiver(receiver, filter);
    Log.i(DEBUG_TAG, "Receiver registered.");
  }

  /**
   * Erzeugt den Listener für das Eingabefeld der Zieltelefonnummer.
   * Die statische Variable outgoingNumber wird aktualisiert.
   */
  private void addListeners(){
    EditText outgoing = (EditText)findViewById(R.id.id_text_targetnumber);
    outgoing.addTextChangedListener(new TextWatcher(){
      public void afterTextChanged(Editable s) {
        Log.i(DEBUG_TAG, "outgoing Number changed:" + s.toString());
        outgoingNumber = s.toString();
        savePreferences();
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after){}
      public void onTextChanged(CharSequence s, int start, int before, int count){}
    });

    ToggleButton tb = (ToggleButton)findViewById(R.id.toggleButton);
    tb.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        checkIncomingCalls =((ToggleButton)view).isChecked();
        savePreferences();
      }
    });

    ToggleButton tb1 = (ToggleButton)findViewById(R.id.toggleButton1);
    tb1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        checkBattery =((ToggleButton)view).isChecked();
        savePreferences();
      }
    });
  }


  /**
   * nimmt Daten vom CallReceiver und SendSMS entgegen.
   * nicht verwendet.
   */
  private BroadcastReceiver receiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
     Log.i(DEBUG_TAG, "Activity receives Broadcast.");
     TextView tv=(TextView)findViewById(R.id.id_caller);
     if(tv != null) {
       Log.i(DEBUG_TAG, "Number: "+intent.getStringExtra("incoming_number"));
       tv.setText(intent.getStringExtra("incoming_number"));
     } else {
       Log.i(DEBUG_TAG, "UI not found.");
     }
    }
  };


  /*------------------------------------------------------------------
   * public static Methoden.
   *------------------------------------------------------------------/

  /**
   * Liefert die Ziel-Telefonnummer
   * @return Ziel-Telefonnummer
   */
  public static String getOutgoingNumber() {
    return outgoingNumber;
  }

  /**
   * Liefert den Status des ToggleButtons Eingehende Anrufe melden.
   * @return true, falls SMS bei eingehenden Anrufen gesendet werden soll.
   */
  public static boolean isCheckIncomingCalls() {
    return checkIncomingCalls;
  }

  /**
   * Liefert den Status des ToggleButtons Akkustatus melden.
   * @return true, falls SMS bei Änderung Batterie-Status gesendet werden soll.
   */
  public static boolean isCheckBattery() {
    return checkBattery;
  }

  /**
   * Liefert den den String für den BroadCastReceiver-Intent.
   * @return Intent-String
   */
  public static String getCallAction() {
    return CALL_ACTION;
  }


}
