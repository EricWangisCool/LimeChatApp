package tw.lime.www;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import tw.lime.www.OftenUsedUtil.chatModel;

public class ChatRoom extends JFrame{
	private JButton send;
	private JPanel chat;
	public JTable jtable;
	private JScrollPane jsp;
	private JTextArea textArea;
	public String chatTableName;
	public Connection conn;
	
	ChatRoom(String userId, String userName, String friendId, String friendName, Connection conn){
		super(String.format("You and %s ChatRoom", friendName)); 
		this.conn = conn;
		setLayout(new BorderLayout());
		
		chatTableName = OftenUsedUtil.checkChatTable(userId, friendId, conn);
		
		// 從DB拉出聊天記錄放入JFrame裡
		chatModel cm = new chatModel(chatTableName);
		jtable = new JTable(cm);
		JScrollPane jsp = new JScrollPane(jtable);
		add(jsp, BorderLayout.CENTER);
		
		
		// 文字輸入欄Jpanel放入JFrame裡
		chat = new JPanel(new BorderLayout());
		textArea = new JTextArea();
		send = new JButton("Send");
		
		chat.setBackground(new java.awt.Color(0, 205, 102) );
		textArea.setBackground(new java.awt.Color(0, 205, 102) );
		
		chat.add(send, BorderLayout.EAST);
		chat.add(textArea, BorderLayout.CENTER);
		add(chat, BorderLayout.SOUTH);
		
		// send button增設功能
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OftenUsedUtil.insertChat(userName, textArea.getText(), chatTableName);	
				textArea.setText("");
			}
		});
		
		setSize(390,700);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		Timer timer = new Timer();
		LoadChatTasks loadchats = new LoadChatTasks(); 
		timer.schedule(loadchats, 0, 1 * 1000);
	}
	
	public class LoadChatTasks extends TimerTask {
		@Override
		public void run() {
			jtable.setModel(new chatModel(chatTableName));
		}
		
	}
	
}
