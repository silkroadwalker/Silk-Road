package com.silkroad.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class CityTest {

    @Test
    public void gettersReturnValuesPassedToSetters() {
        City city = new City();
        city.setId(3L);
        city.setName("Mashhad");

        assertEquals(Long.valueOf(3L), city.getId());
        assertEquals("Mashhad", city.getName());
    }

    @Test
    public void toStringReturnsName() {
        City city = new City();
        city.setName("Tabriz");

        assertEquals("Tabriz", city.toString());
    }
}
