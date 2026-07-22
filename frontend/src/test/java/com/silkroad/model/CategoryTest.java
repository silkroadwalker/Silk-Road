package com.silkroad.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Category}.
 *
 * Covers the fields that support the hierarchical category structure
 * (parentId + hasChildren), e.g. Real Estate -> Apartment / House / Skyscraper.
 */
public class CategoryTest {

    @Test
    public void topLevelCategoryHasNullParent() {
        Category realEstate = new Category();
        realEstate.setId(1L);
        realEstate.setName("Real Estate");
        realEstate.setParentId(null);
        realEstate.setHasChildren(true);

        assertNull(realEstate.getParentId());
        assertTrue(realEstate.isHasChildren());
        assertEquals("Real Estate", realEstate.getName());
    }

    @Test
    public void subCategoryReferencesItsParent() {
        Category apartment = new Category();
        apartment.setId(2L);
        apartment.setName("Apartment");
        apartment.setParentId(1L);
        apartment.setHasChildren(false);

        assertEquals(Long.valueOf(1L), apartment.getParentId());
        assertFalse(apartment.isHasChildren());
    }

    @Test
    public void toStringReturnsName() {
        Category category = new Category();
        category.setName("Skyscraper");

        assertEquals("Skyscraper", category.toString());
    }

    @Test
    public void hasChildrenDefaultsToFalse() {
        Category category = new Category();
        assertFalse(category.isHasChildren());
    }
}
