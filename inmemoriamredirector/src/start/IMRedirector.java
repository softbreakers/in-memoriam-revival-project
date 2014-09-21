package start;
import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Window.Type;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class IMRedirector {

	private JFrame frmInMemoriamRevival;
	
	private static String strHostFilePath = "c:\\windows\\system32\\drivers\\etc\\hosts";
	
	private final String beginBlock = "# IN MEMORIAL REVIVAL PROJECT BEGIN BLOCK";
	private final String endBlock   = "# IN MEMORIAL REVIVAL PROJECT END BLOCK";
	private final String urlIpList = "http://inmemoriam.softbreakers.com/iplist.txt";
	
	private int beginLine = -1, endLine = -1;
	private String actualVersion = "";
	
	private String webVersion = "";
	private List<String> webIpList = null;
	
	private String lastError = "";

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
		frmInMemoriamRevival.setType(Type.UTILITY);
		frmInMemoriamRevival.setResizable(false);
		frmInMemoriamRevival.setTitle("In Memoriam Revival Project");
		frmInMemoriamRevival.setBounds(100, 100, 272, 88);
		frmInMemoriamRevival.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmInMemoriamRevival.getContentPane().setLayout(null);
		
		final JButton btnInstall = new JButton("Install");
		btnInstall.setBounds(10, 11, 89, 23);
		frmInMemoriamRevival.getContentPane().add(btnInstall);
		
		final JButton btnUninstall = new JButton("Uninstall");
		btnUninstall.setBounds(164, 11, 89, 23);
		frmInMemoriamRevival.getContentPane().add(btnUninstall);
		
		final JLabel lblStatus = new JLabel("Scanning...");
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblStatus.setBounds(0, 45, 266, 14);
		frmInMemoriamRevival.getContentPane().add(lblStatus);
		
		lblStatus.setText(findBlock());
		
		btnUninstall.setEnabled(!actualVersion.isEmpty());
		
		if (getIPList()) {
			btnInstall.setEnabled(!actualVersion.equals(webVersion));
			btnInstall.setText(actualVersion.isEmpty() ? "Install":"Update");
		}
		else {
			lblStatus.setText(lastError);
			lastError = "";
			btnInstall.setEnabled(false);
		}
		
		btnUninstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lastError = "";
				if (!actualVersion.isEmpty()) {
					if (removeBlock()) {
						lblStatus.setText("In Memoriam Block removed");
						btnUninstall.setEnabled(false);
						btnInstall.setEnabled(true);
						btnInstall.setText("Install");
					}
					else {
						lblStatus.setText(lastError);
						lastError = "";
					}
				}
			}
		});
		
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
		    beginLine = -1;
		    endLine = -1;
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
}
