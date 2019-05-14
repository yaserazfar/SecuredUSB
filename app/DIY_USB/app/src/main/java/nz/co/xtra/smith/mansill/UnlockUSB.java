package nz.co.xtra.smith.mansill;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class UnlockUSB extends AppCompatActivity {

    private Context context = this;
    String passwordAttempt = "";
    String pinAttempt = "";
    String usbName = "";

    List<USB> USBList = new ArrayList<USB>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_usb);
    }

    public void unlockAUSB(USB u){
        Boolean passwordUnlocked = true;
        Boolean pinUnlocked = true;

        usbName = u.name;

        //If the usb has a password
        if(u.password != null){
            passwordUnlocked = false;
        }

        //If the USB has a pin
        if(u.pin != null){
            pinUnlocked = false;
        }

        //While the password hasn't been unlocked
        while(!passwordUnlocked){
            promptPassword();
            if(passwordAttempt.equals(u.password)){
                passwordUnlocked = true;
            }
        }

        while(!pinUnlocked){
            promptPin();
            if(pinAttempt.equals(u.pin)){
                pinUnlocked = true;
            }
        }

        sendUnlockSignal();
    }

    //Sends the signal to unlock the USB to the computer
    public void sendUnlockSignal(){

    }

    //Prompts the user to enter a pin
    public void promptPin(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Unlock USB: " + usbName);
        builder.setMessage("Pin:");
        final EditText result = new EditText(context);
        result.setTransformationMethod(new PasswordTransformationMethod());
        builder.setView(result);

        //Set the unlock button
        builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pinAttempt = result.getText().toString();
            }
        });

        //Set the cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //Prompts the user to enter a password
    public void promptPassword(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Unlock USB: " + usbName);
        builder.setMessage("Password:");
        final EditText result = new EditText(context);
        result.setTransformationMethod(new PasswordTransformationMethod());
        builder.setView(result);

        //Set the unlock button
        builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                passwordAttempt = result.getText().toString();
            }
        });

        //Set the cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
