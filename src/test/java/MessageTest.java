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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageTest {

    @Test
    @DisplayName("Message ID has exactly 10 characters, all digits")
    public void idFormat() {
        for (int i = 0; i < 20; i++) {
            String id = Message.generateMessageId();
            assertEquals(10, id.length());
            assertTrue(id.matches("[0-9]{10}"));
        }
    }

    /**
     * Part 2 Question example: Keegan's dinner message hashes to 00:0:HITONIGHT.
     * Punctuation in "Hi Mike," and "tonight?" is stripped before the
     * concatenation, so the hash word is HITONIGHT, not HI,MIKETONIGHT?.
     */
    @Test
    @DisplayName("Part 2 example: Keegan -> Mike dinner message -> 00:0:HITONIGHT")
    public void hashKeeganMikeDinner() {
        Message m = new Message("0012345678", 0, "+27821234567",
                                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("00:0:HITONIGHT", m.getMessageHash());
    }

    @Test
    @DisplayName("Part 2 example: Mike -> Keegan payment message -> 01:1:HIPAYMENT")
    public void hashMikeKeeganPayment() {
        Message m = new Message("0123456789", 1, "+27821234567",
                                "Hi Keegan, did you receive the payment?");
        assertEquals("01:1:HIPAYMENT", m.getMessageHash());
    }

    @Test
    @DisplayName("Hash handles single-word body")
    public void hashSingleWord() {
        Message m = new Message("9876543210", 1, "+27821234567", "hi");
        assertEquals("98:1:HIHI", m.getMessageHash());
    }

    @Test
    @DisplayName("Hash handles empty body")
    public void hashEmpty() {
        Message m = new Message("5555555555", 2, "+27821234567", "");
        assertEquals("55:2:", m.getMessageHash());
    }

    @Test
    @DisplayName("Hash format is stable across 100 randomly-ID'd messages")
    public void hashFormatLoop() {
        Pattern p = Pattern.compile("^\\d{2}:\\d+:[A-Z0-9]*$");
        for (int i = 0; i < 100; i++) {
            Message m = new Message(Message.generateMessageId(), i,
                                    "+27821234567",
                                    "Hi Mike, can you join us for dinner tonight?");
            assertTrue(p.matcher(m.getMessageHash()).matches(),
                       "Unexpected hash: " + m.getMessageHash());
            assertTrue(m.getMessageHash().endsWith(":HITONIGHT"),
                       "Dinner body should always end :HITONIGHT, got " + m.getMessageHash());
        }
    }
}