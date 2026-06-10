package com.varshith.coderunner.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionFetchAllResponse {
    String questionId;
    String questionTitle;
    double acceptanceRate;
    String difficulty;
}
