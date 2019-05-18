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
    USB tempUSB;


    BluetoothAdapter myBluetoothAdapter;
    BluetoothSocket myBluetoothSocket;
    BluetoothDevice myBluetoothDevice;
    OutputStream myOutputStream;
    BufferedWriter bw;
    InputStream myInputStream;
    BufferedReader br;

    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    List<USB> USBList = new ArrayList<USB>();
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_usb);



        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

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

        myBluetoothDevice = (BluetoothDevice)getIntent().getParcelableExtra("Bluetooth Device");

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
                //Erases the password
                else{
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
                //Erases the password
                else{
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
            EditText txtName = (EditText)findViewById(R.id.txtName);
            name = txtName.getText().toString();
            //Must have a valid name
            if(name.equals("")){
                errorPrompt("You must set a name for the USB");
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
                    errorPrompt("You must use a unique name for your USB's");
                }
                else {
                    //Must have a valid password or pin
                    if (password.equals("") && pin.equals("")) {
                        errorPrompt("You must set a 4 digit pin or a password");
                    } else {
                        if(usbKey.equals("")){
                            errorPrompt("You must sync your USB");
                        }
                        else {
                            System.out.println("l " + name + " l " + password + " l " + pin + " l");
                            openMainActivity("Save");
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
                //findBT();
                openBT();

            }
        });
    }

    private BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            for(BluetoothDevice device : bluetoothProfile.getConnectedDevices()){
                //GETS THE CONNECTED DEVICE
                //myBluetoothDevice = device;
                //System.out.println("My Bluetooth Device Name: " + myBluetoothDevice.getName());
            }
        }

        @Override
        public void onServiceDisconnected(int i) {

        }
    };


    public void openBT(){
        if(myBluetoothDevice != null) {
            System.out.println("Bluetooth Device is: " + myBluetoothDevice.getName());
            System.out.println("Bluetooth Address is: " + myBluetoothDevice.getAddress());
        }
        else{
            System.out.println("My Bluetooth device is null");
        }

        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Attempting to communicate with the Computer");
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

                try {
                    myBluetoothSocket = myBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                } catch (IOException ex) {
                    System.out.println("Could not create a socket");
                }

                try {
                    myBluetoothSocket.connect();
                    System.out.println("Connected");
                    myOutputStream = myBluetoothSocket.getOutputStream();
                    myInputStream = myBluetoothSocket.getInputStream();

                    br = new BufferedReader(new InputStreamReader(myInputStream));
                    bw = new BufferedWriter(new OutputStreamWriter(myOutputStream));

                    //Sends that the connection has been made
                    myOutputStream.write(49);
                    System.out.println("Sent char");

                    //WHile the name hasn't been sent
                    while (usbKey.equals("")) {
                        usbKey = br.readLine();
                        System.out.println("USB key is: " + usbKey);
                        //bw.write(49);
                    }

                    //Toast.makeText(getApplicationContext(), "USB Synced", Toast.LENGTH_SHORT).show();

                } catch (IOException ex) {

                }

            }
        });
        workerThread.start();



        //beginListenForData();

        //System.out.println("Bluetooth Opened");


    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                //errorPrompt("Device Found");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                //errorPrompt("Connected");
                Toast.makeText(getApplicationContext(), "BlueTooth Device Connected", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                errorPrompt("Searching");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                //errorPrompt("About to disconnect");
                Toast.makeText(getApplicationContext(), "BlueTooth Device is about to disconnect", Toast.LENGTH_SHORT).show();

                //SEND ENCRYPT SIGNAL

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                //errorPrompt("Disconnected");
                Toast.makeText(getApplicationContext(), "BlueTooth Device Disconnected", Toast.LENGTH_SHORT).show();

            }
        }
    };


    //Opens the main activity and gives a USB object back to the main activity
    public void openMainActivity(String state){
        Intent intent = new Intent(this, MainActivity.class);

        USBList.add(new USB(name, password, pin, usbKey));
        int count = 0;
        for(USB u : USBList){
            String title = "USB " + count;
            intent.putExtra(title, u);
            count ++;
        }

        intent.putExtra("Bluetooth Device", myBluetoothDevice);
        startActivity(intent);
    }

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

        //
        builder.setView(view);
        //
        AlertDialog alert = builder.create();
        alert.show();
    }

    //Prompts an input alert
    public void promptPin(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
        builder.setTitle("Set Pin");


        final EditText result = view.findViewById(R.id.txtPassword);

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

    public void errorPrompt(String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
