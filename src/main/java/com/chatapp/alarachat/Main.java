/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp;

/**
 *
 * @author SevenC
 */


import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.service.impl.MessageManagerImpl;
import com.chatapp.service.impl.UserValidator;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final UserValidator validator = new UserValidator();

    private static User currentUser;
    private static MessageManagerImpl manager;


    public static void main(String[] args) {

        System.out.println("=========================================");
        System.out.println("        Welcome to QuickChat.");
        System.out.println("=========================================");

        try {
            currentUser = registerFlow();
            if (currentUser == null) {
                System.out.println("Registration aborted. Goodbye.");
                pauseBeforeExit();
                return;
            }

            if (!loginFlow(currentUser)) {
                System.out.println("Login failed 3 times. Goodbye.");
                pauseBeforeExit();
                return;
            }

            System.out.print("\nHow many messages do you wish to send in this session? ");
            int count = readIntSafely();
            manager = new MessageManagerImpl(count);
            manager.loadStoredFromJson();

            boolean running = true;
            while (running) {
                printMainMenu();
                int choice = readIntSafely();
                switch (choice) {
                    case 1: sendMessagesFlow(count);                break;
                    case 2: System.out.println("Coming Soon.");     break;
                    case 3: storedMessagesFlow();                   break;
                    case 4: running = false;                        break;
                    default: System.out.println("Invalid option. Try again.");
                }
            }
            System.out.println("\nGoodbye!");

        } catch (NoSuchElementException e) {
            System.out.println("\n\n[!] No input detected on standard input.");
            System.out.println("    Re-run from an external terminal:");
            System.out.println("        java -jar target/AlaraChat-1.0-SNAPSHOT.jar");
        } finally {
            pauseBeforeExit();
        }
    }


    private static User registerFlow() {
        System.out.println("\n--- REGISTRATION ---");

        System.out.print("First name: ");
        String first = sc.nextLine().trim();

        System.out.print("Last name: ");
        String last = sc.nextLine().trim();

        String username;
        while (true) {
            System.out.print("Username (must contain '_', max 5 chars): ");
            username = sc.nextLine().trim();
            String msg = validator.validateUsername(username);
            System.out.println(msg);
            if (msg.equals(UserValidator.USERNAME_OK)) break;
        }

        String password;
        while (true) {
            System.out.print("Password (>=8, capital, digit, special): ");
            password = sc.nextLine();
            String msg = validator.validatePassword(password);
            System.out.println(msg);
            if (msg.equals(UserValidator.PASSWORD_OK)) break;
        }

        String cell;
        while (true) {
            System.out.print("Cellphone (e.g. +27821234567 or 0821234567): ");
            cell = sc.nextLine().trim();
            String msg = validator.validateCellphone(cell);
            System.out.println(msg);
            if (msg.equals(UserValidator.CELL_OK)) break;
        }

        return new User(first, last, username, password, cell);
    }


    private static boolean loginFlow(User stored) {
        System.out.println("\n--- LOGIN ---");
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Username (attempt " + attempt + "/3): ");
            String u = sc.nextLine().trim();
            System.out.print("Password: ");
            String p = sc.nextLine();

            String msg = validator.validateLogin(u, p, stored);
            System.out.println(msg);
            if (msg.startsWith(UserValidator.LOGIN_OK_PREFIX)) return true;
        }
        return false;
    }


    private static void printMainMenu() {
        System.out.println("\n============ MAIN MENU ============");
        System.out.println(" 1) Send messages");
        System.out.println(" 2) Show recently sent messages");
        System.out.println(" 3) Stored Messages");
        System.out.println(" 4) Quit");
        System.out.print("Choice: ");
    }


    //Part 3: Stored Messages

    private static void storedMessagesFlow() {
        boolean back = false;
        while (!back) {
            System.out.println("\n========== STORED MESSAGES ==========");
            System.out.println(" 1) Display sender and recipient of all stored messages");
            System.out.println(" 2) Display the longest stored message");
            System.out.println(" 3) Search for a message by message ID");
            System.out.println(" 4) Search for messages by recipient");
            System.out.println(" 5) Delete a message using message hash");
            System.out.println(" 6) Display full report of all stored messages");
            System.out.println(" 7) Back to main menu");
            System.out.print("Choice: ");

            int choice = readIntSafely();
            switch (choice) {
                case 1: displaySenderAndRecipient(); break;
                case 2: displayLongestMessage();     break;
                case 3: searchById();                break;
                case 4: searchByRecipient();         break;
                case 5: deleteByHash();              break;
                case 6: System.out.println(manager.buildReport()); break;
                case 7: back = true;                 break;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }


    private static void displaySenderAndRecipient() {
        Message[] stored = manager.getStoredMessages();
        if (stored.length == 0) { System.out.println("No stored messages."); return; }
        for (Message m : stored) {
            String from = (m.getSender() == null) ? "(unknown)" : m.getSender();
            System.out.println("From: " + from + " | To: " + m.getRecipient());
        }
    }


    private static void displayLongestMessage() {
        Message m = manager.getLongestStoredMessage();
        if (m == null) { System.out.println("No stored messages."); return; }
        System.out.println("Longest message:");
        System.out.println(m);
    }


    private static void searchById() {
        System.out.print("Message ID: ");
        String id = sc.nextLine().trim();
        Message m = manager.searchByMessageId(id);
        if (m == null) System.out.println("No message found with ID " + id);
        else           System.out.println(m);
    }


    private static void searchByRecipient() {
        System.out.print("Recipient (+27...): ");
        String recipient = sc.nextLine().trim();
        Message[] found = manager.searchByRecipient(recipient);
        if (found.length == 0) {
            System.out.println("No messages found for " + recipient);
            return;
        }
        for (Message m : found) System.out.println(m);
    }


    private static void deleteByHash() {
        System.out.print("Message hash: ");
        String hash = sc.nextLine().trim();
        System.out.println(manager.deleteByHash(hash)
            ? "Message deleted."
            : "No message found with hash " + hash);
    }


    //Send-message flow (Part 2) — passes sender, awaits disregard confirm (finally)

    private static void sendMessagesFlow(int allowed) {
        if (allowed <= 0) {
            System.out.println("No messages allowed this session.");
            return;
        }

        for (int i = 0; i < allowed; i++) {
            System.out.println("\n--- Message " + (i + 1) + " of " + allowed + " ---");

            System.out.print("Recipient cell number (+27...): ");
            String recipient = sc.nextLine().trim();
            if (!"Recipient accepted".equals(manager.validateRecipient(recipient))) {
                System.out.println("Recipient number incorrectly formatted.");
                i--; continue;
            }

            System.out.print("Message (max 250 chars): ");
            String body = sc.nextLine();
            String bodyCheck = manager.validateMessageBody(body);
            if (!"Message Sent".equals(bodyCheck)) {
                System.out.println(bodyCheck);
                i--; continue;
            }

            Message m = manager.composeMessage(
                    currentUser.getUsername(), recipient, body);

            System.out.println("\n--- Preview ---");
            System.out.println(m);

            System.out.println("\nWhat would you like to do?");
            System.out.println("  1) Send Message");
            System.out.println("  2) Disregard Message");
            System.out.println("  3) Store Message");
            System.out.print("Choice: ");
            int action = readIntSafely();

            switch (action) {
                case 1:
                    manager.sendMessage(m);
                    System.out.println("Message successfully sent");
                    break;

                case 2:
                    manager.disregardMessage(m);
                    System.out.println("Enter 0 to delete the message.");
                    readIntSafely();
                    break;

                case 3:
                    manager.storeMessage(m);
                    System.out.println("Message successfully stored.");
                    break;

                default:
                    System.out.println("Unknown action – treating as disregard");
                    manager.disregardMessage(m);
                    System.out.println("Enter 0 to delete the message.");
                    readIntSafely();
            }
        }

        System.out.println("\nTotal messages sent this session: "
                         + manager.getSentMessages().length);
    }


    private static int readIntSafely() {
        while (true) {
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("Enter a whole number: "); }
        }
    }


    private static void pauseBeforeExit() {
        System.out.println("\nPress Enter to close...");
        try { sc.nextLine(); } catch (Exception ignored) { }
    }
}