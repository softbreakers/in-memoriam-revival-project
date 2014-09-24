package start;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import javax.swing.ImageIcon;


public class IMRedirector {

	private JFrame frmInMemoriamRevival;
	
	private static String strHostFilePath = null;
	
	private final String beginBlock = "# IN MEMORIAL REVIVAL PROJECT BEGIN BLOCK";
	private final String endBlock   = "# IN MEMORIAL REVIVAL PROJECT END BLOCK";
	public final String urlHome   = "http://inmemoriam.softbreakers.com";
	private final String urlIpList = urlHome + "/iplist.txt";
	
	private String actualVersion = "";
	
	private String webVersion = "";
	private List<String> webIpList = null;
	
	private String lastError = "";
	
	private boolean isWindows = false;
	private boolean isMac = false;
	private List<String> versNumbers = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			strHostFilePath = args[0];
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IMRedirector window = new IMRedirector();
					window.frmInMemoriamRevival.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public IMRedirector() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmInMemoriamRevival = new JFrame();
		frmInMemoriamRevival.setIconImage(Toolkit.getDefaultToolkit().getImage(IMRedirector.class.getResource("/start/fenix_ico_trans.png")));
		frmInMemoriamRevival.setResizable(false);
		frmInMemoriamRevival.setTitle("In Memoriam Revival 1.0");
		frmInMemoriamRevival.setBounds(100, 100, 272, 201);
		frmInMemoriamRevival.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmInMemoriamRevival.getContentPane().setLayout(null);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frmInMemoriamRevival.setLocation(dim.width/2-frmInMemoriamRevival.getSize().width/2, dim.height/2-frmInMemoriamRevival.getSize().height/2);
		
		final JButton btnInstall = new JButton("Install");
		//btnInstall.setIcon(new ImageIcon(IMRedirector.class.getResource("/start/fenix_logo_trans.ico")));
		btnInstall.setBounds(10, 125, 89, 23);
		frmInMemoriamRevival.getContentPane().add(btnInstall);
		
		final JButton btnUninstall = new JButton("Uninstall");
		//btnUninstall.setIcon(new ImageIcon(IMRedirector.class.getResource("/start/fenix_logo_trans.ico")));
		btnUninstall.setBounds(167, 125, 89, 23);
		frmInMemoriamRevival.getContentPane().add(btnUninstall);
		
		final JLabel lblStatus = new JLabel("Scanning...");
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblStatus.setBounds(10, 159, 256, 14);
		frmInMemoriamRevival.getContentPane().add(lblStatus);
		
		String strOS = System.getProperty("os.name");
		String strOSVersion = System.getProperty("os.version");
		String strOSlower = strOS.toLowerCase(Locale.ENGLISH);
		
		isWindows = strOSlower.contains("windows");
		isMac = strOSlower.contains("os x");
		versNumbers = new ArrayList<String>(Arrays.asList(strOSVersion.split("\\.")));
		
		// http://lopica.sourceforge.net/os.html
		// http://en.wikipedia.org/wiki/Hosts_(file)#Location_in_the_file_system
		// https://developer.apple.com/library/mac/technotes/tn2002/tn2110.html
		if (strHostFilePath == null) {
			if (isWindows) {
				if (strOSlower.contains("me") || strOSlower.contains("98")) { 
					strHostFilePath = System.getenv("WINDIR") + "\\hosts";
				}
				else {
					strHostFilePath = System.getenv("SYSTEMROOT") + "\\system32\\drivers\\etc\\hosts";
				}
			}
			else if (isMac) { 
				isMac = true;
				if ((versNumbers.size() > 1) && (versNumbers.get(0)=="10")) {
					try {
						Integer subVers = Integer.parseInt(versNumbers.get(1));
						if (subVers >= 2) {
							strHostFilePath = "/etc/hosts";
						}
					}
					catch (NumberFormatException e) {
						
					}
				}
			}
		}
		
		if (strHostFilePath != null) {  // Test if file exists
			File testFile = new File(strHostFilePath);
			if (!testFile.isFile()) {
				strHostFilePath = null;
			}
		}
		
		if (strHostFilePath == null) { // Hosts file path not detected. Exit
			JOptionPane.showMessageDialog(frmInMemoriamRevival, "Hosts file not found. Introduce your hosts file path as argument.\r\n" +
		       "'IMRedirector.jar [ABSOLUTE_PATH_AND_FILENAME]'\r\n" +
			   "Example: 'IMRedirector.jar c:\\windows\\hosts'");
			System.exit(0);
		}
		
		lblStatus.setText(findBlock());
		
		btnUninstall.setEnabled(!actualVersion.isEmpty());
		
		JLabel lblOS = new JLabel("New label");
		lblOS.setBounds(41, 0, 89, 14);
		frmInMemoriamRevival.getContentPane().add(lblOS);
		
		lblOS.setText(strOS);
		
		if (getIPList()) {
			btnInstall.setEnabled(!actualVersion.equals(webVersion));
			btnInstall.setText(actualVersion.isEmpty() ? "Install":"Update");
		}
		else {
			lblStatus.setText(lastError);
			lastError = "";
			btnInstall.setEnabled(false);
		}
		
		
		JLabel lblHostsFolder = new JLabel("New label");
		lblHostsFolder.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblHostsFolder.setBounds(10, 21, 256, 14);
		frmInMemoriamRevival.getContentPane().add(lblHostsFolder);
		
		lblHostsFolder.setText("Hosts file: " + strHostFilePath);
		
		JLabel lblOSVersion = new JLabel("New label");
		lblOSVersion.setBounds(198, 0, 58, 14);
		frmInMemoriamRevival.getContentPane().add(lblOSVersion);
		
		lblOSVersion.setText(strOSVersion);
		
		JLabel lblRedirectorBlock = new JLabel("Redirector block versions :");
		lblRedirectorBlock.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblRedirectorBlock.setBounds(10, 46, 138, 14);
		frmInMemoriamRevival.getContentPane().add(lblRedirectorBlock);
		
		final JLabel lblBlockInstalled = new JLabel("New label");
		lblBlockInstalled.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBlockInstalled.setBounds(98, 65, 89, 14);
		frmInMemoriamRevival.getContentPane().add(lblBlockInstalled);
		
		JLabel lblBlockWeb = new JLabel("New label");
		lblBlockWeb.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBlockWeb.setBounds(98, 84, 75, 14);
		frmInMemoriamRevival.getContentPane().add(lblBlockWeb);
		
		JLabel lblNewLabel = new JLabel("Installed: ");
		lblNewLabel.setBounds(41, 65, 58, 14);
		frmInMemoriamRevival.getContentPane().add(lblNewLabel);
		
		JLabel lblWeb = new JLabel("Web: ");
		lblWeb.setBounds(41, 84, 58, 14);
		frmInMemoriamRevival.getContentPane().add(lblWeb);
		
		
		lblBlockInstalled.setText(actualVersion.isEmpty()?"--":actualVersion);
		lblBlockWeb.setText(webVersion.isEmpty()?"--":webVersion);
		
		JLabel lblNewLabel_1 = new JLabel("OS:");
		lblNewLabel_1.setBounds(10, 0, 34, 14);
		frmInMemoriamRevival.getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("OS Version:");
		lblNewLabel_2.setBounds(120, 0, 73, 14);
		frmInMemoriamRevival.getContentPane().add(lblNewLabel_2);
		
		JButton btnWeb = new JButton("");
		btnWeb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(urlHome));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		btnWeb.setIcon(new ImageIcon(IMRedirector.class.getResource("/start/fenix_ico_trans.png")));
		btnWeb.setBounds(183, 41, 73, 73);
		frmInMemoriamRevival.getContentPane().add(btnWeb);

		btnUninstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lastError = "";
				if (!actualVersion.isEmpty()) {
					if (removeBlock()) {
						findBlock();
						if (actualVersion.isEmpty()) {
							lblStatus.setText("In Memoriam Block removed");
							btnUninstall.setEnabled(false);
							btnInstall.setEnabled(true);
							btnInstall.setText("Install");
							lblBlockInstalled.setText("--");
							
							cleanDNSCache();
							}							
						}
						else {
							lblStatus.setText("Unknown error. In Memorial Block not removed");
						}
					}
					else {
						lblStatus.setText(lastError);
						lastError = "";
					}
				}
			}
		);
		
		btnInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lastError = "";
				if (!webVersion.isEmpty()) {
					if (!actualVersion.isEmpty()) {
						if (removeBlock()) {
							findBlock();
							if (actualVersion.isEmpty()) {
								lblStatus.setText("In Memoriam Block removed");
								btnUninstall.setEnabled(false);
								btnInstall.setEnabled(true);
								btnInstall.setText("Install");
								lblBlockInstalled.setText("--");
							}
							else {
								lblStatus.setText("Unknown error. In Memorial Block not removed");
							}
							
						}
						else {
							lblStatus.setText(lastError);
						}
					}
					if (lastError.isEmpty()) {
						if (insertBlock()) {
							findBlock();
							if (actualVersion.equals(webVersion))
							{
								lblStatus.setText("In Memoriam Block inserted");
								btnUninstall.setEnabled(true);
								btnInstall.setEnabled(false);
								btnInstall.setText("Update");
								lblBlockInstalled.setText(actualVersion);
								
								cleanDNSCache();
							
							}
							else {
								lblStatus.setText("Unknown error. In Memorial Block not inserted");
							}
						}
					}
					else {
						lblStatus.setText(lastError);
					}
				}
			}
		});
		
	}
	
	public String findBlock() {
		File file = new File(strHostFilePath);

		try {
		    Scanner scanner = new Scanner(file);

		    //now read the file line by line...
		    int lineNum = 0;
		    int beginLine = -1;
		    int endLine = -1;
		    actualVersion = "";
		    while (scanner.hasNextLine()) {
		        String line = scanner.nextLine();
		        lineNum++;
		        if(line.equals(beginBlock)) { 
		        	actualVersion = scanner.nextLine();
		            beginLine = lineNum;
		            lineNum++;
		        }
		        else if(line.equals(endBlock)) {
		        	endLine = lineNum;
		        }
		        if ((beginLine > 0) && (endLine > 0)) {
		        	break;
		        }
		    }
		    scanner.close();
		    if ((beginLine >0) && (endLine > 0)) {
		    	return String.format("In Memoriam block found - Version: %s", actualVersion);
		    }
		    else {
		    	return "In Memoriam block not found";
		    }
		} catch(FileNotFoundException e) { 
		    return String.format("File %s not found", strHostFilePath);
		}		
	}
	
	public boolean removeBlock() {
		File file = new File(strHostFilePath);
		File tempFile = new File(file.getAbsolutePath() + ".tmp");
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(tempFile));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			lastError = "Error creating temp file";
			return false;
		}
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.equals(beginBlock)) {
					while (scanner.hasNextLine()) {
						String lineBlock = scanner.nextLine();
						if (lineBlock.equals(endBlock)) {
							break;
						}
					}
				}
				else {
					pw.println(line);
			        pw.flush();					
				}
			}
			pw.close();
			scanner.close();
			if (!file.delete()) {
		        lastError = "Could not delete original file";
		        return false;
		    } 
			if (!tempFile.renameTo(file)) {
		        lastError = "Could not rename temporal file";
				return false;
		    }		
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			lastError = "Error reading original file";
			return false;
		}
	}
	
	private boolean getIPList() {
		URL url;
		try {
			url = new URL(urlIpList);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lastError = "Malformed URL";
			return false;
		}
		try {
			Scanner s = new Scanner(url.openStream());
			webIpList = new ArrayList<String>(); 
			while(s.hasNextLine()) {
				webIpList.add(s.nextLine());
			}
			s.close();
			if (webIpList.size() < 3) {
				webIpList = null;
				return false;
			}
			else {
				webVersion = webIpList.get(1);
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lastError = "Error downloading IP list";
			return false;
		}	
	}
	
	public boolean insertBlock() {
		if ((webIpList!=null) && (webIpList.size()>0)) {
			try
			{
			    FileWriter fw = new FileWriter(strHostFilePath,true); 
			    for (String line: webIpList) {
			    	fw.write(line + "\r\n");
			    }
			    fw.close();
			    return true;
			}
			catch(IOException ioe)
			{
				lastError = "Append error with host file";
				return false;
			    //System.err.println("IOException: " + ioe.getMessage());
			}
		}
		else {
			lastError ="There is no IP Block to install";
			return false;
		}		
	}
	
	private boolean execCommand(List<String> commands) {	
	    ProcessBuilder pb = new ProcessBuilder(commands);
	    Process process;
		try {
			process = pb.start();
		} catch (IOException e1) {
			lastError = "Error before executing: " + commands.toString();
			return false;
		}
	
	    //Check result
	    try {
			if (process.waitFor() == 0) {
			    return true;
			}
		} catch (InterruptedException e) {
			lastError = "Exception waiting for external command";
		}
	
	    //Abnormal termination
	    lastError = "Error after executing: " + commands.toString();
	    return false;
	}	
	
	public void cleanDNSCache() {
		List<String> ipconfigCmd = new ArrayList<String>();
		ipconfigCmd.add("ipconfig.exe");
		ipconfigCmd.add("/flushdns");
		if (isWindows) {
			if (!execCommand(ipconfigCmd)) {
				JOptionPane.showMessageDialog(null, "Restart computer for changes to take effect.\r\nError when trying to clear DNS cache:\r\n" + lastError);
				lastError = "";
			}
		}
		else { 
			JOptionPane.showMessageDialog(null, "Restart computer or clear DNS cache for changes to take effect.");			
		}
	}
}
