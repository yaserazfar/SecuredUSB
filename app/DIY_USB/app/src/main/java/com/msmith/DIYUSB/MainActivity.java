package com.msmith.DIYUSB;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    List<USB> USBList = new ArrayList<USB>();
    private Context context = this;
    String filename = "USBData";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If its the app is opened
        if((USB)getIntent().getSerializableExtra("USB 0") == null) {
            readFile();
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
        startActivity(intent);
    }

    public void printUSBList(){
        for(USB u : USBList){
            System.out.println(u.getName());
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
                    USBList.add(new USB(st.nextToken(), st.nextToken(), st.nextToken()));
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
