package app.dto;

import java.util.List;

public class UserDTO {
    private Long id;
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
