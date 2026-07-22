package com.silkroad.ui;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Session}.
 *
 * Session keeps its state in static fields (shared across the whole
 * app), so every test cleans up after itself in @After to avoid
 * leaking state into other tests.
 */
public class SessionTest {

    @After
    public void clearSessionAfterEachTest() {
        Session.clear();
    }

    @Test
    public void isLoggedInIsFalseByDefault() {
        assertFalse(Session.isLoggedIn());
        assertNull(Session.getToken());
    }

    @Test
    public void setStoresTokenUsernameAndRole() {
        Session.set("jwt-token-123", "ali92", "SELLER");

        assertEquals("jwt-token-123", Session.getToken());
        assertEquals("ali92", Session.getUsername());
        assertEquals("SELLER", Session.getRole());
        assertTrue(Session.isLoggedIn());
    }

    @Test
    public void clearResetsAllFields() {
        Session.set("jwt-token-123", "ali92", "SELLER");

        Session.clear();

        assertNull(Session.getToken());
        assertNull(Session.getUsername());
        assertNull(Session.getRole());
        assertFalse(Session.isLoggedIn());
    }

    @Test
    public void isLoggedInReflectsTokenPresenceOnly() {
        // even with a null token but a username set, a user should not be considered logged in
        Session.set(null, "ali92", "SELLER");

        assertFalse(Session.isLoggedIn());
    }
}
