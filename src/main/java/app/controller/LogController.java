package app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/logs")
@EnableAsync
public class LogController {

    @Value("${logging.file.name}")
    private String logFilePath;

    // Хранилище задач и статусов
    private final ConcurrentHashMap<String, LogTask> tasks = new ConcurrentHashMap<>();
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    @Data
    @AllArgsConstructor
    private static class LogTask {
        private String status; // "PROCESSING", "COMPLETED", "FAILED"
        private Path tempFile;
        private LocalDate date;
    }

    @Operation(summary = "Start async log generation", description = "Returns task ID for tracking")
    @PostMapping("/generate")
    @SneakyThrows
    public ResponseEntity<String> generateLogs(@RequestParam(required = false) String date) {
        LocalDate parsedDate = null;
        try {
            if (date != null && !date.isEmpty()) {
                parsedDate = LocalDate.parse(date);
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd");
        }

        LocalDate finalParsedDate1 = parsedDate;
        Optional<Map.Entry<String, LogTask>> existing = tasks.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue().getDate(), finalParsedDate1))
                .findFirst();

        if (existing.isPresent()) {
            LogTask t = existing.get().getValue();
            if ("PROCESSING".equals(t.getStatus())) {
                // повторный запрос на время ожидания
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Task for date " + (date != null ? date : "all") + " is still processing");
            } else {
                return ResponseEntity.ok(existing.get().getKey());
            }
        }

        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, new LogTask("PROCESSING", null, parsedDate));

        LocalDate finalParsedDate = parsedDate;
        asyncExecutor.submit(() -> {
            try {
                Thread.sleep(20000);
                ResponseEntity<?> resp = getLogsByDate(date);
                if (resp.getStatusCode() == HttpStatus.OK) {
                    @SuppressWarnings("unchecked")
                    List<String> lines = (List<String>) resp.getBody();
                    Path tmp = Files.createTempFile("logs-", ".txt");
                    Files.write(tmp, lines);
                    tasks.put(taskId, new LogTask("COMPLETED", tmp, finalParsedDate));
                } else {
                    tasks.put(taskId, new LogTask("FAILED", null, finalParsedDate));
                }
            } catch (Exception e) {
                tasks.put(taskId, new LogTask("FAILED", null, finalParsedDate));
            }
        });

        return ResponseEntity.accepted().body(taskId);
    }

    @GetMapping("/status/{taskId}")
    public ResponseEntity<?> getStatus(@PathVariable String taskId) {
        LogTask task = tasks.get(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", task.getStatus());

        resp.put("date", task.getDate() != null
                ? task.getDate().toString()
                : "all");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/download-async/{taskId}")
    public ResponseEntity<Resource> downloadAsync(@PathVariable String taskId) throws IOException {
        LogTask task = tasks.get(taskId);
        if (task == null) {
            // Нет такой задачи
            return ResponseEntity.notFound().build();
        }

        String status = task.getStatus();
        if (!"COMPLETED".equals(status)) {
            // Пока не готово — 202 Accepted с телом-описанием статуса
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(new ByteArrayResource(
                            ("Task " + taskId + " is " + status).getBytes()
                    ));
        }

        // Файл готов — берём из задачи
        Path tmpFile = task.getTempFile();
        if (tmpFile == null || !Files.exists(tmpFile)) {
            // Что‑то пошло не так с файлом
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Загружаем ресурс
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(tmpFile));
        String dateStr = task.getDate() != null ? task.getDate().toString() : "all";
        String filename = "logs-" + dateStr + ".txt";

        // Удаляем временный файл и запись о задаче
        Files.delete(tmpFile);
        tasks.remove(taskId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }


    @Operation(summary = "Get logs by date", description = "Retrieves logs for a"
            + " specified date (format yyyy-MM-dd)."
            + " If no date is provided, all logs are returned.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "500", description = "Error reading log file")
    })
    @GetMapping
    public ResponseEntity<?> getLogsByDate(@RequestParam(required = false) String date) {
        try {
            List<String> lines = Files.lines(Paths.get(logFilePath))
                    .collect(Collectors.toList());

            if (date != null && !date.isEmpty()) {
                LocalDate requestedDate = LocalDate.parse(date);
                lines = lines.stream()
                        .filter(line -> line.contains(requestedDate.toString()))
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(lines);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Use yyyy-MM-dd");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading log file: " + e.getMessage());
        }
    }

    @Operation(summary = "Download logs by date", description = "Downloads logs"
            + " for a specified date as a text file."
            + " If no date is provided, the entire log file is downloaded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log file downloaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "500", description = "Error reading log file")
    })
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogs(@RequestParam(required = false) String date) {
        try {
            List<String> lines = Files.lines(Paths.get(logFilePath))
                    .collect(Collectors.toList());

            if (date != null && !date.isEmpty()) {
                LocalDate requestedDate = LocalDate.parse(date);
                lines = lines.stream()
                        .filter(line -> line.contains(requestedDate.toString()))
                        .collect(Collectors.toList());
            }

            String content = String.join(System.lineSeparator(), lines);
            ByteArrayResource resource = new ByteArrayResource(content.getBytes());
            String filename = (date != null && !date.isEmpty())
                    ? "logs-" + date + ".txt" : "logs-all.txt";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
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