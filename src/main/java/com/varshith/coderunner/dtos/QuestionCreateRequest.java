package com.varshith.coderunner.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCreateRequest {
    private String title;
    private String markdown;
    private double time_limit;
    private int memory_limit;
    private List<String> topics;
    private String difficulty;
    private MultipartFile test_cases;

}
