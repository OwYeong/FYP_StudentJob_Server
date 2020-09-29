package gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;

import communication_section.ServerCore;
import database.DatabaseController;
import database.variable.Query;
import database.variable.Table;
import sql_connection_core.MySqlConnector;

import javax.swing.JButton;

public class Test {

	private JFrame frame;
	private JTextField txtHelloWorld;
	private JButton btnStartConnection;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test window = new Test();
					window.frame.setVisible(true);
					
					MySqlConnector.connectDataBase("clinicapp");
					
					Thread t = new Thread() {
						public void run() {
							try {
								ServerCore.startAcceptRequest();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}; 	
					t.start();
					
					
					
					
				    Table user = DatabaseController.getTable("user");
				    user.printTable();
				    
				    
				    
				    
				    
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Test() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 434, 30);
		frame.getContentPane().add(panel);
		
		txtHelloWorld = new JTextField();
		txtHelloWorld.setEditable(false);
		txtHelloWorld.setText("Hello World");
		panel.add(txtHelloWorld);
		txtHelloWorld.setColumns(10);
		
		btnStartConnection = new JButton("Start Connection");
		btnStartConnection.setBounds(258, 30, 113, 231);
		btnStartConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				try {
            		System.out.println("Start accepting client connection");
            		//handle_connection.startHandleConnection();
            		
//            		String[] value = {"(5, 'pro', 'asd', '012-1254545' )", 
//            				"(6, 'hehe', 'asasdd', '013-1254545' )", 
//            				"(7, 'hihi', 'afgsasdd', '017-1254545' )"} ;
//            		
//            		DatabaseController.insertDataIntoTable("patient", value);
            		
            		//String[] set = {"name='Apple'", "note='i am apple'"};
            		
            		
            		//DatabaseController.updateDataFromTable("patient", set, "id=1");
            		
            		String[] select = {"*"};
            		String[] table = {"patient", "noob"};
            		String[] condition = {"patient.id=noob.id", "name='apple'"};
            		
            		
            		Query combine = DatabaseController.getQuery(select, table, condition);
            		System.out.println("empty is " + combine.EmptyOrNot());
				    combine.printQuery();

            	}catch(Exception ex)
            	{
            		ex.printStackTrace();
            	}
				
			}
		});
		
		frame.getContentPane().add(btnStartConnection);
		
		JButton btnSqlTest = new JButton("SQL test");
		btnSqlTest.setBounds(10, 88, 100, 69);
		
		btnSqlTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				try {
            		//System.out.println("Start s connection");
            		//handle_connection.startHandleConnection();
            		//DatabaseController.deleteDataFromTable("patient", "name='pro'");
            		//sent_connection.startConnection();
					Table u = DatabaseController.getTable("user");
					String[] c = {"privallege"};
					
					String[] search = {"user"};
					u.searchInTable( c , search );

            	}catch(Exception ex)
            	{
            		ex.printStackTrace();
            	}
			}
		});
		frame.getContentPane().add(btnSqlTest);
	}
}
