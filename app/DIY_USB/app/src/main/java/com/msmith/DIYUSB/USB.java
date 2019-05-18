package com.msmith.DIYUSB;

import java.io.Serializable;

public class USB implements Serializable {

    //Stores the information of a USB
    public String name;
    public String password;
    public String pin;
    public String usbKey;


    //Constructs a usb object with all of the attributes
    public USB(String name, String password, String pin, String usbKey){
        this.name = name;
        this.password = password;
        this.pin = pin;
        this.usbKey = usbKey;
    }

    //Constructs a USB object with no key
    public USB(String name, String password, String pin){
        this.name = name;
        this.password = password;
        this.pin = pin;
    }

    //Overrides the tostring method for a USB
    public String toString() {
        String tempPassword = this.password;
        String tempPin = this.pin;

        if(this.password.equals("")){
            tempPassword = " ";
        }
        if(this.pin.equals("")){
            tempPin = " ";
        }
        return name + "," + tempPassword + "," + tempPin +  "," + usbKey + "\n";
    }
}

