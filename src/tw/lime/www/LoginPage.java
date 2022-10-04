package tw.lime.www;
import javax.imageio.ImageIO; 
import javax.swing.*;

import com.mysql.cj.log.Log;

import java.awt.*;  
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.Exception;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;  
  
class LoginPage extends JFrame { 
	private LimePicPanel limepic;
	private EnterAccountPanel EnterAccountPanel;
	public static LoginPage lp;
	public  Connection conn;
	
	  
    LoginPage(){
    	super("Lime");
        setLayout(new BorderLayout());
        conn = OftenUsedUtil.connectDB(); 
        
        // ---------Create JPanel Object---------
        limepic = new LimePicPanel();
        EnterAccountPanel enterAccountPanel = new EnterAccountPanel();
        
        // ---------Set background color---------
        limepic.setBackground(Color.white);
        enterAccountPanel.newPanel.setBackground(Color.white);
          
        // ---------Put into JFrame---------
        add(limepic, BorderLayout.CENTER);
        add(enterAccountPanel.newPanel, BorderLayout.SOUTH);  
        
        // ---------Set JFrame Attribute---------
        setSize(390,450);
        setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
       
	
	private class EnterAccountPanel extends JPanel{ 
	    JButton b1, b2;  
	    JPanel newPanel;  
	    JLabel accountLabel, passLabel;  
	    final JTextField  textField1, textField2; 
    	
    	public EnterAccountPanel() { 
    		// ---------Account Area---------
    		accountLabel = new JLabel();  
    		accountLabel.setText("Account");  
            textField1 = new JTextField(15); // Set length for the account   
            
      
            
            // ---------Password Area---------
            passLabel = new JLabel();  
            passLabel.setText("Password"); 
            textField2 = new JPasswordField(15);     
              
            
            // ---------Login button---------  
            b1 = new JButton("Login");
            b2 = new JButton("Create Account");
            
            
            // ---------Put Above objects into JPanel--------- 
            newPanel = new JPanel(new GridLayout(3, 1));  
            newPanel.add(accountLabel);    
            newPanel.add(textField1);   
            newPanel.add(passLabel);    
            newPanel.add(textField2);   
            newPanel.add(b1);  
            newPanel.add(b2);  
            
            
            b1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String accountValue = textField1.getText();        
				    String passValue = textField2.getText();
				    OftenUsedUtil.CheckAccount(accountValue, passValue, lp, conn); 
				}
			});     
            
            b2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AddAccountPage(conn);
				}
			});
            
		}
    	
	}
		
	public static class LimePicPanel extends JPanel {
		private BufferedImage LimePic;
		
		LimePicPanel(){
			try {
				LimePic = ImageIO.read(getClass().getResource("/Res/Lime.png"));
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(LimePic, 98, 50, null);
		}
		 
	}
	
	
	// ---------程式執行進入點--------- 
	public static void main(String[] args) {
		// 可以用此帳密登入做測試 (帳號: Eric1234, 密碼: 1234)
		lp = new LoginPage();
	}
		
} 