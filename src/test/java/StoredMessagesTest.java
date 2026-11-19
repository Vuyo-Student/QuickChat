/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author SevenC
 */

package com.chatapp;

import com.chatapp.model.Message;
import com.chatapp.service.impl.MessageManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


public class StoredMessagesTest {

    @TempDir
    Path tempDir;

    private MessageManagerImpl mgr;
    private String jsonFile;

    @BeforeEach
    public void setUp() {
        jsonFile = tempDir.resolve("stored.json").toString();
        mgr = new MessageManagerImpl(10, jsonFile);
    }


    @Test
    @DisplayName("Store then reload from JSON (persistence survives restart)")
    public void storeThenLoad() {
        Message m1 = mgr.composeMessage("k_yl", "+27821234567", "Hi Mike, dinner tonight?");
        Message m2 = mgr.composeMessage("k_yl", "+27821234567", "Hi Keegan, payment received.");
        mgr.storeMessage(m1);
        mgr.storeMessage(m2);

        // New manager, same file — Simulates a fresh app start
        MessageManagerImpl fresh = new MessageManagerImpl(10, jsonFile);
        fresh.loadStoredFromJson();

        assertEquals(2, fresh.getStoredMessages().length);
    }

    @Test
    @DisplayName("loadStoredFromJson is a no-op when the file does not exist")
    public void loadMissingFile() {
        MessageManagerImpl fresh = new MessageManagerImpl(10, jsonFile);
        fresh.loadStoredFromJson();
        assertEquals(0, fresh.getStoredMessages().length);
    }

    @Test
    @DisplayName("getStoredMessages returns a defensive copy")
    public void getStoredMessagesIsCopy() {
        Message m = mgr.composeMessage("k_yl", "+27821234567", "test");
        mgr.storeMessage(m);

        Message[] copy = mgr.getStoredMessages();
        copy[0] = null;
        assertEquals(1, mgr.getStoredMessages().length);
    }

    @Test
    @DisplayName("getLongestStoredMessage returns the message with the longest body")
    public void longestMessage() {
        Message shortMsg = mgr.composeMessage("k_yl", "+27821234567", "short");
        Message longMsg  = mgr.composeMessage("k_yl", "+27821234567",
                                              "a much longer message body here");
        Message midMsg   = mgr.composeMessage("k_yl", "+27821234567", "medium length");
        mgr.storeMessage(shortMsg);
        mgr.storeMessage(longMsg);
        mgr.storeMessage(midMsg);

        assertEquals(longMsg, mgr.getLongestStoredMessage());
    }

    @Test
    @DisplayName("getLongestStoredMessage returns null when store is empty")
    public void longestMessageEmpty() {
        assertNull(mgr.getLongestStoredMessage());
    }

    @Test
    @DisplayName("searchByMessageId returns the matching message")
    public void searchById() {
        Message m = mgr.composeMessage("k_yl", "+27821234567", "find me");
        mgr.storeMessage(m);

        assertEquals(m, mgr.searchByMessageId(m.getMessageId()));
    }

    @Test
    @DisplayName("searchByMessageId returns null when not found")
    public void searchByIdNotFound() {
        assertNull(mgr.searchByMessageId("doesnotexist"));
    }

    @Test
    @DisplayName("searchByRecipient returns all matching messages")
    public void searchByRecipient() {
        Message a = mgr.composeMessage("k_yl", "+27821234567", "first");
        Message b = mgr.composeMessage("k_yl", "+27821234567", "second");
        Message c = mgr.composeMessage("k_yl", "+27831234567", "third");
        mgr.storeMessage(a);
        mgr.storeMessage(b);
        mgr.storeMessage(c);

        assertEquals(2, mgr.searchByRecipient("+27821234567").length);
        assertEquals(1, mgr.searchByRecipient("+27831234567").length);
        assertEquals(0, mgr.searchByRecipient("+27999999999").length);
    }

    @Test
    @DisplayName("deleteByHash removes the message and persists the change")
    public void deleteByHash() {
        Message m = mgr.composeMessage("k_yl", "+27821234567", "delete me");
        mgr.storeMessage(m);
        String hash = m.getMessageHash();

        assertTrue(mgr.deleteByHash(hash));
        assertEquals(0, mgr.getStoredMessages().length);

        // Reload from disk — Deletion must be persisted
        MessageManagerImpl fresh = new MessageManagerImpl(10, jsonFile);
        fresh.loadStoredFromJson();
        assertEquals(0, fresh.getStoredMessages().length);
    }

    @Test
    @DisplayName("deleteByHash returns false when hash is not found")
    public void deleteByHashNotFound() {
        assertFalse(mgr.deleteByHash("doesnotexist:0:HI"));
        assertFalse(mgr.deleteByHash(null));
    }

    @Test
    @DisplayName("buildReport includes every stored message and sender")
    public void reportIncludesAll() {
        Message m1 = mgr.composeMessage("k_yl", "+27821234567", "Hi Mike, dinner tonight?");
        Message m2 = mgr.composeMessage("k_yl", "+27821234567", "Hi Keegan, payment received.");
        mgr.storeMessage(m1);
        mgr.storeMessage(m2);

        String report = mgr.buildReport();
        assertTrue(report.contains(m1.getMessageId()));
        assertTrue(report.contains(m2.getMessageId()));
        assertTrue(report.contains("k_yl"));
        assertTrue(report.contains("+27821234567"));
    }

    @Test
    @DisplayName("buildReport handles the empty-store case cleanly")
    public void reportEmpty() {
        assertTrue(mgr.buildReport().contains("No stored messages"));
    }
}
