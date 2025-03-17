package app.dto;

import java.util.List;

public class OrderDto {
    private Long id;
    private String customerName;

    private List<Long> smartphoneIds;

    public OrderDto() {}

    public OrderDto(Long id, String customerName, List<Long> smartphoneIds) {
        this.id = id;
        this.customerName = customerName;
        this.smartphoneIds = smartphoneIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<Long> getSmartphoneIds() {
        return smartphoneIds;
    }

    public void setSmartphoneIds(List<Long> smartphoneIds) {
        this.smartphoneIds = smartphoneIds;
    }
}
