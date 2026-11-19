/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.model;

/**
 *
 * @author SevenC
 */


import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private int    messageNumber;
    private String sender;      // NEW — username of the logged-in user
    private String recipient;
    private String messageBody;
    private String messageHash;
    private String status;

    public Message() { }

    /** Legacy constructor — sender defaults to null. */
    public Message(String messageId, int messageNumber,
                   String recipient, String messageBody) {
        this(messageId, messageNumber, null, recipient, messageBody);
    }

    /** Full constructor with sender (Part 3). */
    public Message(String messageId, int messageNumber, String sender,
                   String recipient, String messageBody) {
        this.messageId     = messageId;
        this.messageNumber = messageNumber;
        this.sender        = sender;
        this.recipient     = recipient;
        this.messageBody   = messageBody;
        this.messageHash   = generateHash();
    }


    /**
     * Builds a 10-digit message ID using String.format (no StringBuilder).
     */
    public static String generateMessageId() {
        long n = (long) (Math.random() * 10_000_000_000L);
        return String.format("%010d", n);
    }


    /**
     * Hash = first2(ID) : number : FIRSTWORDLASTWORD (uppercased).
     * Punctuation is stripped so "Hi Mike, ... tonight?" -> "HITONIGHT".
     */
    private String generateHash() {
        String id = (messageId == null || messageId.length() < 2)
                  ? "00" : messageId.substring(0, 2);

        String body = (messageBody == null) ? "" : messageBody.trim();
        String firstWord, lastWord;

        if (body.isEmpty()) {
            firstWord = "";
            lastWord  = "";
        } else {
            String[] parts = body.split("\\s+");
            firstWord = parts[0].replaceAll("[^A-Za-z0-9]", "");
            lastWord  = parts[parts.length - 1].replaceAll("[^A-Za-z0-9]", "");
        }

        return (id + ":" + messageNumber + ":" + firstWord + lastWord)
                .toUpperCase();
    }


    public String getMessageId()     { return messageId; }
    public int    getMessageNumber() { return messageNumber; }
    public String getSender()        { return sender; }
    public String getRecipient()     { return recipient; }
    public String getMessageBody()   { return messageBody; }
    public String getMessageHash()   { return messageHash; }
    public String getStatus()        { return status; }

    public void setMessageId(String v)     { this.messageId = v; }
    public void setMessageNumber(int v)    { this.messageNumber = v; }
    public void setSender(String v)        { this.sender = v; }
    public void setRecipient(String v)     { this.recipient = v; }
    public void setMessageBody(String v)   { this.messageBody = v; }
    public void setMessageHash(String v)   { this.messageHash = v; }
    public void setStatus(String v)        { this.status = v; }

    @Override
    public String toString() {
        String from = (sender == null) ? "(unknown)" : sender;
        return String.format(
            "ID: %s | #%d | From: %s | To: %s | Status: %s | Hash: %s | Body: %s",
            messageId, messageNumber, from, recipient, status, messageHash, messageBody);
    }
}