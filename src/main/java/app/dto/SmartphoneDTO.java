package app.dto;

public class SmartphoneDTO {
    private Long id;
    private String brand;
    private String model;
    private double price;
    // Если нужно указать, к какому заказу привязан – можно добавить orderId:
    private Long orderId;

    public SmartphoneDTO() {}

    public SmartphoneDTO(Long id, String brand, String model, double price, Long orderId) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.orderId = orderId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
