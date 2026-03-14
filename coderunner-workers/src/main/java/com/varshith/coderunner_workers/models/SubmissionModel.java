package com.varshith.coderunner_workers.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "id_index", columnList = "submissionId")
})
public class SubmissionModel {

    public enum Status {
        PENDING,
        RUNNING,
        ACCEPTED,
        WRONG_ANSWER,
        TLE,
        MLE,
        RUNTIME_ERROR,
        COMPILATION_ERROR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long submissionId;

    @ManyToOne
    @JoinColumn(name = "languageId")
    private LanguageModel language;

    @ManyToOne
    @JoinColumn(name="questionId")
    private QuestionModel question;

    @ManyToOne
    @JoinColumn(name="id")
    private UserModel user;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int timeTaken;
    private int memoryTaken;

    private long submittedAt;
    private String judgeMessage;
    @PrePersist
    public void setDefaultValues() {
        this.submittedAt = System.currentTimeMillis();
        this.judgeMessage = "Submission validation pending";



    }


}
