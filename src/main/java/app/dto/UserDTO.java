package app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя пользователя должно содержать от 2 до 50 символов")
    private String name;

    private List<Long> smartphoneIds;

    public UserDTO() {}

    public UserDTO(Long id, String name, List<Long> smartphoneIds) {
        this.id = id;
        this.name = name;
        this.smartphoneIds = smartphoneIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getSmartphoneIds() {
        return smartphoneIds;
    }

    public void setSmartphoneIds(List<Long> smartphoneIds) {
        this.smartphoneIds = smartphoneIds;
    }
}
