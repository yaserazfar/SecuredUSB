using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace DIY_USB
{
    public partial class Main : Form
    {
        public Main()
        {
            InitializeComponent();
        }

        private void btnSetup_Click(object sender, EventArgs e)
        {
            Setup setup = new Setup();
            setup.StartPosition = FormStartPosition.CenterScreen;
            setup.ShowDialog();
        }

        private void btnDecrypt_Click(object sender, EventArgs e)
        {

        }

        private void btnEncrypt_Click(object sender, EventArgs e)
        {

        }

        private void btnQuit_Click(object sender, EventArgs e) => Close();
    }
}
