package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.Glossary;
import com.harekrishna.translator.service.GlossaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/glossary")
public class GlossaryController {

    private final GlossaryService glossaryService;

    public GlossaryController(GlossaryService glossaryService) {
        this.glossaryService = glossaryService;
    }

    @GetMapping
    public List<Glossary> getAll() {
        return glossaryService.getAll();
    }

    @PostMapping
    public Glossary create(@RequestBody Glossary glossary) {
        return glossaryService.save(glossary);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        glossaryService.delete(id);
        return ResponseEntity.ok().build();
    }
}
