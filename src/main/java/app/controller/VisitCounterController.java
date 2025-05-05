package app.controller;

import app.service.VisitCounterService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class VisitCounterController {
    private final VisitCounterService visitCounterService;

    @GetMapping("/count")
    public ResponseEntity<Integer> getVisitCount(@RequestParam String url) {
        return ResponseEntity.ok(visitCounterService.getVisitCount(url));
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Integer>> getTotalVisitCount() {
        return ResponseEntity.ok(
                Map.of("total", visitCounterService.getTotalVisitCount())
        );
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Integer>> getAllVisitCounts() {
        return ResponseEntity.ok(visitCounterService.getAllVisitCounts());
    }
}