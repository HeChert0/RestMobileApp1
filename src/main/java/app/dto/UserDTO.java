package app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя пользователя должно содержать от 2 до 50 символов")
    private String username; // или name, в зависимости от того, что ты считаешь основным

    private String password;

    // Можно добавить дополнительные поля, например, email и т.д.

    // Для связи со смартфонами – список ID или вложенные объекты
    private List<Long> smartphoneIds;

    public UserDTO() {}

    public UserDTO(Long id, String username, List<Long> smartphoneIds, String password) {
        this.id = id;
        this.username = username;
        this.smartphoneIds = smartphoneIds;
        this.password = password;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public List<Long> getSmartphoneIds() {
        return smartphoneIds;
    }

    public void setSmartphoneIds(List<Long> smartphoneIds) {
        this.smartphoneIds = smartphoneIds;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
