package com.varshith.coderunner.repository;

import com.varshith.coderunner.models.SubmissionModel;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository file for Submission Model extends default JPA Repository, can add custom methods if needed.
 * */
@Repository
public interface SubmissionRepository extends JpaRepository<@NonNull SubmissionModel, @NonNull Long> {
}
