package nz.co.xtra.smith.mansill;

import java.io.Serializable;

public class USB implements Serializable {

    //Stores the information of a USB
    public String name;
    public String password;
    public String pin;
    //public USBKey
    //int num;

    //Constructs a USB object
    public USB(String name, String password, String pin){
        this.name = name;
        this.password = password;
        this.pin = pin;
        //this.num = num;
    }

    //Sets the password
    public void setPassword(String newPassword){
        this.password = newPassword;
    }

    //Sets the pin
    public void setPin(String newPin){
        this.pin = newPin;
    }

    //Gets the name
    public String getName(){ return this.name; }

    //Sets the num
    //public void setNum(int newNum) {this.num = newNum;}

    //Checks if the password is correct
    public boolean isCorrectPassword(String password){
        if(this.password.equals(password)){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isCorrectPin(String pin){
        if(this.pin.equals(pin)){
            return true;
        }
        else{
            return false;
        }
    }

    public String toString() {
        String tempPassword = this.password;
        String tempPin = this.pin;

        if(this.password.equals("")){
            tempPassword = " ";
        }
        if(this.pin.equals("")){
            tempPin = " ";
        }
        return this.getName() + "," + tempPassword + "," + tempPin + "\n";
    }
}

