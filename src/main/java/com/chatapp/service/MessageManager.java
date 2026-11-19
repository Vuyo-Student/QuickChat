/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

/**
 *
 * @author SevenC
 */


import com.chatapp.model.Message;

public interface MessageManager {

    String validateRecipient(String recipient);
    String validateMessageBody(String body);

    // Compose without sender (legacy — sender stored as null). */
    Message composeMessage(String recipient, String body);

    // Compose with sender (Part 3). */
    Message composeMessage(String sender, String recipient, String body);

    void sendMessage(Message m);
    void disregardMessage(Message m);
    void storeMessage(Message m);

    Message[] getSentMessages();
    void      saveStoredToJson();

    //Part 3: Store Data & Display Task Report

    // Reads the JSON file and repopulates the stored array. Missing file = no-op. */
    void loadStoredFromJson();

    // Returns a trimmed copy of all stored messages. */
    Message[] getStoredMessages();

    // Returns the stored message with the longest body, or null if none. */
    Message getLongestStoredMessage();

    // Returns the stored message with the given ID, or null. */
    Message searchByMessageId(String id);

    // Returns every stored message addressed to {@code recipient}. */
    Message[] searchByRecipient(String recipient);

    // Deletes the stored message with the given hash. Returns true if found. */
    boolean deleteByHash(String hash);

    // Returns a human-readable multi-line report of all stored messages. */
    String buildReport();
}