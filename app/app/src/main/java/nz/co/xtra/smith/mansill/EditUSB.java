package nz.co.xtra.smith.mansill;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class EditUSB extends AppCompatActivity {


    String password = "";
    String pin = "";
    String temp = "";
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_usb);

        //Prompts the password alert when switch on
        final Switch passwordSwitch = (Switch) findViewById(R.id.switchPassword);
        passwordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Switch passwordSwitch = (Switch) findViewById(R.id.switchPassword);
                if(passwordSwitch.isChecked()){
                    temp = prompt("Set Password", R.layout.alert_input, passwordSwitch);
                    if(temp != null) {
                        password = temp;
                    }
                }
                System.out.println("| " + password + " | " + pin + " | " + temp + " |");
            }
        });

        //Prompts the pin alert when switch on
        final Switch pinSwitch = (Switch) findViewById(R.id.switchPin);
        pinSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Switch pinSwitch = (Switch) findViewById(R.id.switchPin);
                if(pinSwitch.isChecked()){
                    temp = prompt("Set Pin",  R.layout.alert_input, pinSwitch);
                    //If the pin wasn't changed
                    if(temp != null) {
                        pin = temp;
                    }
                }
                System.out.println("| " + password + " | " + pin + " | " + temp + " |");
            }
        });



        //Sets up the confirm button on click method
        Button btnConfirm = (Button)findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText txtName = (EditText)findViewById(R.id.txtName);
                String name = txtName.getText().toString();

                USB temp = new USB(name, password, pin);
                //Must have a valid name
                if(name.equals("")){
                    errorPrompt("You must set a name for the USB");
                }
                else {
                    //Must have a valid password or pin
                    if(password.equals("") && pin.equals("")){
                        errorPrompt("You must set a 4 digit pin or a password");
                    }
                    else {
                        System.out.println("l "+ name + " l " + password + " l " + pin + " l");
                        openMainActivity(temp);
                    }

                }
            }
        });

        //Cancel button
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity(null);
            }
        });


        Button btnPasswordEdit = (Button) findViewById(R.id.btnPasswordEdit);
        btnPasswordEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                //Prompt edit password
                String temp = prompt("Set Password", R.layout.alert_input, passwordSwitch);
                //If the password wasn't changed
                if(temp != null) {
                    password = temp;
                }
                */

                passwordSwitch.setChecked(true);
            }
        });

        Button btnPinEdit = (Button) findViewById(R.id.btnPinEdit);
        btnPinEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                //Prompt edit pin
                temp = prompt("Set Pin",  R.layout.alert_input, pinSwitch);
                //If the pin wasn't changed
                if(temp != null) {
                    pin = temp;
                }
                */

                pinSwitch.setChecked(true);
            }
        });
    }

    //Opens the main activity and gives a USB object back to the main activity
    public void openMainActivity(USB temp){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Prompts an input alert
    public String prompt(String title, int resource, final Switch theSwitch){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        if(resource != 0) {
            builder.setView(resource);
        }

        final EditText result = new EditText(context);
        //Clicked Set
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //LayoutInflater layoutInflater = LayoutInflater.from(context);
                //View promptUserView = layoutInflater.inflate(R.layout.alert_input, null);
                //EditText result = (EditText) findViewById(R.id.txtDialogUserInput);

                temp = result.getText().toString();
                theSwitch.setChecked(true);
                System.out.println("| " + password + " | " + pin + " | " + temp + " | " + result + " |");
            }
        });

        //Clicked Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                temp = null;
                //dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        return temp;
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
