
import java.io.*;
import java.util.*;

class SeatBookedException extends Exception {
    public SeatBookedException(String message) {
        super(message);
    }
}

abstract class Seat {
    abstract void showSeat();
    abstract int getPrice();
}

class SelectSeat extends Seat {
    int numSeat;
    char zoneSeat;

    @Override
    void showSeat() {
        System.out.println("==================Stage=================");

        for (char zone : new char[]{'D', 'G', 'F'}) {
            System.out.print("Zone " + zone + ": ");
            for (int seat = 1; seat <= 10; seat++) {
                System.out.printf("%2d ", seat);
            }
            System.out.println();
        }

        System.out.println();
    }

    @Override
    int getPrice() {
        if (zoneSeat == 'D') {
            return 100;
        } else if (zoneSeat == 'G') {
            return 80;
        } else if (zoneSeat == 'F') {
            return 50;
        } else {
            return 0;
        }
    }
}

class Contact {
    String name;
    String email;
    String phoneNumber;

    void inputData(Scanner scanner) {
        System.out.print("Enter name: ");
        name = scanner.nextLine();

        System.out.print("Enter email: ");
        email = scanner.nextLine();

        System.out.print("Enter phone number: ");
        phoneNumber = scanner.nextLine();
    }

    void showData() {
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone Number: " + phoneNumber);
    }
}

interface Bookable {
    void bookSeat(SelectSeat seat, Contact contact) throws SeatBookedException;
    void cancelBooking(SelectSeat seat);
}

class Concert implements Bookable {
    String name;
    String date;
    String time;
    String location;
    ArrayList<SelectSeat> bookedSeats = new ArrayList<>();

    @Override
    public void bookSeat(SelectSeat seat, Contact contact) throws SeatBookedException {
        if (isSeatBooked(seat)) {
            throw new SeatBookedException("Seat " + seat.zoneSeat + seat.numSeat + " is already booked.");
        }
        bookedSeats.add(seat);
        System.out.println("Seat " + seat.zoneSeat + seat.numSeat + " booked successfully.");
    }

    @Override
    public void cancelBooking(SelectSeat seat) {
        boolean removed = bookedSeats.remove(seat);
        if (removed) {
            System.out.println("Booking for seat " + seat.zoneSeat + seat.numSeat + " cancelled successfully.");
        } else {
            System.out.println("Seat " + seat.zoneSeat + seat.numSeat + " is not booked.");
        }
    }

    boolean isSeatBooked(SelectSeat seat) {
        for (SelectSeat bookedSeat : bookedSeats) {
            if (bookedSeat.zoneSeat == seat.zoneSeat && bookedSeat.numSeat == seat.numSeat) {
                return true;
            }
        }
        return false;
    }
}

public class oodp {
    static ArrayList<Concert> concerts = new ArrayList<>();
    static HashMap<String, ArrayList<String>> bookings = new HashMap<>();

    public static void main(String[] args) {
        loadConcerts();
        loadBookings();

        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            showMenu();
            choice = getChoice(scanner);

            switch (choice) {
                case 1:
                    bookTickets(scanner);
                    break;
                case 2:
                    viewBookings(scanner);
                    break;
                case 3:
                    cancelBooking(scanner);
                    break;
                case 4:
                    System.out.println("Exit the program");
                    break;
                default:
                    System.out.println("Incorrect choice. Please try again.");
            }
        } while (choice != 4);

        scanner.close();

        saveBookings();
    }

    static void loadConcerts() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("concerts.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Concert concert = new Concert();
                    concert.name = parts[0].trim();
                    concert.date = parts[1].trim();
                    concert.time = parts[2].trim();
                    concert.location = parts[3].trim();
                    concerts.add(concert);
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Concerts file not found. Starting with no concerts.");
        }
    }

    static void loadBookings() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("bookings.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String concertName = parts[0].trim().substring(9);
                    String booking = line.substring(line.indexOf(",") + 1).trim();
                    if (!bookings.containsKey(concertName)) {
                        bookings.put(concertName, new ArrayList<String>());
                    }
                    bookings.get(concertName).add(booking);
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Bookings file not found. Starting with empty bookings.");
        }
    }

    static void saveBookings() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("bookings.txt"));
            for (String concertName : bookings.keySet()) {
                ArrayList<String> concertBookings = bookings.get(concertName);
                for (String booking : concertBookings) {
                    pw.println("Concert: " + concertName + ", " + booking);
                }
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error saving bookings: " + e.getMessage());
        }
    }

    static void showMenu() {
        System.out.println("=================================");
        System.out.println("Concert Ticket Booking System");
        System.out.println("=================================");
        System.out.println("1. Book tickets");
        System.out.println("2. View ticket booking status");
        System.out.println("3. Cancel ticket booking");
        System.out.println("4. Exit the program");
        System.out.println("=================================");
        System.out.print("Please select an option: ");
    }

    static int getChoice(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
        }
        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }

    static void bookTickets(Scanner scanner) {
        System.out.println("Book tickets");
        showConcerts();

        int option = getOption(scanner, concerts.size(), "Please select the concert number you want to book");

        if (option >= 1 && option <= concerts.size()) {
            Bookable concert = concerts.get(option - 1);
            System.out.println("Booking for concert: " + ((Concert)concert).name);

            SelectSeat seat = selectSeat(scanner);
            Contact contact = getContactInfo(scanner);

            try {
                concert.bookSeat(seat, contact);

                int price = seat.getPrice();
                processPayment(price, scanner);

                String booking = "Seat: " + seat.zoneSeat + seat.numSeat +
                        ", Name: " + contact.name +
                        ", Email: " + contact.email +
                        ", Phone: " + contact.phoneNumber;

                ArrayList<String> concertBookings = bookings.get(((Concert)concert).name);
                if (concertBookings == null) {
                    concertBookings = new ArrayList<>();
                    bookings.put(((Concert)concert).name, concertBookings);
                }
                concertBookings.add(booking);

                System.out.println("====== Booking Successful ======");
                saveBookings();
            } catch (SeatBookedException e) {
                System.out.println("Booking failed: " + e.getMessage());
            }
        }
    }

    static void viewBookings(Scanner scanner) {
        System.out.println("View ticket booking status");
    
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
    
        System.out.println("===== Booked Concerts =====");
        int index = 1;
        for (String concertName : bookings.keySet()) {
            System.out.println(index + ". " + concertName);
            index++;
        }
    
        int option = getOption(scanner, bookings.size(), "Please select the concert number to view booking status");
    
        if (option >= 1 && option <= bookings.size()) {
            String selectedConcertName = new ArrayList<>(bookings.keySet()).get(option - 1);
            System.out.println("Booking status for concert: " + selectedConcertName);
    
            ArrayList<String> concertBookings = bookings.get(selectedConcertName);
            if (concertBookings != null && !concertBookings.isEmpty()) {
                for (String booking : concertBookings) {
                    System.out.println(booking);
                }
            } else {
                System.out.println("No bookings found for this concert.");
            }
        }
    }

    static void cancelBooking(Scanner scanner) {
        System.out.println("Cancel ticket booking");
    
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
    
        System.out.println("===== Booked Concerts =====");
        int index = 1;
        for (String concertName : bookings.keySet()) {
            System.out.println(index + ". " + concertName);
            index++;
        }
    
        int concertOption = getOption(scanner, bookings.size(), "Please select the concert number to cancel booking");
    
        if (concertOption >= 1 && concertOption <= bookings.size()) {
            String selectedConcertName = new ArrayList<>(bookings.keySet()).get(concertOption - 1);
            System.out.println("Canceling booking for concert: " + selectedConcertName);
    
            ArrayList<String> concertBookings = bookings.get(selectedConcertName);
            if (concertBookings != null && !concertBookings.isEmpty()) {
                System.out.println("===== Booked Seats =====");
                for (int i = 0; i < concertBookings.size(); i++) {
                    System.out.println((i + 1) + ". " + concertBookings.get(i));
                }
    
                int seatOption = getOption(scanner, concertBookings.size(), "Please select the seat number to cancel booking");
    
                if (seatOption >= 1 && seatOption <= concertBookings.size()) {
                    String selectedBooking = concertBookings.get(seatOption - 1);
                    String[] bookingDetails = selectedBooking.split(", ");
                    String seatString = bookingDetails[0].substring(6);
    
                    concertBookings.remove(seatOption - 1);
                    System.out.println("Booking for seat " + seatString + " cancelled successfully.");
    
                    if (concertBookings.isEmpty()) {
                        bookings.remove(selectedConcertName);
                    }
    
                    saveBookings();
                }
            } else {
                System.out.println("No bookings found for this concert.");
            }
        }
    }
    static void showConcerts() {
        System.out.println("===== Concert Options =====");
        for (int i = 0; i < concerts.size(); i++) {
            Concert concert = concerts.get(i);
            System.out.println((i + 1) + ". " + concert.name +
                    " - " + concert.date +
                    " - " + concert.time +
                    " - " + concert.location);
        }
        System.out.println("0. Back to main menu");
    }

    static int getOption(Scanner scanner, int maxOption, String message) {
        int option;
        while (true) {
            System.out.print(message + " (1-" + maxOption + ") or 0 to go back to main menu: ");
            option = getChoice(scanner);

            if (option >= 0 && option <= maxOption) {
                break;
            }
            System.out.println("Incorrect choice. Please try again.");
        }
        return option;
    }

    static SelectSeat selectSeat(Scanner scanner) {
        SelectSeat seat = new SelectSeat();
        seat.showSeat();

        while (true) {
            System.out.print("Please select Zone (D/G/F): ");
            String zoneInput = scanner.nextLine().trim().toUpperCase();
            if (zoneInput.length() == 1 && (zoneInput.equals("D") || zoneInput.equals("G") || zoneInput.equals("F"))) {
                seat.zoneSeat = zoneInput.charAt(0);
                break;
            }
            System.out.println("Invalid Zone. Please select only D, G, or F.");
        }

        while (true) {
            System.out.print("Please select seat number (1-10): ");
            if (scanner.hasNextInt()) {
                seat.numSeat = scanner.nextInt();
                scanner.nextLine();
                if (seat.numSeat >= 1 && seat.numSeat <= 10) {
                    break;
                }
            } else {
                scanner.nextLine();
            }
            System.out.println("Invalid seat number. Please select only 1-10.");
        }

        System.out.println("Your seat is: " + seat.zoneSeat + seat.numSeat);
        return seat;
    }

    static Contact getContactInfo(Scanner scanner) {
        Contact contact = new Contact();
        contact.inputData(scanner);
        return contact;
    }

    static void processPayment(int price, Scanner scanner) {
        System.out.println("Price: " + price + " baht");

        while (true) {
            System.out.print("Enter amount paid: ");
            if (scanner.hasNextInt()) {
                int paid = scanner.nextInt();
                scanner.nextLine(); 

                if (paid >= price) {
                    int change = paid - price;
                    System.out.println("Change: " + change + " baht");
                    System.out.println("Payment successful!");
                    return;
                } else {
                    System.out.println("Insufficient amount paid. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); 
            }
        }
    }
}

