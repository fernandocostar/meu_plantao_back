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
public class JsonStatesService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, String>> loadStates() throws IOException {
        // Load the JSON file from resources as InputStream
        ClassPathResource resource = new ClassPathResource("data/states.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, String>>>() {});
        }
    }

    public String getStateIdBySigla(String sigla) throws IOException {
        List<Map<String, String>> states = loadStates();
        return states.stream()
                .filter(state -> state.get("Sigla").equals(sigla))
                .map(state -> state.get("ID"))
                .findFirst()
                .orElse(null);
    }

    public boolean isStateSiglaValid(String sigla) throws IOException {
        List<Map<String, String>> states = loadStates();
        return states.stream().anyMatch(state -> state.get("Sigla").equals(sigla));
    }
}
