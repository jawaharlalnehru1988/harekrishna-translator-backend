package com.harekrishna.translator.config;

import com.harekrishna.translator.model.Scripture;
import com.harekrishna.translator.repository.ScriptureRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ScriptureRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Scripture ramayana = new Scripture(null, "Ramayana", "Valmiki", "Kanda", "Sarga", null);
                Scripture mahabharata = new Scripture(null, "Mahabharata", "Vyasa", "Parva", "Adhyaya", null);
                Scripture bhagavadGita = new Scripture(null, "Bhagavad Gita", "Vyasa", "Chapter", "Verse", null);
                Scripture srimadBhagavatam = new Scripture(null, "Srimad Bhagavatam", "Vyasa", "Canto", "Chapter", null);
                
                repository.saveAll(List.of(ramayana, mahabharata, bhagavadGita, srimadBhagavatam));
                System.out.println("Default scriptures initialized.");
            }
        };
    }
}
