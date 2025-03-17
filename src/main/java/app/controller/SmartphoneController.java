package app.controller;

import app.dto.SmartphoneDto;
import app.mapper.SmartphoneMapper;
import app.models.Smartphone;
import app.service.SmartphoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/phones")
public class SmartphoneController {

    private final SmartphoneService smartphoneService;
    private final SmartphoneMapper smartphoneMapper;

    @Autowired
    public SmartphoneController(SmartphoneService smartphoneService, SmartphoneMapper smartphoneMapper){
        this.smartphoneService = smartphoneService;
        this.smartphoneMapper = smartphoneMapper;
    }

    @GetMapping
    public ResponseEntity<List<SmartphoneDto>> getAllSmartphones(){
        List<Smartphone> phones = smartphoneService.getAllSmartphones();
        List<SmartphoneDto> dtos = phones.stream().map(smartphoneMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmartphoneDto> getSmartphoneById(@PathVariable Long id){
        return smartphoneService.getSmartphoneById(id)
                .map(smartphoneMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SmartphoneDto> createSmartphone(@RequestBody SmartphoneDto smartphoneDto){
        Smartphone phone = smartphoneMapper.toEntity(smartphoneDto);
        Smartphone savedPhone = smartphoneService.saveSmartphone(phone);
        return ResponseEntity.ok(smartphoneMapper.toDto(savedPhone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SmartphoneDto> updateSmartphone(@PathVariable Long id, @RequestBody SmartphoneDto smartphoneDto){
        Smartphone phone = smartphoneMapper.toEntity(smartphoneDto);
        Smartphone updatedPhone = smartphoneService.updateSmartphone(id, phone);
        return ResponseEntity.ok(smartphoneMapper.toDto(updatedPhone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSmartphone(@PathVariable Long id){
        smartphoneService.deleteSmartphone(id);
        return ResponseEntity.noContent().build();
    }
}
