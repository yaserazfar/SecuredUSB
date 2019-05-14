package nz.co.xtra.smith.mansill;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

import nz.co.xtra.smith.mansill.R;

public class EditUSB extends AppCompatActivity {

    String name = "";
    String password = "";
    String pin = "";
    String temp = "";
    Boolean justPasswordHitEdit = false;
    Boolean justPinHitEdit = false;
    private Context context = this;
    nz.co.xtra.smith.mansill.USB tempUSB;


    List<nz.co.xtra.smith.mansill.USB> USBList = new ArrayList<nz.co.xtra.smith.mansill.USB>();
    //enum USBStates {SAVE, DELETE, EDIT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_usb);

        //Gets the USB list
        Boolean moreUSBs = true;
        int count = 0;
        while(moreUSBs){
            String title = "USB " + count;
            nz.co.xtra.smith.mansill.USB temp = (nz.co.xtra.smith.mansill.USB)getIntent().getSerializableExtra(title);
            count ++;
            if(temp == null){
                moreUSBs = false;
            }
            else{
                USBList.add(temp);
            }

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

                    for(nz.co.xtra.smith.mansill.USB u: USBList){
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
                            System.out.println("l " + name + " l " + password + " l " + pin + " l");
                            openMainActivity("Save");
                        }
                    }

                }
            }
        });
    }

    //Opens the main activity and gives a USB object back to the main activity
    public void openMainActivity(String state){
        Intent intent = new Intent(this, nz.co.xtra.smith.mansill.MainActivity.class);

        USBList.add(new nz.co.xtra.smith.mansill.USB(name, password, pin));
        int count = 0;
        for(nz.co.xtra.smith.mansill.USB u : USBList){
            String title = "USB " + count;
            intent.putExtra(title, u);
            count ++;
        }
        startActivity(intent);
    }

    public void promptPassword(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Password");


        final EditText result = new EditText(context);
        result.setTransformationMethod(new PasswordTransformationMethod());
        builder.setView(result);

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
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //Prompts an input alert
    public void promptPin(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Pin");


        final EditText result = new EditText(context);
        result.setTransformationMethod(new PasswordTransformationMethod());
        builder.setView(result);

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
            }
        });

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
