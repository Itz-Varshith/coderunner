package com.varshith.coderunner_workers.models;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class QuestionModel {
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
    @Id
    private String questionId;
    private String title;
    private String markdown;
    private String testcasesPath;
    private int testcasesCount;
    private int submissions;
    private int accepted;
    private int timeLimit;
    private int memoryLimit;
    private List<String> topics;
    private Difficulty difficulty;
    private boolean customJudge;
}
