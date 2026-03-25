package com.varshith.coderunner_workers.repository;

import com.varshith.coderunner_workers.models.QuestionModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository  extends JpaRepository<QuestionModel, String> {

    @Transactional
    @Modifying
    @Query("UPDATE QuestionModel q SET q.submissions = q.submissions + 1, " +
            "q.accepted = q.accepted + :acceptedInc WHERE q.questionId = :id")
    void incrementStats(@Param("id") String id, @Param("acceptedInc") int acceptedInc);
}
