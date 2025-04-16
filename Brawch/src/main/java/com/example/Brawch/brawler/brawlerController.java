package com.example.Brawch.brawler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class brawlerController {
    @GetMapping("/brawler")
    public String brawler(){
        /*src/main/resources/templates/ 기본 템플릿 디렉토리 경로*/
        return "brawler/brawlerMain";
    }
    @GetMapping("/detail")
    public  String detail(){
        return "brawler/brawlerdetail";
    }
}
