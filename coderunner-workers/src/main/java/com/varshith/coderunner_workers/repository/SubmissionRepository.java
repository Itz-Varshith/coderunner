package com.varshith.coderunner_workers.repository;


import com.varshith.coderunner_workers.models.SubmissionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionModel, Long> {
}
