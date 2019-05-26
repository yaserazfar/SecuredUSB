using System;
using System.Windows.Forms;
using System.IO.Ports;
using System.Security.Cryptography;
using System.IO;
using System.Linq;

namespace Serial_Port_Application
{
    public partial class Main : Form
    {
        public static SerialPort port;
        public static long UniqueUsbKey = -1; //this is a key that will be given to every usb and is different for each usb *would potentially be in the cloud*
        //public static List<int> UsbKeyList = new List<int>();

        string destinationFile = "";

        public Main()
        {
            InitializeComponent();
            StreamReader r = new StreamReader(Path.Combine(Application.StartupPath, "id.en"));
            UniqueUsbKey = Convert.ToInt64(r.ReadLine());
            r.Close();

            port = new SerialPort("COM3", 9600, Parity.None, 8, StopBits.One);              //REPLACE COM3 WITH YOUR BLUETOOTH PORT (DeviceManager->Ports->StandardBluetooth...)
            port.DataReceived += new SerialDataReceivedEventHandler(port_DataReceived);     //When the serial port receives data (from the phone), it will call the function "port_DataReceived"
            port.Open();
        }
        
        private void port_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            string recievedString = (port.ReadExisting());
            Console.WriteLine(recievedString);         //Write whatever the phone sent

            if (recievedString.Length == 4)
            {
                if (Int32.TryParse(recievedString, out int code))
                {
                    if (code == Verify.code)
                    {
                        port.WriteLine(UniqueUsbKey.ToString());
                    }
                    else
                    {
                        port.WriteLine(Flags.INCORRECT_CODE);
                    }
                }
            }

            if (recievedString == "2")  //once the decrypt button is clicked, it will notify user on mobile and if yes then 2
            {
                labelStatus.Text = "Decrypting files...";
                
                string sourceDirectory = Application.StartupPath;
                string targetDirectory = destinationFile;
                
                //To Decrypt, call this with false
                Copy(sourceDirectory, targetDirectory, false);
            }
        }

        private void buttonEncrypt_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog folderBrowserDialog1 = new FolderBrowserDialog();
            if (folderBrowserDialog1.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            {
                //This encrypts the current directory that the EXE runs in. PLEASE NOTE: This means you can't really test this by just running it, drop the exe
                //In the folder you want to test and run it there instead.
                string sourceDirectory = folderBrowserDialog1.SelectedPath;
                string targetDirectory = Application.StartupPath;

                Copy(sourceDirectory, targetDirectory, true);
            }
        }

        private void buttonDecrypt_Click(object sender, EventArgs e)    //sends key to user and promts them to unlock phone 
        {
            FolderBrowserDialog folderBrowserDialog1 = new FolderBrowserDialog();
            if (folderBrowserDialog1.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            {
                destinationFile = (folderBrowserDialog1.SelectedPath);
                //MessageBox.Show(destinationFile);
                //send UniqueUsbKey of seleceted USB to port - selected from listbox
                port.WriteLine(UniqueUsbKey.ToString());
            }
        }


        /// <summary>
        /// Sets up the two directories. If encrypt is true it will encrypt, if encrypt is false it will decrypt
        /// </summary>
        /// <param name="sourceDirectory"></param>
        /// <param name="targetDirectory"></param>
        /// <param name="encrypt"></param>
        private void Copy(string sourceDirectory, string targetDirectory, bool encrypt)
        {
            DirectoryInfo diSource = new DirectoryInfo(sourceDirectory);
            DirectoryInfo diTarget = new DirectoryInfo(targetDirectory);

            if (encrypt == true)
            {
                EncryptAll(diSource, diTarget);
            }
            else
            {
                DecryptAll(diSource, diTarget);
            }

        }

        /// <summary>
        /// Takes a source directory and a destination directory and fully encrypts everything in it.
        /// NOTE: This also DELETES the orignal unencrypted files, to get them back they must be decrypted
        /// This won't encrypt anything already encrypted
        /// </summary>
        /// <param name="source"></param>
        /// <param name="target"></param>
        private void EncryptAll(DirectoryInfo source, DirectoryInfo target)
        {
            Directory.CreateDirectory(target.FullName);

            foreach (FileInfo fi in source.GetFiles())
            {
                //We want to encrypt these files now. In other words, using target.FullName we now know where we are dropping them.
                Encrypt encrypt = new Encrypt();
                string pass = "klsjndvsodvn";
                string fileName = fi.Name + ".en";
                string destinationFile = Path.Combine(target.FullName, fileName);

                //Make sure it isn't already encrypted or the executable
                if (Path.GetExtension(fi.Name) != ".en" && fi.Name != AppDomain.CurrentDomain.FriendlyName)
                {
                    encrypt.EncryptFile(fi.FullName, destinationFile, pass, encrypt.salt, 1000);
                    fi.Delete();
                }

            }

            foreach (DirectoryInfo diSourceSubDir in source.GetDirectories())
            {
                if (Path.GetFileName(diSourceSubDir.Name) != "System Volume Information")
                {
                    DirectoryInfo nextTargetSubDir = target.CreateSubdirectory(diSourceSubDir.Name);
                    EncryptAll(diSourceSubDir, nextTargetSubDir);
                    //Delete any empty folder dupes
                    if (!Directory.EnumerateFileSystemEntries(diSourceSubDir.FullName).Any())
                    {
                        diSourceSubDir.Delete();
                    }
                }

            }
        }

        /// <summary>
        /// Takes a source folder and a destination folder and fully decrypts everything into it.
        /// </summary>
        /// <param name="source"></param>
        /// <param name="target"></param>
        private void DecryptAll(DirectoryInfo source, DirectoryInfo target)
        {
            try
            {
                Directory.CreateDirectory(target.FullName);

                foreach (FileInfo fi in source.GetFiles())
                {
                    Encrypt test = new Encrypt();
                    string pass = "klsjndvsodvn";
                    string destinationFile;

                    //Decrypt only if it is a .en file
                    if (Path.GetExtension(fi.Name).Contains(".en") && Path.GetFileName(fi.Name) != "id.en")
                    {
                        string fileName = fi.Name;
                        fileName = Path.ChangeExtension(fileName, null);
                        destinationFile = Path.Combine(target.FullName, fileName);
                        test.DecryptFile(fi.FullName, destinationFile, pass, test.salt, 1000);

                    }
                }

                foreach (DirectoryInfo diSourceSubDir in source.GetDirectories())
                {
                    if (Path.GetFileName(diSourceSubDir.Name) != "System Volume Information")
                    {
                        DirectoryInfo nextTargetSubDir = target.CreateSubdirectory(diSourceSubDir.Name);
                        DecryptAll(diSourceSubDir, nextTargetSubDir);
                    }
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message);
            }
        }

        private void buttonQuit_Click(object sender, EventArgs e)
        {
            StreamWriter w = new StreamWriter(Path.Combine(Application.StartupPath, "id.en"));
            w.WriteLine(UniqueUsbKey);
            w.Close();
            Close();
        }

        private void buttonSetup_Click(object sender, EventArgs e)
        {
            Verify verify = new Verify();
            verify.ShowDialog();
        }

        private const int CP_NOCLOSE_BUTTON = 0x200;
        protected override CreateParams CreateParams
        {
            get
            {
                CreateParams myCp = base.CreateParams;
                myCp.ClassStyle = myCp.ClassStyle | CP_NOCLOSE_BUTTON;
                return myCp;
            }
        }
    }
}

