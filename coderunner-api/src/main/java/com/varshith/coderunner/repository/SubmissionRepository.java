package com.varshith.coderunner.repository;

import com.varshith.coderunner.models.SubmissionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionModel, Long> {
}
