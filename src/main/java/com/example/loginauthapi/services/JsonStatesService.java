package com.example.loginauthapi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class JsonStatesService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, String>> loadStates() throws IOException {
        // Load the JSON file from resources
        ClassPathResource resource = new ClassPathResource("data/states.json");
        return objectMapper.readValue(resource.getFile(), new TypeReference<List<Map<String, String>>>() {});
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
