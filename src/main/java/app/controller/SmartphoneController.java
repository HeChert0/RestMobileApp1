package app.controller;

import app.cache.SmartphoneCache;
import app.dto.SmartphoneDto;
import app.mapper.SmartphoneMapper;
import app.models.Smartphone;
import app.service.SmartphoneService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/phones")
public class SmartphoneController {

    private final SmartphoneService smartphoneService;
    private final SmartphoneMapper smartphoneMapper;
    private final SmartphoneCache smartphoneCache;

    @Autowired
    public SmartphoneController(SmartphoneService smartphoneService,
                                SmartphoneMapper smartphoneMapper,
                                SmartphoneCache smartphoneCache) {
        this.smartphoneService = smartphoneService;
        this.smartphoneMapper = smartphoneMapper;
        this.smartphoneCache = smartphoneCache;
    }

    @GetMapping
    public List<SmartphoneDto> getAllSmartphones() {
        List<Smartphone> smartphones = smartphoneService.getAllSmartphones();
        return smartphoneMapper.toDtos(smartphones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmartphoneDto> getSmartphoneById(@PathVariable Long id) {
        return smartphoneService.getSmartphoneById(id)
                .map(smartphoneMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SmartphoneDto> createSmartphone(
            @RequestBody SmartphoneDto smartphoneDto) {
        Smartphone savedPhone = smartphoneService.createSmartphone(smartphoneDto);
        return ResponseEntity.ok(smartphoneMapper.toDto(savedPhone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSmartphone(@PathVariable Long id) {
        if (smartphoneService.getSmartphoneById(id).isPresent()) {
            smartphoneService.deleteSmartphone(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SmartphoneDto> updateSmartphone(
            @PathVariable Long id, @RequestBody SmartphoneDto smartphoneDto) {
        Smartphone updatedPhone = smartphoneService.updateSmartphone(id, smartphoneDto);
        return ResponseEntity.ok(smartphoneMapper.toDto(updatedPhone));
    }


    @GetMapping("/filter")
    public List<Smartphone> filterSmartphones(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Double price) {
        return smartphoneService.getFilteredSmartphones(brand, model, price);
    }

    @GetMapping("/by-customer")
    public ResponseEntity<List<SmartphoneDto>> getPhonesByCustomerName(
            @RequestParam String customerName,
            @RequestParam(defaultValue = "false") boolean nativeQuery) {

        List<Smartphone> cached = smartphoneCache.getFromCache(customerName);
        if (cached != null) {
            return ResponseEntity.ok(smartphoneMapper.toDtos(cached));
        }

        List<Smartphone> smartphones;
        if (nativeQuery) {
            smartphones = smartphoneService.getPhonesByCustomerNameNative(customerName);
        } else {
            smartphones = smartphoneService.getPhonesByCustomerNameJpql(customerName);
        }

        smartphoneCache.putToCache(customerName, smartphones);

        return ResponseEntity.ok(smartphoneMapper.toDtos(smartphones));
    }
}
