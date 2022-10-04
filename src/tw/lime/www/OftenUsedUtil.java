package tw.lime.www;

import java.awt.Component;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.mysql.cj.util.StringUtils;

public class OftenUsedUtil {
//－－－－－－－－－－－－－－－－－－－－－－全頁用公式－－－－－－－－－－－－－－－－－－－－－－
	public static Connection connectDB() {
		Connection conn;
		try {
			// 套入MySQLconnector, 以防使用者的Java為舊版
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/LimeDB", "root", "root");
			return conn;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}

//－－－－－－－－－－－－－－－－－－－－－－LoginPage用公式－－－－－－－－－－－－－－－－－－－－－－
	public static void CheckAccount(String accountValue, String passValue, LoginPage lp, Connection conn) {
		// Check account through mySQL
		String sql = "SELECT * FROM AccountList WHERE Account = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, accountValue);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String EncryptPass = rs.getString("Passw");
				if (BCrypt.checkpw(passValue, EncryptPass)) {
					// CHECK_OK SEND_USERID_TO_FRIENDLIST
					String userid = rs.getString("UserId");
					String username = rs.getString("username");
					JOptionPane.showInternalMessageDialog(null, String.format("Hello, %s! ", username));
					new FriendListPage(userid, username);
					lp.dispose();
				} else {
					// CHECK_PASSW_ERROR
					JOptionPane.showInternalMessageDialog(null, "Wrong Account Password");
				}
			} else {
				// CHECK_ACCOUNT_ERROR
				JOptionPane.showInternalMessageDialog(null, "Wrong Account Password");
			}
		} catch (SQLException e) {
			// CHECK_EXCEPTION_ERROR
			System.out.println(e.toString());
		}
	}

//－－－－－－－－－－－－－－－－－－－－－－addAccountPage用公式－－－－－－－－－－－－－－－－－－－－－－
	public static void createAccount(String account, String passW, String userName, InputStream selectedHeadPic,
			Connection conn) {
		ResultSet rs;
		int rs2;
		PreparedStatement pt;

		if (account.equals("") || passW.equals("") || (userName.equals(""))) {
			JOptionPane.showInternalMessageDialog(null, "Please complete the required field");
		} else {
			try {
				// 先查這個account有沒有被使用過
				Statement stmt = conn.createStatement();
				String sql = String.format("SELECT * FROM AccountList WHERE account = '%s'", account);
				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					// 如果帳號有被使用過就跳出訊息
					JOptionPane.showInternalMessageDialog(null, "This account has been used");
				}
				// 如果帳號沒有被使用, 就可以新增會員
				String hashPass = BCrypt.hashpw(passW, BCrypt.gensalt());

				pt = conn.prepareStatement(
						"INSERT INTO `AccountList`(`Account`, `PassW`, `UserName`, `Headpic`) VALUES (?,?,?,?)");
				pt.setString(1, account);
				pt.setString(2, hashPass);
				pt.setString(3, userName);
				pt.setBlob(4, selectedHeadPic);

				rs2 = pt.executeUpdate();
				if (rs2 > 0) {
					JOptionPane.showInternalMessageDialog(null, "Account added successfully");

				} else {
					System.out.println("Bug in Adding account");
				}

			} catch (Exception e) {
				System.out.println(e.toString());
			}

		}

	}

//－－－－－－－－－－－－－－－－－－－－－－FriendListPage用公式－－－－－－－－－－－－－－－－－－－－－－
	public static class fModel extends DefaultTableModel {

		public fModel(String userId, Connection conn) {
			TreeSet<String> friendList = OftenUsedUtil.returnFriendList(userId, conn);
			ResultSet rs = OftenUsedUtil.returnfriendData(friendList, conn);
			try {
				Object[] data = new Object[4];
				while (rs.next()) {
					data[0] = rs.getString("userid");
					if (rs.getBytes("headpic") != null) {
						data[1] = new ImageIcon(rs.getBytes("headpic"));
					} else {
						data[1] = new ImageIcon(getClass().getResource("/Res/userIcon.png"));
					}
					data[2] = rs.getString("username");
					data[3] = "Chat";
					this.addRow(data);
				}
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 1)
				return ImageIcon.class;
			return Object.class;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		// SET COLUMNHEADERS' NAME
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Friend ID";
			case 1:
				return "Head Picture";
			case 2:
				return "Friend Name";
			case 3:
				return "Function";
			default:
				return "";
			}
		}
	}

	public static TreeSet<String> returnFriendList(String userId, Connection conn) {
		// 以TreeSet的資料型態回傳user的朋友
		try {
			String sql = "SELECT friendid FROM `FriendList` WHERE userid = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			TreeSet<String> tree = new TreeSet<>();
			while (rs.next()) {
				String friendid = rs.getString("friendid");
				tree.add(friendid);
			}
			return tree;
		} catch (SQLException e) {
			System.out.println(e.toString());
			return null;
		}
	}

	public static void addFriendAccount(String userId, String enterAccount, Connection conn) {
		ResultSet rs, rs2;
		int rs3;
		String friendId = "";
		// 先查這個account是否存在
		try {
			String sql = "SELECT userid FROM `accountlist` WHERE account = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, enterAccount);
			rs = ps.executeQuery();
			// 如果存在就檢查accountId是否存在於自己的friendList中
			if (rs.next()) {
				friendId = rs.getString("userid");
				String sql2 = "SELECT * FROM `friendlist` WHERE userid = ? AND friendid = ?";
				PreparedStatement ps2 = conn.prepareStatement(sql2);
				ps2.setString(1, userId);
				ps2.setString(2, friendId);
				rs2 = ps2.executeQuery();
				// 如果user已經加過這個account, 那就不能再加他, 以防別人封鎖他後還繼續騷擾
				if (rs2.next()) {
					JOptionPane.showInternalMessageDialog(null, String.format("You already added this account"));
					// 如果沒有, 就讓彼此互加朋友
				} else {
					String sql3 = "INSERT INTO `FriendList` (`UserId`, `FriendId`) VALUES (?, ?), (?, ?)";
					PreparedStatement ps3 = conn.prepareStatement(sql3);
					ps3.setString(1, userId);
					ps3.setString(2, friendId);
					ps3.setString(3, friendId);
					ps3.setString(4, userId);
					rs3 = ps3.executeUpdate();
					if (rs3 > 0) {
						JOptionPane.showInternalMessageDialog(null, String.format(
								"Friend added successfully! Tell your friend to refresh his/her page and start chatting!"));
					} else {
						System.out.println("There is bug inside adding friend");
					}
				}
			} else {
				JOptionPane.showInternalMessageDialog(null, String.format("This account doesn't exist"));
			}
		} catch (SQLException e) {
			System.out.println(e.toString());
		}

	}

	public static ResultSet returnfriendData(TreeSet<String> friendList, Connection conn) {
		// 將TreeSet的資料型態轉成物件陣列, 再從物件陣列轉成字串
		Object[] stringArray = friendList.toArray();
		StringBuilder sb = new StringBuilder();
		if (stringArray.length > 0) {
			for (int i = 0; i < stringArray.length; i++) {
				sb.append(i == 0 ? stringArray[i] : "," + stringArray[i]);
			}
		}
		String friends = sb.toString();
		// 將朋友字串拿來SQL搜尋資料
		try {
			String sql = "SELECT userid, headpic, username FROM `accountlist` WHERE find_in_set(userid, ?)"; // 經典中的經典
			PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			ps.setString(1, friends);
			return ps.executeQuery();
		} catch (SQLException e) {
			System.out.println(e.toString());
			return null;
		}
	}

//－－－－－－－－－－－－－－－－－－－－－－ChatRoom用公式－－－－－－－－－－－－－－－－－－－－－－
// 檢查這個user和這個friend是否存在聊天記錄, 有的話回傳聊天紀錄tableName, 沒有的話createTable給他們存放
	public static String checkChatTable(String userId, String friendId, Connection conn) {
		String tableName1 = String.format("%s,%schatHistory", userId, friendId);
		String tableName2 = String.format("%s,%schatHistory", friendId, userId);
		try {
			java.sql.DatabaseMetaData dbm = conn.getMetaData();
			ResultSet if1exist = dbm.getTables(null, null, tableName1, null);
			ResultSet if2exist = dbm.getTables(null, null, tableName2, null);
			if (if1exist.next()) {
				return tableName1;
			} else if (if2exist.next()) {
				return tableName2;
			} else {
				Statement stmt = conn.createStatement();
				String sql = String.format(
						"CREATE TABLE `LimeDB`.`%s`" + " ( `message` VARCHAR(100) NOT NULL ,"
								+ " `date` DATETIME on update CURRENT_TIMESTAMP NOT NULL ,"
								+ " `id` INT(20) NOT NULL AUTO_INCREMENT ," + " PRIMARY KEY (`id`)) ENGINE = InnoDB;",
						tableName1);
				stmt.executeUpdate(sql);
				return tableName1;
			}
		} catch (Exception e) {
			return e.toString();
		}
	}

	public static class chatModel extends DefaultTableModel {
		public int chatRows;
		public ResultSet chatHistory;

		public chatModel(String chatTableName) {
			chatRows = OftenUsedUtil.countChatRows(chatTableName);
			chatHistory = OftenUsedUtil.readChatHistory(chatTableName);
		}

		@Override
		public int getRowCount() {
			return chatRows;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			String ret = "";
			try {
				chatHistory.absolute(row + 1);
				ret = chatHistory.getString(column + 1);
			} catch (Exception e) {
				System.out.println(e.toString());
				ret = "";
			}
			return ret;
		}
	}

	public static int countChatRows(String chatTableName) {
		try {
			Connection conn = OftenUsedUtil.connectDB();
			Statement stmt = conn.createStatement();
			String sql = String.format("SELECT count(*) total FROM `%s`", chatTableName);
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			int rows = rs.getInt("total");
			return rows;
		} catch (Exception e) {
			System.out.println(e.toString());
			return 0;
		}
	}

	public static ResultSet readChatHistory(String chatTableName) {
		try {
			Connection conn = OftenUsedUtil.connectDB();
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String sql = String.format("SELECT * FROM `%s`", chatTableName);
			ResultSet rs = stmt.executeQuery(sql);
			return rs;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}

	public static void insertChat(String userName, String message, String chatTableName) {
		try {
			// 抓出當前時間
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dTime = sdf.format(now);
			// 組織對話訊息
			String dialog = String.format("%s: %s", userName, message);
			// 將聊天灌入DB
			Connection conn = OftenUsedUtil.connectDB();
			Statement stmt = conn.createStatement();
			String sql = String.format("INSERT INTO `%s` (`message`, `date`) " + "VALUES ('%s','%s')", chatTableName,
					dialog, dTime);
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println(e.toString());
		}

	}

}

// ADD BUTTON ON FRIENDLIST PAGE 
class ButtonRenderer extends JButton implements TableCellRenderer {

	// CONSTRUCTOR
	public ButtonRenderer() {
		// SET BUTTON PROPERTIES
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object obj, boolean selected, boolean focused, int row,
			int col) {
		// SET PASSED OBJECT AS BUTTON TEXT
		setText((obj == null) ? "" : obj.toString());
		return this;
	}
}