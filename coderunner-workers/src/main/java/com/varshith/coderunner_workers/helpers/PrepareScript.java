package com.varshith.coderunner_workers.helpers;


import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PrepareScript {
    public String makeScript(String scriptName, SubmissionModel submission){
        InputStream is=getClass().getClassLoader().getResourceAsStream("scripts/"+scriptName);
        String script="";

        try {
            if(is==null){
                log.info("Script file for cpp not found");
                return "Script file not found";
            }
            script=new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException err) {
            log.error("Failed to read script", err);
            return "IO Exception occurred";
        }
        String tl= submission.getQuestion().getTimeLimit() +"s";
        String ml=String.valueOf(submission.getQuestion().getMemoryLimit());
        script=script.replace("{{TIME_LIMIT}}", tl);
        script=script.replace("{{MEMORY_LIMIT}}", ml);
        return script;
    }


}
