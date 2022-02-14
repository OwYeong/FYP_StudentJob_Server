package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import communication_section.ServerCore;
import sql_connection_core.MySqlConnector;

import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.TextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.event.ActionEvent;

public class MainScreen extends JFrame {

	private JPanel contentPane;
	private static MainScreen frame;
	
	public static boolean isSqlConnected = false;

	private static TextArea textArea;
	private String logText = "";

	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				
				try {
					
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					frame = new MainScreen();
					frame.setSize(1000, 900);
					frame.setVisible(true);
					
					if(isSqlConnected == false) {
						addLogText("ERROR: Unable To Connect SQL Database to Database Name : FYP_StudentJob, please try again");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainScreen() {
		
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 900);
		
		
		
		contentPane = new JPanel();
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textArea = new TextArea();
		textArea.setBounds(29, 100, 920, 561);
		textArea.setEditable(false);
		contentPane.add(textArea);
		
		JLabel lblServerLog = new JLabel("Server Log");
		lblServerLog.setFont(new Font("Text Me One", Font.PLAIN, 25));
		lblServerLog.setBounds(28, 34, 303, 65);
		contentPane.add(lblServerLog);
		
		JButton btnStartServer = new JButton("Start Server");
		btnStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				Thread t = new Thread() {
					public void run() {
						
						if(isSqlConnected) {
							try {
								ServerCore.startAcceptRequest();
								
								
								
								
								
							}catch(IOException ioException) {
								JOptionPane.showMessageDialog(frame,
									    "Server Start Error. Please Restart The Program.");
								
							}
						}else {
							//default title and icon
							JOptionPane.showMessageDialog(frame,
							    "Please Connect To The Database First");
							
						}
							
							
						
					}
				}; 	
				t.start();
				
				btnStartServer.setBackground(Color.green);
				btnStartServer.setText("Server Running");
				btnStartServer.setEnabled(false);
			}
		});
		btnStartServer.setBounds(309, 696, 169, 78);
		contentPane.add(btnStartServer);
		
		JButton button = new JButton("Connect To Database");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					MySqlConnector.connectDataBase("FYP_StudentJob");
					isSqlConnected = true;
					
					addLogText("Successfully Connect SQL Database to Database Name : FYP_StudentJob");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					isSqlConnected = false;
					JOptionPane.showMessageDialog(frame,
						    "Please Ensure You have already Imported the Database FYP_StudentJob.");
					addLogText("ERROR: Unable To Connect SQL Database to Database Name : FYP_StudentJob");
					e1.printStackTrace();
				}
				
				if(isSqlConnected) {
					button.setBackground(Color.green);
					button.setText("Connected");
					button.setEnabled(false);
				}
			}
		});
		button.setBounds(29, 696, 169, 78);
		contentPane.add(button);
		
		
		
	}
	public static void addLogText(String Message) {
		
		String currentText = textArea.getText();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        String newError = formatter.format(new Date()) + " " +  Message + "\n";
        String newTextToAppend = newError + "\n" + currentText;
        textArea.setText(newTextToAppend);
		
		
	}
}
