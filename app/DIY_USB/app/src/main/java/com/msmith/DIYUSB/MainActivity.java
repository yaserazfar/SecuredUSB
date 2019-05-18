package com.msmith.DIYUSB;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BluetoothAdapter myBluetoothAdapter;
    BluetoothSocket myBluetoothSocket;
    BluetoothDevice myBluetoothDevice;
    OutputStream myOutputStream;
    BufferedWriter bw;
    InputStream myInputStream;
    BufferedReader br;

    String usbToDecrypt = "";

    Set<BluetoothDevice> pairedDevices;

    List<USB> USBList = new ArrayList<USB>();
    private Context context = this;
    String filename = "USBData";

    USB theUSBToDecrypt;
    Boolean hasPassword = true;
    Boolean hasPin = true;

    public void update(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(context.getFilesDir(), filename);

                try{
                    OutputStreamWriter outputStream = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));

                    for(USB u : USBList){
                        outputStream.write(u.toString());
                    }
                    outputStream.close();
                }
                catch(Exception e){
                    System.out.println("File write failed: " + e.toString());
                }

            }
        }).start();
    }

    public void readData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                try {
                    myBluetoothSocket = myBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                } catch (IOException ex) {
                    System.out.println("Could not create a socket");
                }

                try {
                    myBluetoothSocket.connect();
                    System.out.println("Connected");
                    myInputStream = myBluetoothSocket.getInputStream();

                    br = new BufferedReader(new InputStreamReader(myInputStream));
                    //bw = new BufferedWriter(new OutputStreamWriter(myOutputStream));

                    Boolean read = true;
                    while(read){
                        usbToDecrypt = br.readLine();
                        System.out.println("USBkey to decrypt is: " + usbToDecrypt);
                        read = !decryptAUSB();
                    }

                    /*
                    myOutputStream = myBluetoothSocket.getOutputStream();
                    //myInputStream = myBluetoothSocket.getInputStream();

                    myOutputStream.write(50);
                    System.out.println("Sent Decrypt Signal");
                    */


                }
                catch(IOException ex){

                }
            }
        }).start();
    }


    public Boolean decryptAUSB(){
        Boolean found = false;
        for(USB u : USBList){
            //If the usb to decrypt is found
            if(u.usbKey.equals(usbToDecrypt)){
                found = true;
                theUSBToDecrypt = u;

                System.out.println("USB to decrypt is: " + u.name);
                System.out.println("Password: " + u.password);
                System.out.println("Pin:" + u.pin);

                if(u.password.equals(" ")){
                    hasPassword = false;
                }

                if(u.pin.equals(" ")){
                    hasPin = false;
                }

                System.out.println("Password: " + hasPassword+", " + "Pin: " + hasPin);

                if(hasPassword) {
                    promptPassword();
                }

                if(hasPin){
                    promptPin();
                }

            }
        }

        if(!found){
            System.out.println("USB not found");
        }
        return found;
    }

    public void promptPassword(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
                builder.setTitle("Please enter password for: " + theUSBToDecrypt.name);

                final EditText result = view.findViewById(R.id.txtPassword);

                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(theUSBToDecrypt.password.equals(result.getText().toString())){
                            hasPassword = false;
                            if(hasPin){
                                promptPin();
                            }
                            else{
                                sendDecryptSignal();
                            }
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setView(view);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void promptPin(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
                builder.setTitle("Please enter pin for: " + theUSBToDecrypt.name);

                final EditText result = view.findViewById(R.id.txtPassword);

                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(theUSBToDecrypt.pin.equals(result.getText().toString())){
                            hasPin = false;
                            if(hasPassword){
                                promptPassword();
                            }
                            else{
                                sendDecryptSignal();
                            }
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setView(view);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    Handler passwordHandler;

    Handler pinHandler;

    public void sendDecryptSignal() {
        /*
        System.out.println("Attempting to send decrypt signal");
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        try {
            myBluetoothSocket = myBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException ex) {
            System.out.println("Could not create a socket");
        }
        */
        try {
            System.out.println("Attempting to Connect");
            //myBluetoothSocket.connect();
            System.out.println("Connected");
            myOutputStream = myBluetoothSocket.getOutputStream();
            //myInputStream = myBluetoothSocket.getInputStream();

            myOutputStream.write(50);
            System.out.println("Sent Decrypt Signal");

        } catch (IOException ex) {

        }

    }



    /*
    public void promptPassword(String name){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
        builder.setTitle("Please enter password for: " + name);

        final EditText result = view.findViewById(R.id.txtPassword);

        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(theUSBToDecrypt.password.equals(result.getText().toString())){
                    hasPassword = false;
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void promptPin(String name){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
        builder.setTitle("Please enter pin for: " + name);

        final EditText result = view.findViewById(R.id.txtPassword);

        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(theUSBToDecrypt.pin.equals(result.getText().toString())){
                    hasPin = false;
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordHandler = new Handler();
        pinHandler = new Handler();

        //If its the app is opened
        if((USB)getIntent().getSerializableExtra("USB 0") == null) {
            readFile();

        }

        if(getIntent().getParcelableExtra("Bluetooth Device") == null){
            promptPairDevice();
        }
        else{
            myBluetoothDevice = (BluetoothDevice)getIntent().getParcelableExtra("Bluetooth Device");
            System.out.println("Attempting to read Data");
            readData();
        }

        //Reads the USB's from the edit USB activity
        readUSBsFromEdit();

        //Display USB's
        displayUSBs();

        //Prints the USB list to the console
        printUSBList();


        //Sets up the on click method for the Add New USB button
        Button btnAddNewUSB= (Button) findViewById(R.id.btnAddNewUSB);
        btnAddNewUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditUSB();
            }
        });

        //Deletes all of the USB's in the list and removes them from the view
        Button btnDeleteAllUSB = (Button) findViewById(R.id.btnDeleteAllUSB);
        btnDeleteAllUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                USBList.clear();
                TableLayout table = (TableLayout) findViewById(R.id.tableUSB);
                table.removeAllViews();
                update();
            }
        });

        //readData();

        //unlockAUSB();



    }


    public void promptPairDevice(){
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //If the device has no bluetooth
        if(myBluetoothAdapter == null){
            //System.out.println("No bluetooth adapter available");
            Toast.makeText(getApplicationContext(), "No bluetooth adapter available", Toast.LENGTH_SHORT).show();
        }
        else {
            //Enables bluetooth if bluetooth is off
            if (!myBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }
            //If the device has bluetooth enabled
            else {
                //Some how figure out which device to pair to
                //Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
                pairedDevices = myBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    promptChooseDevice();
                } else {
                    //No paired Bluetooth devices
                    //errorPrompt("There are no paired devices");
                    Toast.makeText(getApplicationContext(), "There are no paired devices", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public void promptChooseDevice(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_dropdown, null);
        builder.setTitle("Connect to a paired devices");
        final Spinner spinner = (Spinner) view.findViewById(R.id.dropDown);

        //Gets the name of all of the paired devices
        List<String> pairedDeviceNames = new ArrayList<String>();
        for (BluetoothDevice device : pairedDevices) {
            pairedDeviceNames.add(device.getName());

        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, pairedDeviceNames);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = spinner.getSelectedItem().toString();
                for(BluetoothDevice device : pairedDevices){
                    if(name.equals(device.getName())){
                        //System.out.println(device.getAddress());
                        myBluetoothDevice = myBluetoothAdapter.getRemoteDevice(device.getAddress());
                        //dialog.cancel();
                        //openBT();
                        readData();
                    }
                }
                //System.out.println("My Bluetooth Device is: " + myBluetoothDevice.getName());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    //Opens the edit USB activity
    public void openEditUSB(){
        Intent intent = new Intent(this, EditUSB.class);
        //Adds all of the USBs to the intent
        int count = 0;
        for(USB u : USBList){
            String title = "USB " + count;
            intent.putExtra(title, u);
            count ++;
        }

        intent.putExtra("Bluetooth Device", myBluetoothDevice);
        startActivity(intent);
    }

    public void printUSBList(){
        for(USB u : USBList){
            System.out.println(u.toString());
        }
    }

    //Reads from the file
    public void readFile(){
        String s = "";
        try {
            InputStream inputStream = context.openFileInput(filename);
            if(inputStream != null){
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                s = br.readLine();
                while(s!=null){
                    StringTokenizer st = new StringTokenizer(s, ",");
                    USBList.add(new USB(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
                    s = br.readLine();
                }
            }
        }
        catch(Exception e){
            System.out.println("Error: " + s);
        }
    }

    //Displays the USBs to the table layput
    public void displayUSBs(){
        TableLayout table = (TableLayout) findViewById(R.id.tableUSB);
        //Writes the USBs to the screen
        for(USB u : USBList){
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView t = new TextView(this);
            t.setText(u.getName());
            t.setTextSize(25);
            tr.addView(t);

            table.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    //Reads the usb information from the editUSB activity
    public void readUSBsFromEdit(){
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
        update();
    }
}
