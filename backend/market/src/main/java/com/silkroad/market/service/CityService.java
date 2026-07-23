package com.silkroad.market.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.silkroad.market.dto.city.CityResponse;
import com.silkroad.market.repository.CityRepository;

/**
 * Service class responsible for retrieving city data.
 * 
 * <p>
 * This service provides read-only access to the list of available cities
 * in the system. Cities are used primarily for advertisement location
 * information and filtering.
 * </p>
 * 
 * <p>
 * This is a lightweight service that directly maps city entities to
 * response DTOs without any complex business logic.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see City
 * @see CityRepository
 */
@Service
public class CityService {

    private final CityRepository cityRepository;

    /**
     * Constructs a new CityService with the required dependency.
     * 
     * @param cityRepository repository for city data retrieval
     */
    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    /**
     * Retrieves a list of all available cities.
     * 
     * <p>
     * This method returns all cities in the system, typically used for
     * populating dropdown menus and filters in the user interface.
     * </p>
     * 
     * @return a list of city response DTOs containing ID and name
     */
    public List<CityResponse> getCities() {

        return cityRepository.findAll()
                .stream()
                .map(city -> new CityResponse(
                        city.getId(),
                        city.getName()))
                .toList();
    }
}