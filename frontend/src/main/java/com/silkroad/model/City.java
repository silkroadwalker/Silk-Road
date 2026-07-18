package com.silkroad.model;

public class City {
    private Long id;
    private String name;

    public Long getId() { return id; }
    public String getName() { return name; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() { return name; }
}
