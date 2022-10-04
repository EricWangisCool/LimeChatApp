package tw.lime.www;
import javax.imageio.ImageIO; 
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.FileChooserUI;

import com.mysql.cj.log.Log;

import tw.lime.www.LoginPage.LimePicPanel;

import java.awt.*;  
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.Exception;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;  
  
class AddAccountPage extends JFrame { 
	private Connection conn;
	  
    AddAccountPage(Connection conn){
    	super("Account Registration");
        setLayout(new BorderLayout());
        this.conn = conn;
        
        addAccountArea addaccountarea = new addAccountArea();
        LimePicPanel limePic = new LimePicPanel();
        
        // ---------Set background color---------
        limePic.setBackground(Color.white);
        addaccountarea.newPanel.setBackground(Color.white);
          
        // ---------Put into JFrame---------
        add(limePic, BorderLayout.CENTER);
        add(addaccountarea.newPanel, BorderLayout.SOUTH);  
        
        // ---------Set JFrame Attribute---------
        setSize(390,450);
        setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
       
	
	private class addAccountArea extends JPanel{ 
	    JButton b1, b2;
	    JPanel newPanel;  
	    JLabel accountLabel, passLabel, userNameLabel, headPicLabel;  
	    final JTextField  textField1, textField2, textField3; 
	    private File selectedPic;
	    private InputStream selectedHeadPic;
    	
    	private addAccountArea() { 
    		// ---------Account Area---------
    		accountLabel = new JLabel("Account");  
    		passLabel = new JLabel("Password");  
    		userNameLabel = new JLabel("User Name"); 
    		headPicLabel = new JLabel("Head Picture"); 
    		
    		
            textField1 = new JTextField(15);
            textField2 = new JTextField(15);
            textField3 = new JTextField(15);  
               
            b1 = new JButton("Create Account");
            b2 = new JButton("Choose (Max.100*100)");
            
            
            
            // ---------Put Above objects into JPanel--------- 
            newPanel = new JPanel(new GridLayout(5, 1));  
            newPanel.add(accountLabel);    
            newPanel.add(textField1);  
            
            newPanel.add(passLabel);    
            newPanel.add(textField2);
            
            newPanel.add(userNameLabel);    
            newPanel.add(textField3);
            
            newPanel.add(headPicLabel);
            newPanel.add(b2);
            
            newPanel.add(b1);
            
            
            b1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String account = textField1.getText();
					String passW = textField2.getText();
					String userName = textField3.getText();
					OftenUsedUtil.createAccount(account, passW, userName, selectedHeadPic, conn);
				}
			}); 
            
            b2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser jf = new JFileChooser();
					int returnValue = jf.showOpenDialog(null);
					if(returnValue==JFileChooser.APPROVE_OPTION) {
						selectedPic = jf.getSelectedFile();
						try {
							selectedHeadPic = new FileInputStream(selectedPic);
						} catch (FileNotFoundException e1) {
							System.out.println(e.toString());
						}
						
					}
				}
			});
            
		}
    	
	}
		
} 
