package app.service;

import app.entities.Phone;
import java.util.List;

public interface PhoneService {
    Phone getPhoneById(long id);

    List<Phone> getFilteredPhones(String brand, String model, Integer price);
}
