package app.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class OrderDTO {
    private Long id;
    private String customerName;

    private List<Long> smartphoneIds;

    public OrderDTO() {}

    public OrderDTO(Long id, String customerName, List<Long> smartphoneIds) {
        this.id = id;
        this.customerName = customerName;
        this.smartphoneIds = smartphoneIds;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<Long> getSmartphoneIds() { return smartphoneIds; }
    public void setSmartphoneIds(List<Long> smartphoneIds) { this.smartphoneIds = smartphoneIds; }
}
