package com.varshith.coderunner.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class BaseController {
    @GetMapping("/")
    public String baseMapping(){
        return "Coderunner Server successfully started";
    }
}
