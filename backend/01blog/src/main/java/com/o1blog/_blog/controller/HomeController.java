package com.o1blog._blog.controller;

import org.springframework.stereotype.Controller;

@Controller
public class HomeController {
    public String Home() {
        return "home.html";
    }

}
