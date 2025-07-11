package org.example.mega_crew.domain.webcam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// test page controller
@Controller
public class WebcamViewController {

    @GetMapping("/webcam")
    public String webcamPage(Model model) {
        model.addAttribute("pageTitle", "웹캠 데이터 로컬 저장 테스트");
        return "webcam";
    }

    @GetMapping("/webcam/test")
    public String webcamTestPage(Model model) {
        model.addAttribute("pageTitle", "웹캠 테스트");
        model.addAttribute("testMode", true);
        return "webcam";
    }
}
