package com.varshith.coderunner.repository;

import com.varshith.coderunner.models.LanguageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageModel, Integer> {
    public boolean existsByLanguageName(String languageName);
    public LanguageModel findByLanguageName(String languageName);
}
