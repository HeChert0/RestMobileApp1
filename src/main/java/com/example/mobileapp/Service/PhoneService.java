package com.example.mobileapp.Service;

import java.util.List;
import com.example.mobileapp.Entities.Phone;

public interface PhoneService {
    Phone getPhoneById(long id);
    List<Phone> getFilteredPhones(String brand, String model, Integer price);
}
