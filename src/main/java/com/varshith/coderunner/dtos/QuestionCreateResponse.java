package com.varshith.coderunner.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCreateResponse {
    private String questionId;
    private String questionMarkdown;
}
