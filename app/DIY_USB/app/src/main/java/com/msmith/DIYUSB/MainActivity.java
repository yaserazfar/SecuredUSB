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
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
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
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    //Various Objects needed for bluetooth
    BluetoothAdapter myBluetoothAdapter;
    BluetoothSocket myBluetoothSocket;
    BluetoothDevice myBluetoothDevice;
    OutputStream myOutputStream;
    InputStream myInputStream;

    //Stores the usbToDecrypt
    String usbToDecrypt = "";

    Set<BluetoothDevice> pairedDevices;

    List<USB> USBList = new ArrayList<USB>();
    private Context context = this;
    String filename = "USBData";

    USB theUSBToDecrypt;
    Boolean hasPassword = true;
    Boolean hasPin = true;

    Boolean readData = true;

    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

    //Updates the file containing USBs on the phone
    public void update(){
        new Thread(new Runnable() {
            @Override
            public void run() {
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

    //Reads data from the bluetooth device
    public void readData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(myBluetoothSocket == null) {
                        myBluetoothSocket = myBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        myBluetoothSocket.connect();
                        myInputStream = myBluetoothSocket.getInputStream();
                        myOutputStream = myBluetoothSocket.getOutputStream();
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(myInputStream));

                    //Whiles its waiting to reading a device
                    while(true){
                        //System.out.println("Waiting for input");
                        usbToDecrypt = br.readLine();
                        System.out.println("USBkey to decrypt is: " + usbToDecrypt);
                        decryptAUSB();

                    }
                }
                catch(IOException ex){
                    if(readData) {
                        //Tells the user an error occured
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error: Couldn't connect to the Bluetooth Device", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }).start();
    }


    //Trys to decrypt a usb
    public void decryptAUSB(){
        Boolean found = false;
        for(USB u : USBList){
            //If the usb to decrypt is found
            if(u.usbKey.equals(usbToDecrypt)){

                hasPassword = hasPin = true;

                found = true;
                theUSBToDecrypt = u;

                System.out.println("USB to decrypt is: " + u.name);
                System.out.println("Password: " + u.password);
                System.out.println("Pin:" + u.pin);

                //Sets up if the usb has a password
                if(u.password.equals("")){
                    hasPassword = false;
                }

                //Sets up if the usb has a pin
                if(u.pin.equals("")){
                    hasPin = false;
                }

                System.out.println("Password: " + hasPassword+", " + "Pin: " + hasPin);


                //Prompts the user to enter a password
                if(hasPassword) {
                    System.out.println("Prompting password");
                    //prompt password will prompt the user to enter a pin if the usb has a pin
                    promptPassword();
                }
                //If there is no password, prompts user to enter pin
                else{
                    System.out.println("Prompting pin");
                    promptPin();
                }

            }
        }

        //If the USB trying to be decrypted wasn't found
        if(!found){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Sends an error messgae
                    Toast.makeText(getApplicationContext(), "That USB was not found", Toast.LENGTH_SHORT).show();
                }
            });
            System.out.println("USB not found");
        }
    }

    //Prompts the user to enter the password
    public void promptPassword(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Sets up the alert dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
                builder.setTitle("Please enter password for: " + theUSBToDecrypt.name);

                final EditText result = view.findViewById(R.id.txtPassword);

                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //If the user entered the correct password
                        if(theUSBToDecrypt.password.equals(result.getText().toString())){
                            hasPassword = false;
                            //If the USB has a pin, prompts the user to enter the pin
                            if(hasPin){
                                promptPin();
                            }
                            else{
                                sendDecryptSignal();
                            }

                        }
                        else{
                            //Tells the user that they entered the wrong password
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Error: Incorrect Password", Toast.LENGTH_SHORT).show();
                                }
                            });
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

    //Prompts the user to enter the pin
    public void promptPin(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Sets up the alert dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
                builder.setTitle("Please enter pin for: " + theUSBToDecrypt.name);

                final EditText result = view.findViewById(R.id.txtPassword);

                //Sets up the dialog to enter a pin
                result.setInputType(InputType.TYPE_CLASS_NUMBER);
                result.setTransformationMethod(PasswordTransformationMethod.getInstance());

                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //If the user entered the correct pin
                        if(theUSBToDecrypt.pin.equals(result.getText().toString())){
                            hasPin = false;
                            sendDecryptSignal();
                        }
                        else{
                            //Tells the user that they entered the wrong password
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Error: Incorrect Password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                //Cancel button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                //Displays the alert dialog
                builder.setView(view);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void sendDecryptSignal() {
        try {
            //Sends the decrypt signal
            myOutputStream.write(50);
            Toast.makeText(getApplicationContext(), "USB Decrypted!", Toast.LENGTH_SHORT).show();
            System.out.println("Sent Decrypt Signal");
            readData();

        } catch (IOException ex) {
            Toast.makeText(getApplicationContext(), "Error, Could not send the decrypt signal", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If its the app is opened
        if((USB)getIntent().getSerializableExtra("USB 0") == null) {
            readFile();

        }

        if(getIntent().getParcelableExtra("Bluetooth Device") == null){
            promptPairDevice();
        }
        else{
            System.out.println("Setting up bluetooth device");
            myBluetoothDevice = (BluetoothDevice)getIntent().getParcelableExtra("Bluetooth Device");
            System.out.println("Attempting to read Data");
            readData();
        }

        //Reads the USBs from the edit USB activity
        readUSBsFromEdit();

        //Display USBs
        displayUSBs();


        //Sets up the on click method for the Add New USB button
        Button btnAddNewUSB= (Button) findViewById(R.id.btnAddNewUSB);
        btnAddNewUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetoothDevice != null) {
                    openEditUSB();
                }
                else{
                    Toast.makeText(getApplicationContext(), "You must connect to a device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Deletes all of the USB's in the list and removes them from the view
        Button btnDeleteAllUSB = (Button) findViewById(R.id.btnDeleteAllUSB);
        btnDeleteAllUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDeleteUSB();
            }
        });

        Button btnBluetooth = (Button) findViewById(R.id.btnConnectToComputer);
        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptPairDevice();
            }
        });
    }


    public void promptDeleteUSB(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_dropdown, null);
        builder.setTitle("Delete a USB");
        final Spinner spinner = (Spinner) view.findViewById(R.id.dropDown);

        //Gets the names of a USB
        List<String> usbNames = new ArrayList<>();
        for(USB u : USBList){
            usbNames.add(u.name);
        }

        //Sets the items of the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, usbNames);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = spinner.getSelectedItem().toString();
                //Deletes the USB with the same name
                for(USB u : USBList){
                    if(u.name.equals(name)){
                        //Removes the USB
                        USBList.remove(u);
                        //Updates the file of USBs
                        update();
                        //Removes all of the items from the table
                        TableLayout table = (TableLayout) findViewById(R.id.tableUSB);
                        table.removeAllViews();
                        //Displays the new USBs
                        displayUSBs();

                        break;
                    }
                }
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

                while(true) {
                    if(myBluetoothAdapter.isEnabled()) {
                        promptPairDevice();
                        break;
                    }
                }
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
                        myBluetoothDevice = myBluetoothAdapter.getRemoteDevice(device.getAddress());
                        myBluetoothSocket = null;
                        Toast.makeText(getApplicationContext(), "Device: " + myBluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                        readData();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "You must connect to a device", Toast.LENGTH_SHORT).show();
                //promptChooseDevice();
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
        readData = false;
        try {
            myBluetoothSocket.close();
        }
        catch(IOException ex){

        }
        startActivity(intent);
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
                    String n = st.nextToken();
                    String pass = st.nextToken();
                    String p = st.nextToken();
                    String key = st.nextToken();

                    //Calculates if the USB doesn't have a pin or doesn't have a password
                    if(p.equals(" ")){
                        p = "";
                    }

                    if(pass.equals(" ")){
                        pass = "";
                    }

                    USBList.add(new USB(n, pass, p, key));
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
            t.setText(u.name);
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
