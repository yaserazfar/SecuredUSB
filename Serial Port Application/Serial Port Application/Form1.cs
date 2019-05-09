using System;
using System.Windows.Forms;
using System.IO.Ports;

namespace Serial_Port_Application
{
    public partial class Form1 : Form
    {
        static SerialPort port;

        public Form1()
        {
            InitializeComponent();
        }
        
        private void buttonConnect_Click(object sender, EventArgs e)
        {
            port = new SerialPort("COM3", 9600, Parity.None, 8, StopBits.One);              //REPLACE COM7 WITH YOUR BLUETOOTH PORT (DeviceManager->Ports->StandardBluetooth...)
            textBoxStatus.Text = "Please unlock your phone...";

            port.DataReceived += new SerialDataReceivedEventHandler(port_DataReceived);     //When the serial port receives data (from the phone), it will call the function "port_DataReceived"
            port.Open();

            while (true)
            {
                port.WriteLine(TextBoxCommunication.Text);     //Write whatever we enter into console to the serial port (send to phone)
            }
        }

        private void port_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            Console.WriteLine(port.ReadExisting());         //Write whatever the phone sent
            TextBox.CheckForIllegalCrossThreadCalls = false;
            textBoxStatus.Text = "Unlocked...";
        }
    }
}

