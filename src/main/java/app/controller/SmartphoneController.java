package app.controller;

import app.cache.SmartphoneCache;
import app.dto.SmartphoneDTO;
import app.mapper.SmartphoneMapper;
import app.models.Smartphone;
import app.service.SmartphoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<SmartphoneDTO> getAllSmartphones() {
        List<Smartphone> smartphones = smartphoneService.getAllSmartphones();
        return smartphoneMapper.toDtos(smartphones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmartphoneDTO> getSmartphoneById(@PathVariable Long id) {
        return smartphoneService.getSmartphoneById(id)
                .map(smartphoneMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SmartphoneDTO> createSmartphone(
            @RequestBody SmartphoneDTO smartphoneDTO) {
        Smartphone savedPhone = smartphoneService.createSmartphone(smartphoneDTO);
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
    public ResponseEntity<SmartphoneDTO> updateSmartphone(
            @PathVariable Long id, @RequestBody SmartphoneDTO smartphoneDTO) {
        Smartphone updatedPhone = smartphoneService.updateSmartphone(id, smartphoneDTO);
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
    public ResponseEntity<List<SmartphoneDTO>> getPhonesByCustomerName(
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
            smartphones = smartphoneService.getPhonesByCustomerNameJPQL(customerName);
        }

        smartphoneCache.putToCache(customerName, smartphones);

        return ResponseEntity.ok(smartphoneMapper.toDtos(smartphones));
    }
}
