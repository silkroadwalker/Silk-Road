package com.silkroad.market.dto.city;

public class CityResponse {

    private Long id;
    private String name;

    public CityResponse() {
    }

    public CityResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}