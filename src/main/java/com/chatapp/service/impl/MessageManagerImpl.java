/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service.impl;

/**
 *
 * @author SevenC
 */


import com.chatapp.model.Message;
import com.chatapp.service.MessageManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * //In-memory message store with JSON write-through persistence.
 *
 * @see <a href="https://github.com/google/gson">https://github.com/google/gson</a>
 */
public class MessageManagerImpl implements MessageManager {

    private final String jsonFile;

    private final Message[] sentMessages;
    private final Message[] disregardedMessages;
    private final Message[] storedMessages;

    private int sentCount;
    private int disregardedCount;
    private int storedCount;

    private int messageCounter = 0;


    public MessageManagerImpl(int capacity) {
        this(capacity, "stored_messages.json");
    }

    public MessageManagerImpl(int capacity, String jsonFile) {
        this.jsonFile = jsonFile;
        int cap = Math.max(capacity, 10);
        sentMessages        = new Message[cap];
        disregardedMessages = new Message[cap];
        storedMessages      = new Message[cap];
    }


    //Validation

    @Override
    public String validateRecipient(String recipient) {
        if (recipient == null) return "Recipient number incorrectly formatted.";
        if (!recipient.matches("^\\+27[6-8][0-9]{8}$"))
            return "Recipient number incorrectly formatted.";
        return "Recipient accepted";
    }

    @Override
    public String validateMessageBody(String body) {
        if (body == null) return "Please enter a message of less than 250 characters";
        if (body.length() > 250)
            return "Please enter a message of less than 250 characters";
        return "Message Sent";
    }


    //Compose

    @Override
    public Message composeMessage(String recipient, String body) {
        return composeMessage(null, recipient, body);
    }

    @Override
    public Message composeMessage(String sender, String recipient, String body) {
        messageCounter++;
        String id = Message.generateMessageId();
        return new Message(id, messageCounter, sender, recipient, body);
    }


    //Status changes

    @Override
    public void sendMessage(Message m) {
        m.setStatus("SENT");
        if (sentCount < sentMessages.length) sentMessages[sentCount++] = m;
    }

    @Override
    public void disregardMessage(Message m) {
        m.setStatus("DISREGARDED");
        if (disregardedCount < disregardedMessages.length)
            disregardedMessages[disregardedCount++] = m;
    }

    @Override
    public void storeMessage(Message m) {
        m.setStatus("STORED");
        if (storedCount < storedMessages.length)
            storedMessages[storedCount++] = m;
        saveStoredToJson();
    }


    //Read APIs

    @Override
    public Message[] getSentMessages() {
        return Arrays.copyOf(sentMessages, sentCount);
    }

    @Override
    public Message[] getStoredMessages() {
        return Arrays.copyOf(storedMessages, storedCount);
    }


    //JSON persistence

    @Override
    public void saveStoredToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Message[] live = Arrays.copyOf(storedMessages, storedCount);
        try (FileWriter fw = new FileWriter(jsonFile)) {
            gson.toJson(live, fw);
        } catch (IOException e) {
            System.err.println("Could not save JSON: " + e.getMessage());
        }
    }

    /**
     * //Reads {@code jsonFile} and repopulates the in-memory stored array.
     *
     */
    @Override
    public void loadStoredFromJson() {
        File file = new File(jsonFile);
        if (!file.exists()) return;

        Gson gson = new Gson();
        try (FileReader fr = new FileReader(file)) {
            Message[] loaded = gson.fromJson(fr, Message[].class);
            if (loaded == null) return;

            int maxNum = 0;
            for (Message m : loaded) {
                if (m == null) continue;
                if (storedCount < storedMessages.length) {
                    storedMessages[storedCount++] = m;
                }
                if (m.getMessageNumber() > maxNum) maxNum = m.getMessageNumber();
            }
            messageCounter = maxNum;
        } catch (IOException | JsonParseException e) {
            System.err.println("Could not load JSON: " + e.getMessage());
        }
    }


    //Part 3: report helpers

    @Override
    public Message getLongestStoredMessage() {
        if (storedCount == 0) return null;
        Message longest = storedMessages[0];
        int longestLen = bodyLength(longest);
        for (int i = 1; i < storedCount; i++) {
            int len = bodyLength(storedMessages[i]);
            if (len > longestLen) {
                longest    = storedMessages[i];
                longestLen = len;
            }
        }
        return longest;
    }

    private int bodyLength(Message m) {
        return (m.getMessageBody() == null) ? 0 : m.getMessageBody().length();
    }


    @Override
    public Message searchByMessageId(String id) {
        if (id == null) return null;
        for (int i = 0; i < storedCount; i++) {
            if (id.equals(storedMessages[i].getMessageId())) return storedMessages[i];
        }
        return null;
    }


    @Override
    public Message[] searchByRecipient(String recipient) {
        if (recipient == null) return new Message[0];
        Message[] found = new Message[storedCount];
        int n = 0;
        for (int i = 0; i < storedCount; i++) {
            if (recipient.equals(storedMessages[i].getRecipient()))
                found[n++] = storedMessages[i];
        }
        return Arrays.copyOf(found, n);
    }


    @Override
    public boolean deleteByHash(String hash) {
        if (hash == null) return false;
        for (int i = 0; i < storedCount; i++) {
            if (hash.equalsIgnoreCase(storedMessages[i].getMessageHash())) {
                for (int j = i; j < storedCount - 1; j++)
                    storedMessages[j] = storedMessages[j + 1];
                storedMessages[storedCount - 1] = null;
                storedCount--;
                saveStoredToJson();
                return true;
            }
        }
        return false;
    }


    @Override
    public String buildReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== STORED MESSAGES REPORT =====\n");
        if (storedCount == 0) {
            sb.append("No stored messages.\n");
            return sb.toString();
        }
        for (int i = 0; i < storedCount; i++) {
            Message m = storedMessages[i];
            String from = (m.getSender() == null) ? "(unknown)" : m.getSender();
            sb.append("Message #").append(m.getMessageNumber()).append("\n");
            sb.append("  ID:     ").append(m.getMessageId()).append("\n");
            sb.append("  From:   ").append(from).append("\n");
            sb.append("  To:     ").append(m.getRecipient()).append("\n");
            sb.append("  Hash:   ").append(m.getMessageHash()).append("\n");
            sb.append("  Body:   ").append(m.getMessageBody()).append("\n");
            sb.append("  Status: ").append(m.getStatus()).append("\n");
            sb.append("---\n");
        }
        return sb.toString();
    }
}