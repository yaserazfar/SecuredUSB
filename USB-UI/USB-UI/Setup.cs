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
    public partial class Setup : Form
    {
        private Random rng = new Random();

        public Setup()
        {
            InitializeComponent();
            int code = rng.Next(0, 10000);
            txtCode.Text = code.ToString("D4");
        }
    }
}
