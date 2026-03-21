package com.varshith.coderunner.repository;

import com.varshith.coderunner.models.UserModel;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Default user repository does not contain any attributes to search for only has UserId String init.
 * */
@Repository
public interface UserRepository extends JpaRepository<@NonNull UserModel, @NonNull String> {
}
