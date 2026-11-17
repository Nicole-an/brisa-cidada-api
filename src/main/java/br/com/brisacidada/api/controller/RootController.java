package br.com.brisacidada.api.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Hidden
@Controller
public class RootController {

    @GetMapping("/")
    public RedirectView root() {
        return new RedirectView("/swagger-ui.html");
    }
}
