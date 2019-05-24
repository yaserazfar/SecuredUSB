using System;
using System.Windows.Forms;
using System.IO.Ports;
using System.Security.Cryptography;
using System.IO;
using System.Linq;

namespace Serial_Port_Application
{
    public partial class Form1 : Form
    {
        static SerialPort port;
        public static int UniqueUsbKey = 0; //this is a key that will be given to every usb and is different for each usb *would potentially be in the cloud*
        //public static List<int> UsbKeyList = new List<int>();

        string destinationFile = "";

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
                string sourceDirectory = Application.StartupPath;
                string targetDirectory = destinationFile;
                

                //To Decrypt, call this with false
                Copy(sourceDirectory, targetDirectory, false);
            }
        }

        private void buttonEncrypt_Click(object sender, EventArgs e)
        {
            //This encrypts the current directory that the EXE runs in. PLEASE NOTE: This means you can't really test this by just running it, drop the exe
            //In the folder you want to test and run it there instead.
            string sourceDirectory = Application.StartupPath;
            string targetDirectory = Application.StartupPath;

            Copy(sourceDirectory, targetDirectory, true);

        }

        private void buttonDecrypt_Click(object sender, EventArgs e)    //sends key to user and promts them to unlock phone 
        {
            FolderBrowserDialog folderBrowserDialog1 = new FolderBrowserDialog();
            if (folderBrowserDialog1.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            {
                destinationFile = (folderBrowserDialog1.SelectedPath);
            }
            //MessageBox.Show(destinationFile);
            //send UniqueUsbKey of seleceted USB to port - selected from listbox
            port.WriteLine(UniqueUsbKey.ToString());
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
                Encrypt test = new Encrypt();
                string pass = "klsjndvsodvn";
                string fileName = fi.Name + ".en";
                string destinationFile = Path.Combine(target.FullName, fileName);

                //Make sure it isn't already encrypted or the executable
                if (Path.GetExtension(fi.Name) != ".en" && fi.Name != "Serial Port Application.exe")
                {
                    test.EncryptFile(fi.FullName, destinationFile, pass, test.salt, 1000);
                    fi.Delete();
                }

            }

            foreach (DirectoryInfo diSourceSubDir in source.GetDirectories())
            {
                DirectoryInfo nextTargetSubDir =
                    target.CreateSubdirectory(diSourceSubDir.Name);
                EncryptAll(diSourceSubDir, nextTargetSubDir);
                //Delete any empty folder dupes
                if (!Directory.EnumerateFileSystemEntries(diSourceSubDir.FullName).Any())
                {
                    diSourceSubDir.Delete();
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
            Directory.CreateDirectory(target.FullName);

            foreach (FileInfo fi in source.GetFiles())
            {
                Encrypt test = new Encrypt();
                string pass = "klsjndvsodvn";
                string destinationFile;

                //Decrypt only if it is a .en file
                if (Path.GetExtension(fi.Name) == ".en")
                {
                    string fileName = fi.Name;
                    fileName = Path.ChangeExtension(fileName, null);
                    destinationFile = Path.Combine(target.FullName, fileName);
                    test.DecryptFile(fi.FullName, destinationFile, pass, test.salt, 1000);
                }
            }

            foreach (DirectoryInfo diSourceSubDir in source.GetDirectories())
            {
                DirectoryInfo nextTargetSubDir =
                    target.CreateSubdirectory(diSourceSubDir.Name);
                DecryptAll(diSourceSubDir, nextTargetSubDir);
            }
        }
    }

    /// <summary>
    /// Class that contains all neccecary functions for encryption.
    /// </summary>
    class Encrypt
    {
        // Rfc2898DeriveBytes constants:
        public readonly byte[] salt = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }; // Must be at least eight bytes.  MAKE THIS SALTIER!
        public const int iterations = 1042; // Recommendation is >= 1000.

        /// <summary>Decrypt a file.</summary>
        /// <param name="sourceFilename">The full path and name of the file to be decrypted.</param>
        /// <param name="destinationFilename">The full path and name of the file to be output.</param>
        /// <param name="password">The password for the decryption.</param>
        /// <param name="salt">The salt to be applied to the password.</param>
        /// <param name="iterations">The number of iterations Rfc2898DeriveBytes should use before generating the key and initialization vector for the decryption.</param>
        public void DecryptFile(string sourceFilename, string destinationFilename, string password, byte[] salt, int iterations)
        {
            AesManaged aes = new AesManaged();
            aes.BlockSize = aes.LegalBlockSizes[0].MaxSize;
            aes.KeySize = aes.LegalKeySizes[0].MaxSize;
            // NB: Rfc2898DeriveBytes initialization and subsequent calls to   GetBytes   must be eactly the same, including order, on both the encryption and decryption sides.
            Rfc2898DeriveBytes key = new Rfc2898DeriveBytes(password, salt, iterations);
            aes.Key = key.GetBytes(aes.KeySize / 8);
            aes.IV = key.GetBytes(aes.BlockSize / 8);
            aes.Mode = CipherMode.CBC;
            ICryptoTransform transform = aes.CreateDecryptor(aes.Key, aes.IV);

            using (FileStream destination = new FileStream(destinationFilename, FileMode.CreateNew, FileAccess.Write, FileShare.None))
            {
                using (CryptoStream cryptoStream = new CryptoStream(destination, transform, CryptoStreamMode.Write))
                {
                    try
                    {
                        using (FileStream source = new FileStream(sourceFilename, FileMode.Open, FileAccess.Read, FileShare.Read))
                        {
                            source.CopyTo(cryptoStream);
                        }
                    }
                    catch (CryptographicException exception)
                    {
                        if (exception.Message == "Padding is invalid and cannot be removed.")
                            throw new ApplicationException("Universal Microsoft Cryptographic Exception (Not to be believed!)", exception);
                        else
                            throw;
                    }
                }
            }
        }

        /// <summary>Encrypt a file.</summary>
        /// <param name="sourceFilename">The full path and name of the file to be encrypted.</param>
        /// <param name="destinationFilename">The full path and name of the file to be output.</param>
        /// <param name="password">The password for the encryption.</param>
        /// <param name="salt">The salt to be applied to the password.</param>
        /// <param name="iterations">The number of iterations Rfc2898DeriveBytes should use before generating the key and initialization vector for the decryption.</param>
        public void EncryptFile(string sourceFilename, string destinationFilename, string password, byte[] salt, int iterations)
        {
            AesManaged aes = new AesManaged();
            aes.BlockSize = aes.LegalBlockSizes[0].MaxSize;
            aes.KeySize = aes.LegalKeySizes[0].MaxSize;
            // NB: Rfc2898DeriveBytes initialization and subsequent calls to   GetBytes   must be eactly the same, including order, on both the encryption and decryption sides.
            Rfc2898DeriveBytes key = new Rfc2898DeriveBytes(password, salt, iterations);
            aes.Key = key.GetBytes(aes.KeySize / 8);
            aes.IV = key.GetBytes(aes.BlockSize / 8);
            aes.Mode = CipherMode.CBC;
            ICryptoTransform transform = aes.CreateEncryptor(aes.Key, aes.IV);

            using (FileStream destination = new FileStream(destinationFilename, FileMode.CreateNew, FileAccess.Write, FileShare.None))
            {
                using (CryptoStream cryptoStream = new CryptoStream(destination, transform, CryptoStreamMode.Write))
                {
                    using (FileStream source = new FileStream(sourceFilename, FileMode.Open, FileAccess.Read, FileShare.Read))
                    {
                        source.CopyTo(cryptoStream);
                    }
                }
            }
        }

    }
}

