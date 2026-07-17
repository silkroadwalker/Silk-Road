package com.silkroad.market.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.silkroad.market.dto.city.CityResponse;
import com.silkroad.market.repository.CityRepository;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<CityResponse> getCities() {

        return cityRepository.findAll()
                .stream()
                .map(city -> new CityResponse(
                        city.getId(),
                        city.getName()))
                .toList();
    }
}