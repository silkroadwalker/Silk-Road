package com.silkroad.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link AdDetail}.
 *
 * AdDetail extends Ad (inheritance): an AdDetail IS-A Ad, plus extra
 * fields only relevant to the single-ad detail view (seller info,
 * rejection reason, average rating, submitter flag). These tests check
 * both the inherited behaviour and the subclass-only fields.
 */
public class AdDetailTest {

    private AdDetail detail;

    @Before
    public void setUp() {
        detail = new AdDetail();
    }

    @Test
    public void adDetailIsAnAd() {
        Ad asAd = detail;
        assertTrue(asAd instanceof AdDetail);
        assertTrue(asAd instanceof Ad);
    }

    @Test
    public void inheritedSettersAndGettersWork() {
        detail.setTitle("Villa in Shomal");
        detail.setPrice(500000);
        detail.setCity("Rasht");

        assertEquals("Villa in Shomal", detail.getTitle());
        assertEquals(500000, detail.getPrice(), 0.0001);
        assertEquals("Rasht", detail.getCity());
    }

    @Test
    public void subclassOnlyFieldsWork() {
        detail.setSellerFullName("Sara Ahmadi");
        detail.setSellerPhone("0912xxxxxxx");
        detail.setRejectionReason(null);
        detail.setSubmitter(true);
        detail.setAverageRating(4.5);

        assertEquals("Sara Ahmadi", detail.getSellerFullName());
        assertEquals("0912xxxxxxx", detail.getSellerPhone());
        assertNull(detail.getRejectionReason());
        assertTrue(detail.isSubmitter());
        assertEquals(Double.valueOf(4.5), detail.getAverageRating());
    }

    @Test
    public void submitterDefaultsToFalse() {
        assertFalse(detail.isSubmitter());
    }

    @Test
    public void toStringIsInheritedFromAd() {
        detail.setTitle("Laptop");
        detail.setPrice(999.0);
        detail.setCity("Isfahan");

        assertEquals("Laptop - 999.0 (Isfahan)", detail.toString());
    }
}
