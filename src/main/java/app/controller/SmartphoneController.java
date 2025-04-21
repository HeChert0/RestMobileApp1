package app.controller;

import app.dto.SmartphoneDto;
import app.mapper.SmartphoneMapper;
import app.models.Smartphone;
import app.service.SmartphoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
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

    @Autowired
    public SmartphoneController(SmartphoneService smartphoneService,
                                SmartphoneMapper smartphoneMapper) {
        this.smartphoneService = smartphoneService;
        this.smartphoneMapper = smartphoneMapper;
    }

    @Operation(summary = "Get all smartphones",
            description = "Retrieves a list of all smartphones and caches each one individually")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Smartphones retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping
    public ResponseEntity<List<SmartphoneDto>> getAllSmartphones() {
        List<Smartphone> phones = smartphoneService.getAllSmartphones();
        List<SmartphoneDto> dtos = phones.stream()
                .map(smartphoneMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get smartphone by ID",
            description = "Retrieves a single smartphone by its ID using cache if available")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Smartphone found"),
        @ApiResponse(responseCode = "404", description = "Smartphone not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SmartphoneDto> getSmartphoneById(@PathVariable Long id) {
        return smartphoneService.getSmartphoneById(id)
                .map(smartphoneMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new smartphone",
            description = "Creates a new smartphone and caches it")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Smartphone created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<SmartphoneDto> createSmartphone(
            @RequestBody @Valid SmartphoneDto smartphoneDto) {
        Smartphone phone = smartphoneMapper.toEntity(smartphoneDto);
        Smartphone savedPhone = smartphoneService.saveSmartphone(phone);
        return ResponseEntity.ok(smartphoneMapper.toDto(savedPhone));
    }

    @Operation(summary = "Update a smartphone",
            description = "Updates an existing smartphone and updates its cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Smartphone updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Smartphone not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SmartphoneDto> updateSmartphone(
            @PathVariable Long id, @RequestBody @Valid SmartphoneDto smartphoneDto) {
        Smartphone phone = smartphoneMapper.toEntity(smartphoneDto);
        Smartphone updatedPhone = smartphoneService.updateSmartphone(id, phone);
        return ResponseEntity.ok(smartphoneMapper.toDto(updatedPhone));
    }

    @Operation(summary = "Delete a smartphone",
            description = "Deletes a smartphone and updates the cache accordingly")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Smartphone deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Smartphone not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSmartphone(@PathVariable Long id) {
        smartphoneService.deleteSmartphone(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Filter smartphones",
            description = "Retrieves a list of smartphones filtered by"
                    + " optional parameters: brand, model and price. "
                    + "Set nativeQuery=true to use a native SQL query; otherwise JPQL is used."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Smartphones filtered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<SmartphoneDto>> filterSmartphones(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Double price,
            @RequestParam(defaultValue = "false") boolean nativeQuery) {

        List<Smartphone> filtered = smartphoneService.filterSmartphones(brand,
                model, price, nativeQuery);
        List<SmartphoneDto> dtos = filtered.stream()
                .map(smartphoneMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<SmartphoneDto>> createSmartphonesBulk(
            @RequestBody List<SmartphoneDto> dtos) {
        List<SmartphoneDto> saved = dtos.stream()
                .map(smartphoneMapper::toEntity)
                .map(smartphoneService::saveSmartphone)
                .map(smartphoneMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(saved);
    }


}
