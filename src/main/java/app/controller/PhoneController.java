package app.controller;

import app.entities.Phone;
import app.service.PhoneService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/phones")
public class PhoneController {

    private final PhoneService phoneService;

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
            @RequestParam(required = false) Integer price) {
        return phoneService.getFilteredPhones(brand, model, price);
    }
}
