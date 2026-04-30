package com.harekrishna.translator.service;

import com.harekrishna.translator.model.Glossary;
import com.harekrishna.translator.repository.GlossaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GlossaryService {

    private final GlossaryRepository glossaryRepository;

    public GlossaryService(GlossaryRepository glossaryRepository) {
        this.glossaryRepository = glossaryRepository;
    }

    public List<Glossary> getAll() {
        return glossaryRepository.findAll();
    }

    public Glossary save(Glossary glossary) {
        return glossaryRepository.save(glossary);
    }

    public void delete(Long id) {
        glossaryRepository.deleteById(id);
    }

    public String getGlossaryPrompt() {
        try {
            List<Glossary> glossaryList = glossaryRepository.findAll();
            if (glossaryList.isEmpty()) {
                return "";
            }

            String terms = glossaryList.stream()
                    .map(g -> String.format("'%s' should be translated as '%s'", g.getSourceWord(), g.getTargetWord()))
                    .collect(Collectors.joining(", "));

            return "\n\nCRITICAL GLOSSARY (Follow these specific translations if encountered): " + terms + ".";
        } catch (Exception e) {
            System.err.println("Could not fetch glossary from DB: " + e.getMessage());
            return ""; // Fallback to empty prompt so the app doesn't crash
        }
    }
}
