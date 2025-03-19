package app.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {
    private Long id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private Double totalAmount;

    private LocalDate orderDate;

    @NotEmpty(message = "Order must contain at least one smartphone")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Long> smartphoneIds;

    public List<SmartphoneDto> getSmartphones() {
        return smartphones;
    }

    public void setSmartphones(List<SmartphoneDto> smartphones) {
        this.smartphones = smartphones;
    }

    private List<SmartphoneDto> smartphones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public List<Long> getSmartphoneIds() {
        return smartphoneIds;
    }

    public void setSmartphoneIds(List<Long> smartphoneIds) {
        this.smartphoneIds = smartphoneIds;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
