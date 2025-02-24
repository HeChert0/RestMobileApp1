package app.service;

import java.util.List;
import app.entities.Phone;

public interface PhoneService {
    Phone getPhoneById(long id);
    List<Phone> getFilteredPhones(String brand, String model, Integer price);
}
