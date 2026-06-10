package com.varshith.coderunner.repository;

import com.varshith.coderunner.models.SubmissionModel;
import com.varshith.coderunner.models.UserModel;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Repository file for Submission Model extends default JPA Repository, can add custom methods if needed.
 * */
@Repository
public interface SubmissionRepository extends JpaRepository<@NonNull SubmissionModel, @NonNull Long> {
    List<SubmissionModel> findByUserId(String userId);

    // Most recent 50 submissions for a user, used by the cached all-submissions endpoint.
    List<SubmissionModel> findTop50ByUserIdOrderBySubmittedAtDesc(String userId);

    String user(UserModel user);
}
