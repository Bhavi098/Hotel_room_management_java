package hotelmanagementsystem;

import java.sql.*;

public class DBHotel {
	// Static connection used throughout the application
	static Connection con = null;

	/**
	 * Establishes a connection to the database.
	 */
	public static void dbConnect() throws ClassNotFoundException, SQLException {
		// Load MySQL JDBC Driver
		Class.forName("com.mysql.cj.jdbc.Driver");
		// Establish connection (update credentials if needed)
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel_management_project", "root", "");
		System.out.println("Database connected: " + con);
	}

	/**
	 * Closes the database connection if it exists.
	 */
	public static void dbDisconnect() throws SQLException {
		if (con != null) {
			con.close();
			con = null;
			System.out.println("Database disconnected.");
		} else {
			System.out.println("Database connection was already null; nothing to disconnect.");
		}
	}

	// ----------------- Room and Customer Methods ------------------

	public static String getRoomDetails() throws SQLException {
		String query = "SELECT r_no, r_details, r_price FROM rooms";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		String result = "+-------+-----------------------+-------+\n";
		result += "|Room No|Details\t\t|Price\t|\n";
		result += "+-------+-----------------------+-------+\n";
		while (rs.next()) {
			result += "|" + rs.getInt(1) + "\t" +
					"|" + rs.getString(2) + "\t" +
					"|" + rs.getInt(3) + "\t|\n";
		}
		result += "+-------+-----------------------+-------+";
		return result;
	}

	public static String getRoomDetailsWithAvailability() throws SQLException {
		String query = "SELECT r_no, r_details, r_price, r_availability FROM rooms";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		String result = "+-------+-----------------------+-------+---------------+\n";
		result += "|Room No|Details\t\t|Price\t|Availability\t|\n";
		result += "+-------+-----------------------+-------+---------------+\n";
		while (rs.next()) {
			result += "|" + rs.getInt(1) + "\t" +
					"|" + rs.getString(2) + "\t" +
					"|" + rs.getInt(3) + "\t" +
					"|" + rs.getString(4) + "\t|\n";
		}
		result += "+-------+-----------------------+-------+---------------+\n";
		return result;
	}

	public static boolean isRoomAvailable(int bookedRN) throws SQLException {
		String query = "SELECT r_availability FROM rooms WHERE r_no = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, bookedRN);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return rs.getString(1).equalsIgnoreCase("Available");
		}
		return false;
	}

	public static boolean ifRoomNoIsValid(int bookedRN) throws SQLException {
		String query = "SELECT r_no FROM rooms";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		while (rs.next()) {
			if (rs.getInt(1) == bookedRN) {
				return true;
			}
		}
		return false;
	}

	public static boolean bookRoom(int bookedRN, int bookedDays, String firstName, String lastName, int aadharNo) throws SQLException {
		boolean check = false;
		// Get room price from rooms table
		String priceQuery = "SELECT r_price FROM rooms WHERE r_no = ?";
		PreparedStatement ps = con.prepareStatement(priceQuery);
		ps.setInt(1, bookedRN);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			int roomPrice = rs.getInt(1);
			// Insert details into customers table
			String queryic = "INSERT INTO customers (c_first_name, c_last_name, c_aadhar_no, c_room_no, c_booked_days, c_balance_amt) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement psic = con.prepareStatement(queryic);
			psic.setString(1, firstName);
			psic.setString(2, lastName);
			psic.setInt(3, aadharNo);
			psic.setInt(4, bookedRN);
			psic.setInt(5, bookedDays);
			psic.setInt(6, roomPrice * bookedDays);
			int affectedRowic = psic.executeUpdate();

			// Update rooms table to mark the room as not available
			String queryir = "UPDATE rooms SET c_aadhar_no = ?, r_availability = ? WHERE r_no = ?";
			PreparedStatement psir = con.prepareStatement(queryir);
			psir.setInt(1, aadharNo);
			psir.setString(2, "Not Available");
			psir.setInt(3, bookedRN);
			int affectedRowir = psir.executeUpdate();

			if (affectedRowic > 0 && affectedRowir > 0) {
				check = true;
			}
		}
		return check;
	}

	// ----------------- User Login Methods ------------------

	public static boolean rnAndAnAuthenication(int roomNo, int aadharNo) throws SQLException {
		if (ifRoomNoIsValid(roomNo)) {
			String query = "SELECT c_aadhar_no FROM rooms WHERE r_no = ?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setInt(1, roomNo);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				int tableAadharNo = rs.getInt(1);
				return tableAadharNo == aadharNo;
			}
		}
		return false;
	}

	public static String forgotRN(int aadharNo) throws SQLException {
		String query = "SELECT r_no FROM rooms WHERE c_aadhar_no = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, aadharNo);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			int roomNo = rs.getInt(1);
			return "Your Room No is " + roomNo;
		}
		return "There is no Room Booked with this Aadhar Number!";
	}

	// ----------------- Food Ordering Methods ------------------

	public static String displayFoodMenu() throws SQLException {
		String query = "SELECT * FROM food_menu";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		String result = "+-------+-----------------------+-------+\n";
		result += "|ID\t|Food Item\t\t|Price\t|\n";
		result += "+-------+-----------------------+-------+\n";
		while (rs.next()) {
			result += "|" + rs.getInt(1) + "\t|";
			if (rs.getString(2).length() > 14) {
				result += rs.getString(2) + "\t|";
			} else {
				result += rs.getString(2) + "\t\t|";
			}
			result += rs.getInt(3) + "\t|\n";
		}
		result += "+-------+-----------------------+-------+\n";
		return result;
	}

	public static boolean isFoodItemIDValid(int itemID) throws SQLException {
		String query = "SELECT * FROM food_menu WHERE f_id = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, itemID);
		ResultSet rs = ps.executeQuery();
		return rs.next();
	}

	public static String addOrderToFoodOrderTable(int itemID, int itemQuantity, int roomNo) throws SQLException {
		// Fetch food item details
		String query = "SELECT * FROM food_menu WHERE f_id = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, itemID);
		ResultSet rs = ps.executeQuery();
		rs.next();
		String fName = rs.getString(2);
		int fPrice = rs.getInt(3);

		// Insert order details into food_order table
		String queryo = "INSERT INTO food_order (fid, room_no, fname, fprice, fqty, ftotal) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pso = con.prepareStatement(queryo);
		pso.setInt(1, itemID);
		pso.setInt(2, roomNo);
		pso.setString(3, fName);
		pso.setInt(4, fPrice);
		pso.setInt(5, itemQuantity);
		pso.setInt(6, fPrice * itemQuantity);
		pso.executeUpdate();

		String result = (fName.length() > 14)
				? "| " + fName + "\t| "
				: "| " + fName + "\t\t| ";
		result += fPrice + "\t| " + itemQuantity + "\t| " + (fPrice * itemQuantity) + "\t\t|\n";
		return result;
	}

	public static int totalPrice(int itemID, int itemQuantity) throws SQLException {
		String query = "SELECT * FROM food_menu WHERE f_id = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, itemID);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int fPrice = rs.getInt(3);
		return fPrice * itemQuantity;
	}

	public static String addTotalAmountInCustomerTable(int roomNo, int aadharNo, int totalAmount) throws SQLException {
		String query = "UPDATE customers SET c_balance_amt = (c_balance_amt + ?) WHERE (c_aadhar_no = ? AND c_room_no = ? AND c_balance_amt != 0)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, totalAmount);
		ps.setInt(2, aadharNo);
		ps.setInt(3, roomNo);
		int affectedRow = ps.executeUpdate();
		if (affectedRow == 1) {
			return "Thank You for Ordering!\nAmount will be Added to your Account!";
		} else {
			return "There was an issue while Adding amount to your Account.\nPlease Try Again Later!";
		}
	}

	// ----------------- Checkout Methods ------------------

	private static String RoomBill(int roomNo, int aadharNo) throws SQLException {
		String result = "";
		String query = "SELECT r_details, r_price FROM rooms WHERE r_no = ? AND c_aadhar_no = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, roomNo);
		ps.setInt(2, aadharNo);
		ResultSet rs = ps.executeQuery();
		rs.next();
		String roomDetails = rs.getString(1);
		int roomPrice = rs.getInt(2);

		String querydays = "SELECT c_booked_days FROM customers WHERE (c_aadhar_no = ? AND c_room_no = ? AND c_balance_amt != 0)";
		PreparedStatement psd = con.prepareStatement(querydays);
		psd.setInt(1, aadharNo);
		psd.setInt(2, roomNo);
		ResultSet rsd = psd.executeQuery();
		rsd.next();
		int daysBooked = rsd.getInt(1);

		result += "\n***************************************************************\n";
		result += "\t\t\tFINAL BILL\n";
		result += "\t\t\t----------\n";
		result += "\nRoom Costs: \n";
		result += "+-------+-------------------------------+-------+-------+-------+\n";
		result += "| Room\t| Details\t\t\t| Price\t| Days\t| Total\t|\n";
		result += "+-------+-------------------------------+-------+-------+-------+\n";
		result += "| " + roomNo + "\t";
		result += (roomDetails.length() < 21) ? "| " + roomDetails + "\t\t" : "| " + roomDetails + "\t";
		result += "| " + roomPrice + "\t";
		result += "| " + daysBooked + "\t";
		result += "| " + (roomPrice * daysBooked) + "\t|\n";
		result += "+-------+-------------------------------+-------+-------+-------+\n";
		return result;
	}

	private static String FinalFoodBill(int roomNo, int aadharNo) throws SQLException {
		String result = "";
		String queryf = "SELECT room_no, fname, fprice, SUM(fqty) AS qty, SUM(ftotal) AS total FROM food_order GROUP BY room_no, fname HAVING room_no = ?";
		PreparedStatement psf = con.prepareStatement(queryf);
		psf.setInt(1, roomNo);
		ResultSet rsf = psf.executeQuery();
		int foodBill = 0;
		result += "\nFood Costs: \n";
		result += "+-----------------------+-------+-------+---------------+\n";
		result += "| Food Item\t\t| Price\t| Qty\t| Subtotal\t|\n";
		result += "+-----------------------+-------+-------+---------------+\n";
		while (rsf.next()) {
			String fName = rsf.getString(2);
			int itemQuantity = rsf.getInt(4);
			int fPrice = rsf.getInt(3);
			int ftotal = rsf.getInt(5);
			result += (fName.length() > 14) ? "| " + fName + "\t| " : "| " + fName + "\t\t| ";
			result += fPrice + "\t| " + itemQuantity + "\t| " + ftotal + "\t\t|\n";
			foodBill += ftotal;
		}
		result += "+-----------------------+-------+-------+---------------+\n";
		result += "Your Overall Food Bill is Rs." + foodBill + "\n";
		return result;
	}

	public static String getFinalBill(int roomNo, int aadharNo) throws SQLException {
		String roomBill = RoomBill(roomNo, aadharNo);
		String finalFoodBill = FinalFoodBill(roomNo, aadharNo);
		return roomBill + finalFoodBill;
	}

	public static int grantTotalAmount(int roomNo, int aadharNo) throws SQLException {
		String query = "SELECT c_balance_amt FROM customers WHERE (c_aadhar_no = ? AND c_room_no = ? AND c_balance_amt != 0)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, aadharNo);
		ps.setInt(2, roomNo);
		ResultSet rs = ps.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	public static void removeAllCustomerDetails(int roomNo, int aadharNo) throws SQLException {
		removeFoodOrderHistory(roomNo);
		setCustomerBalanceToZero(roomNo, aadharNo);
		removeCusDetailsFromRoomsTable(roomNo);
	}

	private static void removeFoodOrderHistory(int roomNo) throws SQLException {
		String query = "DELETE FROM food_order WHERE room_no = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, roomNo);
		ps.executeUpdate();
	}

	private static void setCustomerBalanceToZero(int roomNo, int aadharNo) throws SQLException {
		String query = "UPDATE customers SET c_balance_amt = 0 WHERE (c_aadhar_no = ? AND c_room_no = ? AND c_balance_amt != 0)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, aadharNo);
		ps.setInt(2, roomNo);
		ps.executeUpdate();
	}

	private static void removeCusDetailsFromRoomsTable(int roomNo) throws SQLException {
		String query = "UPDATE rooms SET c_aadhar_no = NULL, r_availability = 'Available' WHERE r_no = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, roomNo);
		ps.executeUpdate();
	}
}
