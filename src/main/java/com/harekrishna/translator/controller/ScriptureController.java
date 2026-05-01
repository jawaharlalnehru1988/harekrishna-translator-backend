package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.Scripture;
import com.harekrishna.translator.repository.ScriptureRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scriptures")
public class ScriptureController {

    private final ScriptureRepository scriptureRepository;

    public ScriptureController(ScriptureRepository scriptureRepository) {
        this.scriptureRepository = scriptureRepository;
    }

    @GetMapping
    public List<Scripture> getAll() {
        return scriptureRepository.findAll();
    }

    @PostMapping
    public Scripture create(@RequestBody Scripture scripture) {
        return scriptureRepository.save(scripture);
    }
}
