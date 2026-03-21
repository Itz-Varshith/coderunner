package com.varshith.coderunner.repository;


import com.varshith.coderunner.models.QuestionModel;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository file for QuestionModel extends default JPA Repository, not custom functions added.
 * */
@Repository
public interface QuestionRepository extends JpaRepository<@NonNull QuestionModel,@NonNull String> {
}
