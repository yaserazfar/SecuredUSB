using System;
using System.Windows.Forms;
using System.IO.Ports;

namespace Serial_Port_Application
{
    public partial class Form1 : Form
    {
        static SerialPort port;
        public static int UniqueUsbKey = 0; //this is a key that will be given to every usb and is different for each usb *would potentially be in the cloud*
        //public static List<int> UsbKeyList = new List<int>();

        public Form1()
        {
            InitializeComponent();
            port = new SerialPort("COM3", 9600, Parity.None, 8, StopBits.One);              //REPLACE COM3 WITH YOUR BLUETOOTH PORT (DeviceManager->Ports->StandardBluetooth...)

            port.DataReceived += new SerialDataReceivedEventHandler(port_DataReceived);     //When the serial port receives data (from the phone), it will call the function "port_DataReceived"
            port.Open();

        }
        
        private void port_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            string recievedString = (port.ReadExisting());
            Console.WriteLine(recievedString);         //Write whatever the phone sent

            if (recievedString == "1") //this is the integer used to represent when user is trying to sync usb
            {
                UniqueUsbKey++;
                //port.WriteLine("USB Connected");
                port.WriteLine(UniqueUsbKey.ToString());

                //UsbKeyList.Add(UniqueUsbKey);
                
                TextBox.CheckForIllegalCrossThreadCalls = false;
                listBoxUsb.Items.Add("USB #" + UniqueUsbKey);
            }

            if (recievedString == "2")  //once the decrypt button is clicked, it will notify user on mobile and if yes then 2
            {
                //TextBox.CheckForIllegalCrossThreadCalls = false;
                //labelStatus.Text = "Decrypting files...";

                //===========------------TO DO DANIEL-------================
                //decrypt usb information onto pc 
            }
        }

        private void buttonEncrypt_Click(object sender, EventArgs e)
        {
            //TextBox.CheckForIllegalCrossThreadCalls = false;
            //labelStatus.Text = "Encrypting files...";

            //=============------------TO DO DANIEL-------====================
            //encrypt the files 
        }

        private void buttonDecrypt_Click(object sender, EventArgs e)    //sends key to user and promts them to unlock phone 
        {
            //send UniqueUsbKey of seleceted USB to port - selected from listbox
            port.WriteLine(UniqueUsbKey.ToString());
        }
    }
}

