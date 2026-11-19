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
import com.chatapp.service.impl.MessageManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MessageManagerTest {

    private MessageManagerImpl mgr;

    @BeforeEach
    public void setUp() {
        mgr = new MessageManagerImpl(10, "test_stored_messages.json");
    }


    @Test
    @DisplayName("Compose generates a 10-digit ID and increments the number")
    public void composeGeneratesIdAndNumber() {
        Message m1 = mgr.composeMessage("+27821234567", "hello world");
        Message m2 = mgr.composeMessage("+27821234567", "second message");

        assertEquals(10, m1.getMessageId().length());
        assertEquals(1,  m1.getMessageNumber());
        assertEquals(2,  m2.getMessageNumber());
    }


    @Test
    @DisplayName("Compose of the Mike/Keegan dinner message yields :1:HITONIGHT hash suffix")
    public void composePart2MikeKeeganDinner() {
        Message m = mgr.composeMessage("+27821234567",
                "Hi Mike, can you join us for dinner tonight?");
        assertTrue(m.getMessageHash().matches("\\d{2}:1:HITONIGHT"),
                   "Got " + m.getMessageHash());
    }


    @Test
    @DisplayName("Compose of the Keegan/Mike payment message yields :2:HIPAYMENT hash suffix")
    public void composePart2KeeganMikePayment() {
        mgr.composeMessage("+27821234567", "Hi Mike, can you join us for dinner tonight?");
        Message m = mgr.composeMessage("+27821234567",
                "Hi Keegan, did you receive the payment?");
        assertTrue(m.getMessageHash().matches("\\d{2}:2:HIPAYMENT"),
                   "Got " + m.getMessageHash());
    }


    @Test
    @DisplayName("Composed hashes always match \\d{2}:\\d+:[A-Z0-9]* (loop)")
    public void composeHashLoop() {
        Pattern p = Pattern.compile("^\\d{2}:\\d+:[A-Z0-9]*$");
        for (int i = 0; i < 50; i++) {
            Message m = mgr.composeMessage("+27821234567",
                    "Hi Keegan, did you receive the payment?");
            assertTrue(p.matcher(m.getMessageHash()).matches(),
                       "Bad hash: " + m.getMessageHash());
        }
    }


    @Test
    @DisplayName("Send sets status SENT and adds the message to the sent array")
    public void sendAndCount() {
        Message m = mgr.composeMessage("+27821234567", "hi");
        mgr.sendMessage(m);
        assertEquals(1, mgr.getSentMessages().length);
        assertEquals("SENT", m.getStatus());
    }

    @Test
    @DisplayName("Disregard sets status DISREGARDED")
    public void disregardAndCount() {
        Message m = mgr.composeMessage("+27821234567", "hi");
        mgr.disregardMessage(m);
        assertEquals("DISREGARDED", m.getStatus());
    }

    @Test
    @DisplayName("Store sets status STORED")
    public void storeAndCount() {
        Message m = mgr.composeMessage("+27821234567", "persist me");
        mgr.storeMessage(m);
        assertEquals("STORED", m.getStatus());
    }


    @Test
    @DisplayName("Message body over 250 chars is rejected")
    public void longBodyRejected() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 251; i++) sb.append('a');
        assertEquals("Please enter a message of less than 250 characters",
                     mgr.validateMessageBody(sb.toString()));
    }

    @Test
    @DisplayName("Message body 250 chars or fewer is accepted")
    public void okBodyAccepted() {
        assertEquals("Message Sent", mgr.validateMessageBody("hello"));
    }


    @Test
    @DisplayName("Recipient in +27 SA international format is accepted")
    public void recipientOk() {
        assertEquals("Recipient accepted", mgr.validateRecipient("+27821234567"));
        assertEquals("Recipient accepted", mgr.validateRecipient("+27721234567"));
        assertEquals("Recipient accepted", mgr.validateRecipient("+27821234568"));
    }

    @Test
    @DisplayName("Recipient outside +27 SA format is rejected (including local 0 numbers)")
    public void recipientBad() {
        assertEquals("Recipient number incorrectly formatted.",
                     mgr.validateRecipient("12345"));
        assertEquals("Recipient number incorrectly formatted.",
                     mgr.validateRecipient("+15551234567"));
        // Part 2 Question: local numbers are NOT valid recipients
        assertEquals("Recipient number incorrectly formatted.",
                     mgr.validateRecipient("0821234567"));
        // 9 is not in the required [6-8] range
        assertEquals("Recipient number incorrectly formatted.",
                     mgr.validateRecipient("+27921234567"));
    }
}