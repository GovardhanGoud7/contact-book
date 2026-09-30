import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Contact Book: add, view, search, edit and delete contacts.
 * Contacts are saved in contacts.txt (in the folder you run the program from).
 *
 * Run in VS Code: open this file and click the Run button above main.
 */
public class ContactBook {

    // ---------- Contact class ----------
    static class Contact {
        String name;
        String phone;
        String email;

        Contact(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }

        @Override
        public String toString() {
            String mail = email.isEmpty() ? "-" : email;
            return String.format("%-18s %-16s %s", name, phone, mail);
        }
    }

    private static final Path FILE = Paths.get("contacts.txt");
    private static final List<Contact> contacts = new ArrayList<>();
    private static final Scanner input = new Scanner(System.in);

    // ---------- Saving and loading ----------
    static void load() {
        if (!Files.exists(FILE)) return;
        try {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                String[] parts = line.split("\t", -1);
                if (parts.length >= 2) {
                    contacts.add(new Contact(parts[0], parts[1], parts.length > 2 ? parts[2] : ""));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + FILE + ": " + e.getMessage());
        }
    }

    static void save() {
        List<String> lines = new ArrayList<>();
        for (Contact c : contacts) lines.add(c.name + "\t" + c.phone + "\t" + c.email);
        try {
            Files.write(FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("  (Could not save contacts: " + e.getMessage() + ")");
        }
    }

    // ---------- Input helpers ----------
    static String clean(String text) {
        // tabs would break the save format, so replace them with spaces
        return text.replace('\t', ' ').trim();
    }

    static String askName() {
        while (true) {
            System.out.print("Name: ");
            String name = clean(input.nextLine());
            if (!name.isEmpty()) return name;
            System.out.println("  Name can't be empty.");
        }
    }

    static String askPhone() {
        while (true) {
            System.out.print("Phone: ");
            String phone = clean(input.nextLine());
            if (phone.matches("[0-9+()\\-\\s]{7,20}")) return phone;
            System.out.println("  Enter a valid phone number (7-20 digits, may include + - ( )).");
        }
    }

    static String askEmail() {
        while (true) {
            System.out.print("Email (optional, press Enter to skip): ");
            String email = clean(input.nextLine());
            if (email.isEmpty() || email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) return email;
            System.out.println("  That doesn't look like an email address.");
        }
    }

    // ---------- Finding contacts ----------
    static List<Contact> search(String text) {
        List<Contact> found = new ArrayList<>();
        String q = text.toLowerCase();
        for (Contact c : contacts) {
            if (c.name.toLowerCase().contains(q) || c.phone.contains(text)) found.add(c);
        }
        found.sort(Comparator.comparing(c -> c.name.toLowerCase()));
        return found;
    }

    /** Asks for a search word and lets the user pick one match. Returns null if none chosen. */
    static Contact pickContact(String action) {
        if (contacts.isEmpty()) {
            System.out.println("  No contacts yet.");
            return null;
        }
        System.out.print("Name or number to " + action + ": ");
        List<Contact> found = search(clean(input.nextLine()));
        if (found.isEmpty()) {
            System.out.println("  No matching contact.");
            return null;
        }
        if (found.size() == 1) return found.get(0);

        System.out.println("  Several matches:");
        for (int i = 0; i < found.size(); i++) System.out.println("  " + (i + 1) + ") " + found.get(i));
        System.out.print("Pick a number: ");
        String text = input.nextLine().trim();
        if (text.matches("\\d+") && Integer.parseInt(text) >= 1 && Integer.parseInt(text) <= found.size()) {
            return found.get(Integer.parseInt(text) - 1);
        }
        System.out.println("  Cancelled.");
        return null;
    }

    // ---------- Menu actions ----------
    static void addContact() {
        String name = askName();
        String phone = askPhone();
        String email = askEmail();
        contacts.add(new Contact(name, phone, email));
        save();
        System.out.println("  Saved " + name + ".");
    }

    static void viewAll() {
        if (contacts.isEmpty()) {
            System.out.println("  No contacts yet. Add one with option 1.");
            return;
        }
        List<Contact> sorted = new ArrayList<>(contacts);
        sorted.sort(Comparator.comparing(c -> c.name.toLowerCase()));
        System.out.println();
        System.out.printf("  %-18s %-16s %s%n", "NAME", "PHONE", "EMAIL");
        for (Contact c : sorted) System.out.println("  " + c);
        System.out.println("  (" + sorted.size() + " contacts)");
    }

    static void searchContacts() {
        System.out.print("Search for: ");
        List<Contact> found = search(clean(input.nextLine()));
        if (found.isEmpty()) {
            System.out.println("  No matching contact.");
            return;
        }
        for (Contact c : found) System.out.println("  " + c);
    }

    static void editContact() {
        Contact c = pickContact("edit");
        if (c == null) return;
        System.out.println("  Editing " + c.name + ". Type new details.");
        c.name = askName();
        c.phone = askPhone();
        c.email = askEmail();
        save();
        System.out.println("  Updated.");
    }

    static void deleteContact() {
        Contact c = pickContact("delete");
        if (c == null) return;
        System.out.print("Delete " + c.name + "? (y/n): ");
        if (input.nextLine().trim().equalsIgnoreCase("y")) {
            contacts.remove(c);
            save();
            System.out.println("  Deleted.");
        } else {
            System.out.println("  Kept.");
        }
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        load();
        System.out.println("=== Contact Book === (" + contacts.size() + " saved contacts)");
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1) Add   2) View all   3) Search   4) Edit   5) Delete   6) Quit");
            System.out.print("Your choice: ");
            switch (input.nextLine().trim()) {
                case "1" -> addContact();
                case "2" -> viewAll();
                case "3" -> searchContacts();
                case "4" -> editContact();
                case "5" -> deleteContact();
                case "6" -> running = false;
                default -> System.out.println("  Please enter a number from 1 to 6.");
            }
        }
        System.out.println("Goodbye!");
    }
}
