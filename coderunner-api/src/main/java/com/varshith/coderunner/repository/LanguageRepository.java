package com.varshith.coderunner.repository;

import com.varshith.coderunner.models.LanguageModel;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/*
 * Repository extends default JPA Repository.
 * */
@Repository
public interface LanguageRepository extends JpaRepository<@NonNull LanguageModel, @NonNull Integer> {
    // Get language existence by language name using direct spring jpa methods.
     boolean existsByLanguageName(String languageName);
    // Get Language by its names provide normalized names in the string which would be recognized by the database, may throw
    // not found if language name is not normalized.
     LanguageModel findByLanguageName(String languageName);
}
