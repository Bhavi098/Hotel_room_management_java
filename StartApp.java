package hotelmanagementsystem;

public class StartApp {
	public static void main(String[] args) {
		try {
			// Establish database connection
			DBHotel.dbConnect();
			// Start the interactive UI
			UIHotel.startApp();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Disconnect from the database
			try {
				DBHotel.dbDisconnect();
			} catch (Exception e) {
				System.err.println("Error disconnecting: " + e.getMessage());
			}
		}
	}
}
