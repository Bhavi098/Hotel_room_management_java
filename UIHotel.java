package hotelmanagementsystem;

import java.sql.SQLException;
import java.util.Scanner;

public class UIHotel {

	public static void startApp() throws SQLException {
		Scanner scn = new Scanner(System.in);
		boolean exit = false;
		System.out.println("******** WELCOME TO HOTEL TRIVAGO *******");

		while (!exit) {
			System.out.println("\n*****************************************");
			System.out.println("How may we help you?");
			System.out.println("1. I want to Book a Room.");
			System.out.println("2. I want to Login.");
			System.out.println("3. Quit.");
			System.out.print("\nEnter your Choice: ");

			int choice = scn.nextInt();
			scn.nextLine(); // consume newline

			switch (choice) {
				case 1:
					O1BookARoom(scn);
					break;
				case 2:
					O2UserLogin(scn);
					break;
				case 3:
					exit = true;
					break;
				default:
					System.out.println("Please give valid Input!");
			}
		}
		scn.close();
	}

	private static void O1BookARoom(Scanner scn) throws SQLException {
		boolean back = false;
		while (!back) {
			System.out.println("\n*****************************************");
			System.out.println("Select Operation to Perform: ");
			System.out.println("1. Show all Rooms.");
			System.out.println("2. Book a Room.");
			System.out.println("3. Go Back");
			System.out.print("\nEnter your Choice: ");
			int choice1 = scn.nextInt();
			scn.nextLine(); // consume newline

			switch (choice1) {
				case 1:
					System.out.println("\nDISPLAYING ALL ROOMS:");
					System.out.println(DBHotel.getRoomDetails());
					break;
				case 2:
					System.out.println("\nDISPLAYING ALL ROOMS with Availability:");
					System.out.println(DBHotel.getRoomDetailsWithAvailability());
					System.out.print("Enter Room Number you want to Book: ");
					int bookedRN = scn.nextInt();
					scn.nextLine();
					if (DBHotel.ifRoomNoIsValid(bookedRN)) {
						if (DBHotel.isRoomAvailable(bookedRN)) {
							System.out.print("Number of Days you want to Book the Room: ");
							int bookedDays = scn.nextInt();
							scn.nextLine();
							System.out.print("Enter Your First Name: ");
							String firstName = scn.nextLine();
							System.out.print("Enter Your Last Name: ");
							String lastName = scn.nextLine();
							System.out.print("Enter Your Aadhar Number [Without Spaces]: ");
							int aadharNo = scn.nextInt();
							scn.nextLine();
							System.out.print("Do you want to confirm Your Booking? (Y/N): ");
							char bookingConfirm = scn.next().toLowerCase().charAt(0);
							scn.nextLine();

							if (bookingConfirm != 'n') {
								boolean isBookingConfirm = DBHotel.bookRoom(bookedRN, bookedDays, firstName, lastName, aadharNo);
								if (isBookingConfirm) {
									System.out.println("\nHi " + firstName + ", Your Desired Room has been Successfully Booked!");
									return;
								} else {
									System.out.println("Sorry! Your Booking was Unsuccessful!");
								}
							} else {
								System.out.println("Booking Cancelled!");
							}
						} else {
							System.out.println("Room No " + bookedRN + " is Not Available.");
						}
					} else {
						System.out.println("Please Enter a Valid Room Number!");
					}
					break;
				case 3:
					back = true;
					break;
				default:
					System.out.println("Please give valid Input!");
			}
		}
	}

	private static void O2UserLogin(Scanner scn) throws SQLException {
		boolean back = false;
		while (!back) {
			System.out.println("\n*****************************************");
			System.out.println("Select Operation to Perform:");
			System.out.println("1. Login with Room Number and Aadhar Number");
			System.out.println("2. Forgot Room Number");
			System.out.println("3. Go Back");
			System.out.print("\nEnter your Choice: ");
			int choice = scn.nextInt();
			scn.nextLine();
			switch (choice) {
				case 1:
					O2O1EnterRNAndAN(scn);
					break;
				case 2:
					O2O2ForgotRN(scn);
					break;
				case 3:
					back = true;
					break;
				default:
					System.out.println("Please give valid Input!");
			}
		}
	}

	private static void O2O1EnterRNAndAN(Scanner scn) throws SQLException {
		System.out.println("\n*****************************************");
		System.out.print("Enter Room Number: ");
		int roomNo = scn.nextInt();
		//scn.nextLine();
		System.out.print("Enter Aadhar Number [Without Spaces]: ");
		int aadharNo = scn.nextInt();
		//int aadharNo=Integer.parseInt(adharNo);
		//scn.nextLine();

		if (DBHotel.rnAndAnAuthenication(roomNo, aadharNo)) {
			System.out.println("\n\tLogin Successful!");
			boolean loggedIn = true;
			while (loggedIn) {
				System.out.println("\n*****************************************");
				System.out.println("Select Operation to Perform:");
				System.out.println("1. Order Food.");
				System.out.println("2. Check Out.");
				System.out.println("3. Go Back.");
				System.out.print("\nEnter your Choice: ");
				int choice = scn.nextInt();
				scn.nextLine();
				switch (choice) {
					case 1:
						orderFood(roomNo, aadharNo, scn);
						break;
					case 2:
						checkout(roomNo, aadharNo, scn);
						loggedIn = false;  // Exit after checkout
						break;
					case 3:
						loggedIn = false;
						break;
					default:
						System.out.println("Please give valid Input!");
				}
			}
		} else {
			System.out.println("Please Enter Valid Details!");
		}
	}

	private static void O2O2ForgotRN(Scanner scn) throws SQLException {
		System.out.print("Enter Your Aadhar Number [Without Spaces]: ");
		int aadharNo = scn.nextInt();
		scn.nextLine();
		System.out.println(DBHotel.forgotRN(aadharNo));
	}

	private static void orderFood(int roomNo, int aadharNo, Scanner scn) throws SQLException {
		String totalOrder = "";
		int totalAmount = 0;
		boolean ordering = true;
		while (ordering) {
			System.out.println(DBHotel.displayFoodMenu());
			System.out.print("Enter Item ID: ");
			int itemID = scn.nextInt();
			scn.nextLine();
			if (DBHotel.isFoodItemIDValid(itemID)) {
				System.out.print("Enter Item Quantity: ");
				int itemQuantity = scn.nextInt();
				scn.nextLine();
				String curOrder = DBHotel.addOrderToFoodOrderTable(itemID, itemQuantity, roomNo);
				totalOrder += curOrder;
				int curAmount = DBHotel.totalPrice(itemID, itemQuantity);
				totalAmount += curAmount;
				System.out.print("Order More items ? [ Y / N ]: ");
				char ans = scn.next().toLowerCase().charAt(0);
				scn.nextLine();
				if (ans == 'n') {
					System.out.println("+-----------------------+-------+-------+---------------+");
					System.out.println("| Food Item\t\t| Price\t| Qty\t| Subtotal\t|");
					System.out.println("+-----------------------+-------+-------+---------------+");
					System.out.print(totalOrder);
					System.out.println("+-----------------------+-------+-------+---------------+");
					System.out.println("Total Amount: Rs." + totalAmount);
					System.out.println(DBHotel.addTotalAmountInCustomerTable(roomNo, aadharNo, totalAmount));
					ordering = false;
				}
			} else {
				System.out.println("Please Enter Valid Food ID!");
			}
		}
	}

	private static void checkout(int roomNo, int aadharNo, Scanner scn) throws SQLException {
		System.out.println(DBHotel.getFinalBill(roomNo, aadharNo));
		int grantTotalAmount = DBHotel.grantTotalAmount(roomNo, aadharNo);
		System.out.println("Grand Total to be Paid before Checkout: Rs." + grantTotalAmount);
		boolean paying = true;
		while (paying) {
			System.out.print("Enter the Amount to Pay: ");
			int payment = scn.nextInt();
			scn.nextLine();
			if (payment == grantTotalAmount) {
				System.out.println("\n*****************************************");
				System.out.println("Thank you for your Payment!\nPlease visit us Again!");
				System.out.println("\n*****************************************");
				DBHotel.removeAllCustomerDetails(roomNo, aadharNo);
				paying = false;
			} else {
				System.out.println("Please Enter the Exact Amount: Rs." + grantTotalAmount);
			}
		}
	}
}
