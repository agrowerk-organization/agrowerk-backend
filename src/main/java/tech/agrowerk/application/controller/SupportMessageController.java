package tech.agrowerk.application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.SupportMessageService;

@RestController
@RequestMapping("/support-messages")
public class SupportMessageController {
    private final SupportMessageService supportMessageService;

    public SupportMessageController(SupportMessageService supportMessageService) {
        this.supportMessageService = supportMessageService;
    }
}
