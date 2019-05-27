using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Serial_Port_Application
{
    public partial class Verify : Form
    {
        private Random rng = new Random();
        public static int code;
        public Verify()
        {
            InitializeComponent();
            code = rng.Next(0, 10000);
            txtCode.Text = code.ToString("D4");
            Main.port.WriteLine(Flags.REGISTER);
        }

        private void buttonOK_Click(object sender, EventArgs e)
        {
            Close();
        }
    }
}
