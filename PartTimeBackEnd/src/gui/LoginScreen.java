package gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.Image;

import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import sql_connection_core.MySqlConnector;

import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JEditorPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginScreen {

	private JFrame frame;
	private JLabel lblStudentJobServer;
	private JPanel panel;
	private JTextField textField;
	private JLabel lblPassword;
	private JPasswordField textField_1;

	
	//since admin user is does not contain any relationship to the database which means to server employer and student
	//we will just hard code this.
	private final String ADMIN_USERNAME = "admin";
	private final String ADMIN_PASSWORD = "admin";
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					try {
						MySqlConnector.connectDataBase("FYP_StudentJob");
						MainScreen.isSqlConnected = true;
						
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						MainScreen.isSqlConnected = false;
						
						e1.printStackTrace();
					}
					
					LoginScreen window = new LoginScreen();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoginScreen() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.decode("#353B59"));
		
		
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(1000, 900);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		panel.setSize(1000, 900);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		
		JLabel lblStudentJob = new JLabel();
		lblStudentJob.setHorizontalAlignment(SwingConstants.CENTER);
		lblStudentJob.setBounds(402, 6, 200, 220);
		lblStudentJob.setIcon(new ImageIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("studentJob.png")).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
		panel.add(lblStudentJob);
		lblStudentJob.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		
		textField = new JTextField();
		textField.setFont(new Font("Montserrat", Font.PLAIN, 15));
		textField.setBounds(254, 315, 464, 48);
		panel.add(textField);
		textField.setColumns(10);
		
		JLabel lblAdminUserName = new JLabel("Admin User Name");
		lblAdminUserName.setFont(new Font("Text Me One", Font.PLAIN, 20));
		lblAdminUserName.setBounds(255, 281, 157, 34);
		panel.add(lblAdminUserName);
		
		lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Text Me One", Font.PLAIN, 20));
		lblPassword.setBounds(255, 390, 157, 34);
		panel.add(lblPassword);
		
		textField_1 = new JPasswordField();
		textField_1.setFont(new Font("Montserrat", Font.PLAIN, 15));
		textField_1.setColumns(10);
		textField_1.setBounds(254, 428, 464, 48);
		panel.add(textField_1);
		
		
		
		JButton btnNewButton = new JButton("Login");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username =textField.getText(); 
				@SuppressWarnings("deprecation")
				String password = textField_1.getText();
				
				if(username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
					frame.dispose();
					MainScreen mainScreen = new MainScreen();
					mainScreen.setVisible(true);
				}else {
					//default title and icon
					JOptionPane.showMessageDialog(frame,
					    "Username Or Password is wrong. Please Try Again!.");
				}
			}
		});
		btnNewButton.setBounds(387, 507, 141, 67);
		btnNewButton.setBackground(Color.decode("#505ED8"));
		btnNewButton.setForeground(new Color(255, 255, 255));
		panel.add(btnNewButton);
		
		lblStudentJobServer = new JLabel("Student Job Server Application Admin Control Panel");
		lblStudentJobServer.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lblStudentJobServer, BorderLayout.NORTH);
		lblStudentJobServer.setForeground(Color.white);
		lblStudentJobServer.setFont(new Font("Text Me One", Font.PLAIN, 40));
		lblStudentJobServer.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
	}
}
