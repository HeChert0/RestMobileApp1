package app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Value("${logging.file.name}")
    private String logFilePath;

    @Operation(summary = "Get logs by date", description = "Retrieves logs for a specified date (format yyyy-MM-dd)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(responseCode = "500", description = "Error reading log file")
    })
    @GetMapping
    public ResponseEntity<?> getLogsByDate(@RequestParam String date) {
        try {
            LocalDate requestedDate = LocalDate.parse(date);
            List<String> lines = Files.lines(Paths.get(logFilePath))
                    .filter(line -> line.contains(requestedDate.toString()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(lines);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Use yyyy-MM-dd");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading log file: " + e.getMessage());
        }
    }

    @Operation(summary = "Download logs by date", description = "Downloads logs for a specified date as a text file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Log file downloaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(responseCode = "500", description = "Error reading log file")
    })
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogs(@RequestParam String date) {
        try {
            LocalDate requestedDate = LocalDate.parse(date);

            List<String> lines = Files.lines(Paths.get(logFilePath))
                    .filter(line -> line.contains(requestedDate.toString()))
                    .collect(Collectors.toList());

            String content = String.join(System.lineSeparator(), lines);
            ByteArrayResource resource = new ByteArrayResource(content.getBytes());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"logs-" + date + ".txt\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}
