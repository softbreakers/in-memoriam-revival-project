using System.IO;
using System.Diagnostics;
using System.Windows.Forms;

namespace IMRedirectorLauncher
{
    class IMRedirectorLauncher
    {
        static void Main(string[] args)
        {
            string arguments = "";
            foreach (string arg in args)
            {
                arguments = arguments + " " + arg;
            }
            string path = "\"" + Directory.GetCurrentDirectory() + "\\IMRedirector.jar" + "\"";
            System.Diagnostics.Process clientProcess = new Process();
            clientProcess.StartInfo.FileName = "java";
            clientProcess.StartInfo.Arguments = @"-jar " + path + " " + arguments;

            clientProcess.StartInfo.UseShellExecute = false;
            clientProcess.StartInfo.CreateNoWindow = true;

            clientProcess.Start();
            clientProcess.WaitForExit();   
            bool error = (clientProcess.ExitCode != 0);
            if (error)
            {
                MessageBox.Show(
                    "Error while launching IMRedirector. Error code: " + clientProcess.ExitCode.ToString(), 
                    "IMRedirectorLauncher", 
                    MessageBoxButtons.OK, 
                    MessageBoxIcon.Error);
            }
        }
    }
}
