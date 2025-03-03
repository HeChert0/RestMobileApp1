package app.controller;

import app.entities.Phone;
import app.service.PhoneService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/phones")
public class PhoneController {

    private final PhoneService phoneService;
    private static final Set<String> VALID_PARAMS = Set.of("brand", "model", "price");

    public PhoneController(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @GetMapping("/{id}")
    public Phone getPhoneById(@PathVariable long id) {
        return phoneService.getPhoneById(id);
    }

    @GetMapping
    public List<Phone> getPhones(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer price,
            HttpServletRequest request) {

        for (String param : request.getParameterMap().keySet()) {
            if (!VALID_PARAMS.contains(param)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unknown parameter: " + param);
            }
        }

        return phoneService.getFilteredPhones(brand, model, price);
    }
}
