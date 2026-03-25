package com.varshith.coderunner.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model file for Question, contains ID, markdown, title, testcasesPath on the server(can be replaced to a cloud based storage
 * based on need if and when we deploy), contains other simple variables for bounds in the question and other metadata
 * related to the question, and finally the testcases folder structure is as follows input/*.txt(*=consecutive integers,
 * verified by QuestionValidator.java), judge.cpp (Judge file for code checking) and finally judge_program (executable)
 * of the judge.cpp file, computed by the worker if compilation is not already done, Lazy caching technique.
 * (Future improvement on this model is to directly call worker service to do compilation while creating question to avoid
 * race conditions in code execution when there are multiple cache misses).
 * */

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
    @Column(columnDefinition = "TEXT")
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
