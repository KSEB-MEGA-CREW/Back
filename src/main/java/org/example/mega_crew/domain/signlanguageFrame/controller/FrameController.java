package org.example.mega_crew.domain.signlanguageFrame.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguageFrame.service.AsyncAIProxyService;
import org.example.mega_crew.domain.signlanguageFrame.service.FrameValidationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signlanguage")
@RequiredArgsConstructor
@Slf4j
public class FrameController {
    private final FrameValidationService frameValidationService;
    private final AsyncAIProxyService asyncAIProxyService;

}
