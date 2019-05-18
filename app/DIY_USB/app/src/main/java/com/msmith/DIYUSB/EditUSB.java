package com.msmith.DIYUSB;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EditUSB extends AppCompatActivity {

    String name = "";
    String password = "";
    String pin = "";
    String usbKey = "";
    Boolean justPasswordHitEdit = false;
    Boolean justPinHitEdit = false;
    private Context context = this;

    BluetoothSocket myBluetoothSocket;
    BluetoothDevice myBluetoothDevice;
    OutputStream myOutputStream;
    BufferedWriter bw;
    InputStream myInputStream;
    BufferedReader br;

    Thread workerThread;

    List<USB> USBList = new ArrayList<USB>();
    Set<BluetoothDevice> pairedDevices;

    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_usb);

        //Gets the USB list
        Boolean moreUSBs = true;
        int count = 0;
        while(moreUSBs){
            String title = "USB " + count;
            USB temp = (USB)getIntent().getSerializableExtra(title);
            count ++;
            if(temp == null){
                moreUSBs = false;
            }
            else{
                USBList.add(temp);
            }

        }

        //Sets the bluetooth objects, should have been received from the other activity
        try {
            myBluetoothDevice = (BluetoothDevice) getIntent().getParcelableExtra("Bluetooth Device");
        }
        catch(Exception ex){
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }

        //Sets up the password switch
        final Switch passwordSwitch = (Switch) findViewById(R.id.switchPassword);
        //Sets up the password button
        final Button btnPasswordEdit = (Button) findViewById(R.id.btnPasswordEdit);

        //Sets up the pin switch
        final Switch pinSwitch = (Switch) findViewById(R.id.switchPin);
        //Sets up the pin button
        final Button btnPinEdit = (Button) findViewById(R.id.btnPinEdit);

        //Sets up the confirm button on click method
        Button btnConfirm = (Button)findViewById(R.id.btnConfirm);
        //Sets up the sync usb button on click method
        Button btnSyncUSB = (Button)findViewById(R.id.btnSyncUSB);

        //On password switch value chang
        passwordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the switch it turned on
                if(passwordSwitch.isChecked()) {
                    //If the edit button was just clicked, don't run the prompt
                    if(justPasswordHitEdit) {
                        justPasswordHitEdit = false;
                    }
                    else{
                        //Clicks the password button
                        btnPasswordEdit.callOnClick();
                    }
                }
                //If the switch was turned off
                else{
                    //Erase the password
                    password = "";
                }
            }
        });

        //On password edit button click event method
        btnPasswordEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prompt edit password
                promptPassword();
            }
        });

        //On pin switch change event method
        pinSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the switch it turned on
                if(pinSwitch.isChecked()) {
                    //If the edit button was just clicked, don't run the prompt
                    if(justPinHitEdit) {
                        justPinHitEdit = false;
                    }
                    else{
                        //Clicks the password button
                        btnPinEdit.callOnClick();
                    }
                }
                //If the switch is switched off
                else{
                    //Erases the pin
                    pin = "";
                }
            }
        });

        //On pin button change event
        btnPinEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prompt edit pin
                promptPin();
            }
        });


        //On confirm button click event method
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //USBs must have a name, a password AND OR a pin, and must be synced
                //Pins must be four digits
                //Passwords must be >= 7 digits

                EditText txtName = (EditText)findViewById(R.id.txtName);
                name = txtName.getText().toString();
                //Must have a valid name
                if(name.equals("")){
                    Toast.makeText(getApplicationContext(), "You must set a name for the USB", Toast.LENGTH_SHORT).show();
                }
                else {
                    Boolean nameUsed = false;

                    for(USB u: USBList){
                        if(u.name.equals(name)){
                            nameUsed = true;
                            break;
                        }
                    }

                    if(nameUsed){
                        Toast.makeText(getApplicationContext(), "You must use a unique name for your USBs", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //Must have a valid password or pin
                        if (password.equals("") && pin.equals("")) {
                            Toast.makeText(getApplicationContext(), "You must set a 4 digit pin or a password", Toast.LENGTH_SHORT).show();
                        } else {
                            if(!password.equals("") && password.length() < 7){
                                Toast.makeText(getApplicationContext(), "\"You must set a password with more than 7 characters", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                if(!pin.equals("") && pin.length() != 4){
                                    Toast.makeText(getApplicationContext(), "You must set a pin with 4 characters", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    if (usbKey.equals("")) {
                                        Toast.makeText(getApplicationContext(), "You must sync your USB", Toast.LENGTH_SHORT).show();
                                    } else {
                                        System.out.println("l " + name + " l " + password + " l " + pin + " l");
                                        openMainActivity();
                                    }
                                }
                            }
                        }
                    }

                }
            }
        });

        //The on click event method for the sync USB button
        btnSyncUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If the usbkey has not been set
                if(usbKey.equals("")) {
                    workerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Attempting to communicate with the Computer");
                            try {
                                myBluetoothSocket = myBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                                myBluetoothSocket.connect();
                                myInputStream = myBluetoothSocket.getInputStream();
                                myOutputStream = myBluetoothSocket.getOutputStream();

                                System.out.println("Connected");

                                br = new BufferedReader(new InputStreamReader(myInputStream));
                                bw = new BufferedWriter(new OutputStreamWriter(myOutputStream));

                                //Sends a 1, showing that the connection has been made
                                myOutputStream.write(49);
                                System.out.println("Sent char");

                                //While the name hasn't been sent
                                while (usbKey.equals("")) {
                                    //Sets the key of the USB
                                    usbKey = br.readLine();
                                    System.out.println("USB key is: " + usbKey);
                                }

                                //Tells the user that the USB has successfully synced
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "USB Synced", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (IOException ex) {
                                //The USB was unable to sync for some reason
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Unable to sync USB. Try Again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    });
                    workerThread.start();
                }
                //If the usb key has already been set
                else{
                    Toast.makeText(getApplicationContext(), "The USB is already synced", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    //Opens the main activity and gives a USB object back to the main activity
    public void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);

        USBList.add(new USB(name, password, pin, usbKey));
        int count = 0;
        for(USB u : USBList){
            String title = "USB " + count;
            intent.putExtra(title, u);
            count ++;
        }

        //Sends the bluetooth device to the main activity
        intent.putExtra("Bluetooth Device", myBluetoothDevice);
        try{
            myBluetoothSocket.close();
        }
        catch(IOException ex){
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        startActivity(intent);
    }

    //Prompts the user to enter a password
    public void promptPassword(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
        builder.setTitle("Set Password");

        final EditText result = view.findViewById(R.id.txtPassword);

        //Clicked Set
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){

                password = result.getText().toString();

                //Sets up the password switch
                final Switch passwordSwitch = (Switch) findViewById(R.id.switchPassword);
                //Changes the state of the switch
                if(password.equals("")){
                    justPasswordHitEdit = false;
                    passwordSwitch.setChecked(false);
                }
                else{
                    justPasswordHitEdit = true;
                    passwordSwitch.setChecked(true);
                }

            }
        });

        //Clicked Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Sets up the password switch
                final Switch passwordSwitch = (Switch) findViewById(R.id.switchPassword);
                if(password.equals("")){
                    passwordSwitch.setChecked(false);
                }
                else{
                    passwordSwitch.setChecked(true);
                }
            }
        });

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();
    }

    //Prompts an input alert
    public void promptPin(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
        builder.setTitle("Set Pin");


        final EditText result = view.findViewById(R.id.txtPassword);
        result.setInputType(InputType.TYPE_CLASS_NUMBER);
        result.setTransformationMethod(PasswordTransformationMethod.getInstance());


        //Clicked Set
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){

                pin = result.getText().toString();

                //Sets up the password switch
                final Switch pinSwitch = (Switch) findViewById(R.id.switchPin);
                //Changes the state of the switch
                if(pin.equals("")){
                    justPinHitEdit = false;
                    pinSwitch.setChecked(false);
                }
                else{
                    justPinHitEdit = true;
                    pinSwitch.setChecked(true);
                }

            }
        });

        //Clicked Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Sets up the password switch
                final Switch pinSwitch = (Switch) findViewById(R.id.switchPin);
                if(pin.equals("")) {
                    pinSwitch.setChecked(false);
                }
                else{
                    pinSwitch.setChecked(true);
                }
            }
        });

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
