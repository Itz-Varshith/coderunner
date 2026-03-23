package com.varshith.coderunner.dtos;

import com.varshith.coderunner.models.QuestionModel;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class QuestionFetchResponse {
    private QuestionModel questionModel;
}
