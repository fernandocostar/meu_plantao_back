package com.example.loginauthapi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class JsonCitiesService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, String>> loadCities() throws IOException {
        // Load the JSON file from resources as InputStream
        ClassPathResource resource = new ClassPathResource("data/cities.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, String>>>() {});
        }
    }

    public boolean isCityValid(String cityName, String stateId) throws IOException {
        List<Map<String, String>> cities = loadCities();
        return cities.stream()
                .anyMatch(city -> city.get("Nome").equals(cityName) && city.get("Estado").equals(stateId));
    }
}
