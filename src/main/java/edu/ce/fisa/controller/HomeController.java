package edu.ce.fisa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/hello")
    public String getHello() {
        return "Hello GET!";
    }

    @PostMapping("/hello")
    public String postHello(@RequestBody String name) {
        return "Hello POST! " + name;
    }
}