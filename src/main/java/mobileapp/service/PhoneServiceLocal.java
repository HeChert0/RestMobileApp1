package mobileapp.service;


import mobileapp.dao.PhoneDAO;
import mobileapp.entities.Phone;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PhoneServiceLocal implements PhoneService {

    private final PhoneDAO phoneDAO;

    public PhoneServiceLocal(PhoneDAO phoneDAO) {
        this.phoneDAO = phoneDAO;
    }

    @Override
    public Phone getPhoneById(long id) {
        return phoneDAO.findById(id).orElseThrow(() -> new NoSuchElementException("There is no phone with id " + id));
    }

    @Override
    public List<Phone> getFilteredPhones(String brand, String model, Integer price) {
        List<Phone> phones = phoneDAO.findAll();

        return phones.stream()
                .filter(phone -> brand == null || phone.getBrand().equalsIgnoreCase(brand))
                .filter(phone -> model == null || phone.getModel().equalsIgnoreCase(model))
                .filter(phone -> price == null || phone.getPrice().equals(price))
                .toList();
    }
}
