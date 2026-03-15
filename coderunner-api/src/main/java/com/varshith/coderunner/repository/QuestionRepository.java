package com.varshith.coderunner.repository;


import com.varshith.coderunner.models.QuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionModel, String> {
}
