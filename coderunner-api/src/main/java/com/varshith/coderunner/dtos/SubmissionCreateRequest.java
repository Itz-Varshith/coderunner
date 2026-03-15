package com.varshith.coderunner.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionCreateRequest {
    private String code;
    private String language;
    private String userId;
    private String questionId;
}
