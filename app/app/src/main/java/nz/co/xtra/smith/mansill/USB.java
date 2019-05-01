package nz.co.xtra.smith.mansill;

public class USB {

    //Stores the information of a USB
    String name;
    String password;
    String pin;

    //Constructs a USB object
    public USB(String name, String password, String pin){
        this.name = name;
        this.password = password;
        this.pin = pin;
    }

    //Sets the password
    public void setPassword(String newPassword){
        this.password = newPassword;
    }

    //Sets the pin
    public void setPin(String newPin){
        this.pin = newPin;
    }

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
}

