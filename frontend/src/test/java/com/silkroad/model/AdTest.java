package com.silkroad.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Ad}.
 * Covers getters/setters and the custom toString() representation
 * used when an Ad is rendered as plain text (e.g. in lists/combo boxes).
 */
public class AdTest {

    private Ad ad;

    @Before
    public void setUp() {
        ad = new Ad();
    }

    @Test
    public void gettersReturnValuesPassedToSetters() {
        List<String> images = Arrays.asList("img1.png", "img2.png");

        ad.setId(1L);
        ad.setTitle("iPhone 13");
        ad.setDescription("Like new, barely used");
        ad.setPrice(1200.0);
        ad.setCity("Tehran");
        ad.setCategory("Electronics");
        ad.setStatus("ACTIVE");
        ad.setSellerUsername("ali92");
        ad.setImageUrls(images);
        ad.setCreatedAt("2026-01-01T10:00:00");
        ad.setThumbnailUrl("img1.png");

        assertEquals(Long.valueOf(1L), ad.getId());
        assertEquals("iPhone 13", ad.getTitle());
        assertEquals("Like new, barely used", ad.getDescription());
        assertEquals(1200.0, ad.getPrice(), 0.0001);
        assertEquals("Tehran", ad.getCity());
        assertEquals("Electronics", ad.getCategory());
        assertEquals("ACTIVE", ad.getStatus());
        assertEquals("ali92", ad.getSellerUsername());
        assertEquals(images, ad.getImageUrls());
        assertEquals("2026-01-01T10:00:00", ad.getCreatedAt());
        assertEquals("img1.png", ad.getThumbnailUrl());
    }

    @Test
    public void newAdHasNullDefaults() {
        assertNull(ad.getId());
        assertNull(ad.getTitle());
        assertNull(ad.getCity());
        assertNull(ad.getImageUrls());
        // primitive double defaults to 0.0
        assertEquals(0.0, ad.getPrice(), 0.0001);
    }

    @Test
    public void toStringContainsTitlePriceAndCity() {
        ad.setTitle("Bicycle");
        ad.setPrice(150.5);
        ad.setCity("Shiraz");

        String text = ad.toString();

        assertEquals("Bicycle - 150.5 (Shiraz)", text);
    }
}
