package tw.lime.www;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Timer;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import tw.lime.www.OftenUsedUtil.chatModel;
import tw.lime.www.OftenUsedUtil.fModel;

public class FriendListPage extends JFrame {
	private fModel fm;
	private JTable jtable;
	private JScrollPane jsp;
	private String userId, userName;
	private 
	final static Connection conn = OftenUsedUtil.connectDB();

	public FriendListPage(String userId, String userName) {
		super("FriendList");
		this.userId = userId;
		this.userName = userName;
		setLayout(new BorderLayout());

		// 將朋友ID和名字製作成Jtable並塞入JFrame內
		fm = new fModel(userId, conn);
		jtable = new JTable(fm);
		jtable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
		jtable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JTextField()));
		jtable.setRowHeight(100);
		// 將朋友ID隱藏
		jtable.getColumnModel().getColumn(0).setMinWidth(0);
		jtable.getColumnModel().getColumn(0).setMaxWidth(0);
		jtable.getColumnModel().getColumn(0).setWidth(0);
		
		JTableHeader header = jtable.getTableHeader();
		header.setBackground(new java.awt.Color(0, 205, 102));
		jsp = new JScrollPane(jtable);
		add(jsp, BorderLayout.CENTER);
		
		// ---------add friend field---------
		JLabel addFriendLabel = new JLabel("Friend Account:");
        JTextField friendAccountTextField = new JTextField(15); // Set length for the account
        JButton addFriendbtn = new JButton("Add or Refresh");
        
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.add(addFriendLabel);
        panel.add(friendAccountTextField);
        panel.add(addFriendbtn);
        panel.setBackground(new java.awt.Color(0, 205, 102));
        add(panel, BorderLayout.SOUTH);
        
        addFriendbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String enterAccount = friendAccountTextField.getText();
				if(enterAccount.equals("")) {
					refreshJtable();
				}else {
					OftenUsedUtil.addFriendAccount(userId,enterAccount, conn);
					refreshJtable();
				}
			}
		});
        
		// 設置Jframe屬性
		setSize(390, 700);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private void refreshJtable() {
		jtable.setModel(new fModel(userId, conn));
		jtable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
		jtable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JTextField()));
		jtable.getColumnModel().getColumn(0).setMinWidth(0);
		jtable.getColumnModel().getColumn(0).setMaxWidth(0);
		jtable.getColumnModel().getColumn(0).setWidth(0);
	}

	
	// ADDING BUTTON FOR STRING "CHAT"
	class ButtonEditor extends DefaultCellEditor {
		protected JButton btn;
		private String lbl;
		private Boolean clicked;

		public ButtonEditor(JTextField txt) {
			super(txt);

			btn = new JButton();
			btn.setOpaque(true);
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Find friendId and create Chatroom
					int btnRow = jtable.getSelectedRow();
					String friendId = jtable.getValueAt(btnRow, 0).toString();
					String friendName = jtable.getValueAt(btnRow, 2).toString();
					new ChatRoom(userId, userName, friendId, friendName, conn);
					refreshJtable();
				}
			});
		}

		// OVERRIDE A COUPLE OF METHODS
		@Override
		public Component getTableCellEditorComponent(JTable table, Object obj, boolean selected, int row, int col) {

			// SET TEXT TO BUTTON,SET CLICKED TO TRUE,THEN RETURN THE BTN OBJECT
			lbl = (obj == null) ? "" : obj.toString();
			btn.setText(lbl);
			clicked = true;
			return btn;
		}

		// IF BUTTON CELL VALUE CHNAGES,IF CLICKED THAT IS
		@Override
		public Object getCellEditorValue() {

			if (clicked) {
				// SHOW US SOME MESSAGE
				// JOptionPane.showMessageDialog(btn, lbl+" Clicked");
			}
			// SET IT TO FALSE NOW THAT ITS CLICKED
			clicked = false;
			return new String(lbl);
		}

		@Override
		public boolean stopCellEditing() {

			// SET CLICKED TO FALSE FIRST
			clicked = false;
			return super.stopCellEditing();
		}

		@Override
		protected void fireEditingStopped() {
			// TODO Auto-generated method stub
			super.fireEditingStopped();
		}
	}


}
