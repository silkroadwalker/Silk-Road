package com.silkroad.api;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the small data-holder classes nested inside
 * {@link ApiClient}: AdFilter (search criteria) and ApiResult
 * (login/signup response).
 *
 * Note: ApiClient's HTTP-calling methods (login, searchAds, ...) are
 * not unit tested here because they perform real network I/O against
 * the backend (http://localhost:8080) - that kind of call belongs in
 * an integration test, not a JUnit unit test.
 */
public class ApiClientDataHoldersTest {

    @Test
    public void adFilterFieldsAreNullByDefault() {
        ApiClient.AdFilter filter = new ApiClient.AdFilter();

        assertNull(filter.keyword);
        assertNull(filter.categoryId);
        assertNull(filter.cityId);
        assertNull(filter.minPrice);
        assertNull(filter.maxPrice);
    }

    @Test
    public void adFilterFieldsCanBeSetDirectly() {
        ApiClient.AdFilter filter = new ApiClient.AdFilter();
        filter.keyword = "laptop";
        filter.categoryId = 2L;
        filter.cityId = 1L;
        filter.minPrice = "100";
        filter.maxPrice = "1000";

        assertEquals("laptop", filter.keyword);
        assertEquals(Long.valueOf(2L), filter.categoryId);
        assertEquals(Long.valueOf(1L), filter.cityId);
        assertEquals("100", filter.minPrice);
        assertEquals("1000", filter.maxPrice);
    }

    @Test
    public void apiResultDefaultsToNotSuccessful() {
        ApiClient.ApiResult result = new ApiClient.ApiResult();

        assertFalse(result.success);
        assertNull(result.message);
        assertNull(result.token);
    }

    @Test
    public void adminUserFieldsCanBeSetDirectly() {
        ApiClient.AdminUser user = new ApiClient.AdminUser();
        user.id = 7L;
        user.username = "ali92";
        user.fullName = "Ali Rezaei";
        user.status = "ACTIVE";

        assertEquals(Long.valueOf(7L), user.id);
        assertEquals("ali92", user.username);
        assertEquals("Ali Rezaei", user.fullName);
        assertEquals("ACTIVE", user.status);
    }
}
