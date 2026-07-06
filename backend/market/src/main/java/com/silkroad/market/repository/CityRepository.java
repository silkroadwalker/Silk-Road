package com.silkroad.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.silkroad.market.entity.City;

public interface CityRepository extends JpaRepository<City, Long> {
}