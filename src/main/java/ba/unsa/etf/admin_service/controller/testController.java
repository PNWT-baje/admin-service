package ba.unsa.etf.admin_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class testController {

    @GetMapping("/")
    public String test() {
        return "Hello darkness my old friend!";
    }
}
