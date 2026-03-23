package com.varshith.coderunner.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class QuestionFetchAllResponse {
    Map<String, String> question;
}
