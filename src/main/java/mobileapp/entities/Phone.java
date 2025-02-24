package mobileapp.entities;


public class Phone {
    private Long id;
    private String brand;
    private String model;



    private Integer price;

    public Phone(Long id, String brand, String model, Integer price) {
        this.id = id;
        this.model = model;
        this.price = price;
        this.brand = brand;
    }

    public Phone() {
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }
}
