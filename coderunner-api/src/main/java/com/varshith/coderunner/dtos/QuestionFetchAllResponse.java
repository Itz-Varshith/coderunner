package com.varshith.coderunner.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionFetchAllResponse {
    String questionId;
    String questionTitle;
    double acceptanceRate;
    String difficulty;
}
