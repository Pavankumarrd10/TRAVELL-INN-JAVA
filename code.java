import java.util.*;

/**
 * TravelApp.java
 * Improved, cleaned-up Java translation of the provided C++ OOPS project.
 *
 * Single-file runnable program for console.
 * Features:
 *  - Users: Customer & Admin (inheritance + polymorphism)
 *  - Register & Login
 *  - Hotels and Transportation (Vehicle)
 *  - Booking with State pattern (Pending, Confirmed, Cancelled)
 *  - Payment & refunds
 *  - Exception handling for admin-only operations
 *
 * Compile: javac TravelApp.java
 * Run:     java TravelApp
 */

public class TravelApp {
    // ----- Custom exception -----
    static class ApplicationException extends Exception {
        private final int errNo;
        public ApplicationException(int errNo, String msg) {
            super(msg);
            this.errNo = errNo;
        }
        public int getErrNo() { return errNo; }
    }

    // ----- Users -----
    static abstract class User {
        protected int userId;
        protected String name;
        protected String email;
        protected String password;
        protected String location;

        public User(int userId, String name, String email, String location, String password) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.location = location;
            this.password = password;
        }

        public int getUserId() { return userId; }
        public String getPassword() { return password; }
        public abstract void printDetails();
        public abstract void updateProfile(Scanner sc);

        public void generateReceipt() {
            System.out.println("----- Receipt -----");
            System.out.printf("Name: %s\nEmail: %s\nLocation: %s\n", name, email, location);
        }
    }

    static class Customer extends User {
        public Customer(int id, String name, String email, String location, String password) {
            super(id, name, email, location, password);
        }
        @Override
        public void printDetails() {
            System.out.printf("[Customer] %d : %s : %s : %s\n", userId, name, email, location);
        }
        @Override
        public void updateProfile(Scanner sc) {
            System.out.print("Change password? (y/n): ");
            String ch = sc.nextLine().trim();
            if (ch.equalsIgnoreCase("y")) {
                System.out.print("Enter new password: ");
                String p = sc.nextLine().trim();
                this.password = p;
                System.out.println("Password updated.");
            }
        }
    }

    static class Admin extends User {
        public Admin(int id, String name, String email, String location, String password) {
            super(id, name, email, location, password);
        }
        @Override
        public void printDetails() {
            System.out.printf("[Admin] %d : %s : %s : %s\n", userId, name, email, location);
        }
        @Override
        public void updateProfile(Scanner sc) {
            System.out.print("Change password? (y/n): ");
            String ch = sc.nextLine().trim();
            if (ch.equalsIgnoreCase("y")) {
                System.out.print("Enter new password: ");
                String p = sc.nextLine().trim();
                this.password = p;
                System.out.println("Password updated.");
            }
        }
    }

    // ----- Register/Login helper -----
    static class Register {
        public void signUp(List<User> users, Scanner sc, int assignedId, String password) {
            System.out.println("=== Sign Up ===");
            System.out.print("Enter name: ");
            String name = sc.nextLine().trim();
            System.out.print("Enter email: ");
            String email = sc.nextLine().trim();
            System.out.print("Enter location: ");
            String location = sc.nextLine().trim();
            Customer c = new Customer(assignedId, name, email, location, password);
            users.add(c);
            System.out.println("Signed up successfully with userId: " + assignedId);
        }

        public int login(List<User> users, Scanner sc) {
            System.out.println("=== Login ===");
            System.out.print("Enter userId: ");
            String idStr = sc.nextLine().trim();
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid id format.");
                return -1;
            }
            System.out.print("Enter password: ");
            String pw = sc.nextLine().trim();
            for (User u : users) {
                if (u.getUserId() == id && u.getPassword().equals(pw)) {
                    System.out.println("Login successful.");
                    return id;
                }
            }
            System.out.println("No such user. Do you want to sign up with this id? (y/n)");
            String ch = sc.nextLine().trim();
            if (ch.equalsIgnoreCase("y")) {
                signUp(users, sc, id, pw);
                return id;
            }
            return -1;
        }
    }

    // ----- Hotel -----
    static class Hotel {
        int hotelId;
        String name;
        String location;
        int rating;
        double pricePerRoom;

        public Hotel(int hotelId, String name, String location, int rating, double price) {
            this.hotelId = hotelId;
            this.name = name;
            this.location = location;
            this.rating = rating;
            this.pricePerRoom = price;
        }

        public void printHotel() {
            System.out.printf("Hotel %d : %s : %s : Rating=%d : Price=%.2f\n",
                    hotelId, name, location, rating, pricePerRoom);
        }

        public int getHotelId() { return hotelId; }
        public String getLocation() { return location; }
        public void setPricePerRoom(double p) { this.pricePerRoom = p; }
    }

    // ----- Vehicle & Transportation -----
    static class Vehicle {
        int vehicleId;
        String vehicleType;
        String franchiseName;
        int seatingCapacity;
        String from;
        String to;
        double price;

        public Vehicle(int id, String type, String franchise, int seats, String from, String to, double price) {
            this.vehicleId = id;
            this.vehicleType = type;
            this.franchiseName = franchise;
            this.seatingCapacity = seats;
            this.from = from;
            this.to = to;
            this.price = price;
        }

        public void printVehicleDetails() {
            System.out.printf("Vehicle %d : %s : %s : Seats=%d : %s -> %s : Price=%.2f\n",
                    vehicleId, vehicleType, franchiseName, seatingCapacity, from, to, price);
        }
    }

    static class Transportation {
        String source;
        String destination;
        List<Vehicle> vehicles = new ArrayList<>();

        public Transportation(String s, String d) {
            this.source = s;
            this.destination = d;
        }

        public void addVehicle(Vehicle v) { vehicles.add(v); }

        public void printAllVehicles() {
            for (Vehicle v : vehicles) v.printVehicleDetails();
        }
    }

    // ----- Booking State Pattern -----
    interface BookingState {
        void confirmBooking(Booking booking);
        void cancelBooking(Booking booking);
        String stateName();
    }

    static class PendingState implements BookingState {
        public void confirmBooking(Booking booking) {
            booking.setState(new ConfirmedState());
            System.out.println("Booking moved from Pending -> Confirmed");
        }
        public void cancelBooking(Booking booking) {
            booking.setState(new CancelledState());
            System.out.println("Booking moved from Pending -> Cancelled");
        }
        public String stateName() { return "PENDING"; }
    }

    static class ConfirmedState implements BookingState {
        public void confirmBooking(Booking booking) {
            System.out.println("Booking already confirmed.");
        }
        public void cancelBooking(Booking booking) {
            booking.setState(new CancelledState());
            System.out.println("Booking moved from Confirmed -> Cancelled");
        }
        public String stateName() { return "CONFIRMED"; }
    }

    static class CancelledState implements BookingState {
        public void confirmBooking(Booking booking) {
            System.out.println("Cannot confirm a cancelled booking.");
        }
        public void cancelBooking(Booking booking) {
            System.out.println("Booking already cancelled.");
        }
        public String stateName() { return "CANCELLED"; }
    }

    static class Booking {
        int bookingId;
        List<User> users = new ArrayList<>();
        List<Hotel> availableHotels = new ArrayList<>();
        List<Transportation> availableTransport = new ArrayList<>();
        List<Hotel> bookedHotels = new ArrayList<>();
        List<Vehicle> bookedVehicles = new ArrayList<>();
        BookingState currentState;

        public Booking(int id) {
            this.bookingId = id;
            this.currentState = new PendingState();
        }

        public void setState(BookingState s) { this.currentState = s; }
        public String getStateName() { return currentState.stateName(); }

        public double bookHotel(Scanner sc) {
            System.out.print("Enter Hotel ID to book: ");
            int hid = readInt(sc);
            System.out.print("Enter number of rooms: ");
            int rooms = readInt(sc);
            Hotel found = null;
            for (Hotel h : availableHotels) if (h.getHotelId() == hid) { found = h; break; }
            if (found == null) {
                System.out.println("Hotel not found.");
                return 0;
            }
            double charge = rooms * found.pricePerRoom;
            // Create a shallow copy to represent booked hotel (store charged price)
            Hotel booked = new Hotel(found.hotelId, found.name, found.location, found.rating, charge);
            bookedHotels.add(booked);
            currentState.confirmBooking(this); // move to Confirmed
            return charge;
        }

        public double bookVehicle(Scanner sc) {
            System.out.print("Enter source: ");
            String s = sc.nextLine().trim();
            System.out.print("Enter destination: ");
            String d = sc.nextLine().trim();
            // List vehicles matching route
            List<Vehicle> matches = new ArrayList<>();
            for (Transportation t : availableTransport) {
                for (Vehicle v : t.vehicles) {
                    if (v.from.equalsIgnoreCase(s) && v.to.equalsIgnoreCase(d)) matches.add(v);
                }
            }
            if (matches.isEmpty()) {
                System.out.println("No vehicles found for route.");
                return 0;
            }
            System.out.println("Matching vehicles:");
            for (Vehicle v : matches) v.printVehicleDetails();
            System.out.print("Enter vehicleId to book: ");
            int vid = readInt(sc);
            System.out.print("Enter number of tickets: ");
            int qty = readInt(sc);
            Vehicle sel = null;
            for (Vehicle v : matches) if (v.vehicleId == vid) { sel = v; break; }
            if (sel == null) {
                System.out.println("Vehicle not found among matches.");
                return 0;
            }
            double charge = qty * sel.price;
            // Add a shallow booked vehicle (with charged amount stored in price)
            Vehicle booked = new Vehicle(sel.vehicleId, sel.vehicleType, sel.franchiseName, sel.seatingCapacity, sel.from, sel.to, charge);
            bookedVehicles.add(booked);
            currentState.confirmBooking(this);
            return charge;
        }

        public void generateReceipt(int userId) {
            for (User u : users) {
                if (u.getUserId() == userId) {
                    u.generateReceipt();
                    return;
                }
            }
            System.out.println("User not found in booking context.");
        }

        public double cancellation() {
            double sum = 0;
            for (Hotel h : bookedHotels) sum += h.pricePerRoom;
            for (Vehicle v : bookedVehicles) sum += v.price;
            currentState.cancelBooking(this);
            return sum;
        }
    }

    // ----- Payment -----
    static class Payment {
        int paymentId;
        String paymentMode;
        double wallet;

        public Payment(int id, String mode, double wallet) {
            this.paymentId = id;
            this.paymentMode = mode;
            this.wallet = wallet;
        }

        public boolean makePayment(double amount, Scanner sc) {
            System.out.printf("Bill: %.2f\n", amount);
            System.out.println("[1] Wallet\n[2] UPI/Card (simulate)");
            System.out.print("Choose payment method: ");
            int choice = readInt(sc);
            if (choice == 1) {
                if (wallet >= amount) {
                    wallet -= amount;
                    System.out.println("Payment successful via wallet. Remaining wallet: " + wallet);
                    return true;
                } else {
                    System.out.println("Insufficient wallet balance.");
                    return false;
                }
            } else {
                // Simulate external payment success
                System.out.println("Payment successful via external method.");
                return true;
            }
        }

        public void refundPayment(double amount) {
            wallet += amount;
            System.out.println("Refund processed. Wallet credited by: " + amount);
            System.out.println("Updated wallet balance: " + wallet);
        }
    }

    // ----- Utility: read int robustly -----
    private static int readInt(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }

    // ----- Main program -----
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<User> users = new ArrayList<>();
        List<Hotel> hotels = new ArrayList<>();
        List<Transportation> transportList = new ArrayList<>();

        // Seed some users
        users.add(new Customer(101, "sachin", "sac@gmail.com", "Hubli", "123al"));
        users.add(new Customer(102, "alwyn", "alw@gmail.com", "Hubli", "123ay"));
        users.add(new Customer(103, "pavan", "pav123@gmail.com", "Gadag", "pav123"));
        users.add(new Admin(1001, "ascii", "as@gmail.com", "Bangalore", "qqq"));
        users.add(new Admin(1002, "robert", "rob@gmail.com", "Belgaum", "aaa"));

        // Seed hotels
        hotels.add(new Hotel(1, "Denissons", "Hubli", 5, 5000));
        hotels.add(new Hotel(2, "TravelInn", "Hubli", 4, 3000));
        hotels.add(new Hotel(3, "Pavan'sHotel", "Bangalore", 4, 4000));
        hotels.add(new Hotel(4, "Richid", "Bangalore", 3, 3800));

        // Seed transport and vehicles
        Vehicle v1 = new Vehicle(1001, "Bus", "SRS", 50, "Hubli", "Goa", 700);
        Vehicle v2 = new Vehicle(2001, "Flight", "SpiceJet", 180, "Hubli", "Goa", 5000);
        Vehicle v3 = new Vehicle(3001, "Train", "RaniChennama", 1000, "Hubli", "Bangalore", 800);

        Transportation t1 = new Transportation("Hubli", "Goa");
        Transportation t2 = new Transportation("Hubli", "Bangalore");
        t1.addVehicle(v1);
        t1.addVehicle(v2);
        t2.addVehicle(v3);

        transportList.add(t1);
        transportList.add(t2);

        Booking booking = new Booking(101);
        booking.users = users;
        booking.availableHotels = hotels;
        booking.availableTransport = transportList;

        Payment payment = new Payment(1, "Online", 20000);

        Register reg = new Register();
        int loggedInId = -1;
        double lastCharge = 0.0;
        boolean running = true;

        System.out.println("WELCOME!!!");
        while (running) {
            System.out.println("\nMENU");
            System.out.println("[1] Login");
            System.out.println("[2] Visit Profile");
            System.out.println("[3] Update Profile");
            System.out.println("[4] Display available hotels (by destination)");
            System.out.println("[5] Book Hotel rooms");
            System.out.println("[6] Display available Transportation services (by route)");
            System.out.println("[7] Book Tickets for travelling");
            System.out.println("[8] Make Payment for last booking");
            System.out.println("[9] Cancel Booking & Refund (if paid)");
            System.out.println("[10] Update Hotel room prices (admin only)");
            System.out.println("[11] Update Ticket prices (admin only)");
            System.out.println("[12] Exit");
            System.out.print("Enter choice: ");
            int ch = readInt(sc);
            switch (ch) {
                case 1:
                    loggedInId = reg.login(users, sc);
                    break;
                case 2:
                    if (loggedInId > 0) {
                        users.stream().filter(u -> u.getUserId() == loggedInId).forEach(User::printDetails);
                    } else System.out.println("Please login first.");
                    break;
                case 3:
                    if (loggedInId > 0) {
                        users.stream().filter(u -> u.getUserId() == loggedInId).findFirst().ifPresent(u -> u.updateProfile(sc));
                    } else System.out.println("Please login first.");
                    break;
                case 4:
                    if (loggedInId > 0) {
                        System.out.print("Enter destination: ");
                        String dest = sc.nextLine().trim();
                        hotels.stream().filter(h -> h.getLocation().equalsIgnoreCase(dest)).forEach(Hotel::printHotel);
                    } else System.out.println("Please login first.");
                    break;
                case 5:
                    if (loggedInId > 0) {
                        booking.setState(new PendingState());
                        lastCharge = booking.bookHotel(sc);
                        // after booking customer details shown when paying
                    } else System.out.println("Please login first.");
                    break;
                case 6:
                    if (loggedInId > 0) {
                        System.out.print("Enter source: ");
                        String s = sc.nextLine().trim();
                        System.out.print("Enter destination: ");
                        String d = sc.nextLine().trim();
                        for (Transportation t : transportList) {
                            for (Vehicle v : t.vehicles) {
                                if (v.from.equalsIgnoreCase(s) && v.to.equalsIgnoreCase(d)) v.printVehicleDetails();
                            }
                        }
                    } else System.out.println("Please login first.");
                    break;
                case 7:
                    if (loggedInId > 0) {
                        booking.setState(new PendingState());
                        lastCharge = booking.bookVehicle(sc);
                    } else System.out.println("Please login first.");
                    break;
                case 8:
                    if (lastCharge > 0) {
                        System.out.println("Receipt details:");
                        booking.generateReceipt(loggedInId);
                        boolean paid = payment.makePayment(lastCharge, sc);
                        if (!paid) System.out.println("Payment failed.");
                        else lastCharge = 0; // consider paid
                    } else {
                        System.out.println("No pending booking amount. Book first.");
                    }
                    break;
                case 9:
                    if (!booking.bookedHotels.isEmpty() || !booking.bookedVehicles.isEmpty()) {
                        double refund = booking.cancellation();
                        // For simplicity assume we refund immediately to wallet
                        payment.refundPayment(refund);
                    } else {
                        System.out.println("No bookings to cancel.");
                    }
                    break;
                case 10:
                    if (loggedInId > 0) {
                        try {
                            updateHotelPrice(users, loggedInId, hotels, sc);
                        } catch (ApplicationException ae) {
                            System.out.println("ERROR!! " + ae.getErrNo() + " : " + ae.getMessage());
                        }
                    } else System.out.println("Please login first.");
                    break;
                case 11:
                    if (loggedInId > 0) {
                        try {
                            updateVehiclePrice(users, loggedInId, transportList, sc);
                        } catch (ApplicationException ae) {
                            System.out.println("ERROR!! " + ae.getErrNo() + " : " + ae.getMessage());
                        }
                    } else System.out.println("Please login first.");
                    break;
                case 12:
                    System.out.println("Exiting... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        sc.close();
    }

    // ----- Admin-only helpers -----
    private static void updateHotelPrice(List<User> users, int loggedInId, List<Hotel> hotels, Scanner sc) throws ApplicationException {
        if (!isAdmin(users, loggedInId)) throw new ApplicationException(101, "User is not an admin");
        System.out.println("Admin verified.");
        System.out.print("Enter Hotel ID to update: ");
        int hid = readInt(sc);
        Hotel found = null;
        for (Hotel h : hotels) if (h.getHotelId() == hid) { found = h; break; }
        if (found == null) {
            System.out.println("Hotel not found.");
            return;
        }
        System.out.print("Enter new price per room: ");
        double p;
        while (true) {
            String s = sc.nextLine().trim();
            try {
                p = Double.parseDouble(s);
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid price. Try again: ");
            }
        }
        found.setPricePerRoom(p);
        System.out.println("Updated hotel details:");
        found.printHotel();
    }

    private static void updateVehiclePrice(List<User> users, int loggedInId, List<Transportation> transports, Scanner sc) throws ApplicationException {
        if (!isAdmin(users, loggedInId)) throw new ApplicationException(1002, "User is not an admin");
        System.out.println("Admin verified.");
        System.out.print("Enter vehicle ID to update: ");
        int vid = readInt(sc);
        Vehicle found = null;
        for (Transportation t : transports) {
            for (Vehicle v : t.vehicles) {
                if (v.vehicleId == vid) { found = v; break; }
            }
            if (found != null) break;
        }
        if (found == null) {
            System.out.println("Vehicle not found.");
            return;
        }
        System.out.print("Enter new price: ");
        double p;
        while (true) {
            String s = sc.nextLine().trim();
            try {
                p = Double.parseDouble(s);
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid price. Try again: ");
            }
        }
        found.price = p;
        System.out.println("Updated vehicle details:");
        found.printVehicleDetails();
    }

    private static boolean isAdmin(List<User> users, int id) {
        for (User u : users) {
            if (u.getUserId() == id && u instanceof Admin) return true;
        }
        return false;
    }
}
