package app.service;

import app.dao.PhoneDao;
import app.entities.Phone;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PhoneServiceLocal implements PhoneService {

    private final PhoneDao phoneDao;

    public PhoneServiceLocal(PhoneDao phoneDao) {
        this.phoneDao = phoneDao;
    }

    @Override
    public Phone getPhoneById(long id) {
        return phoneDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No phone with id " + id));
    }

    @Override
    public List<Phone> getFilteredPhones(String brand, String model, Integer price) {
        List<Phone> filteredPhones = phoneDao.findAll().stream()
                .filter(matches(brand, Phone::getBrand))
                .filter(matches(model, Phone::getModel))
                .filter(matches(price, Phone::getPrice))
                .toList();

        if (filteredPhones.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Phones not found");
        }

        return filteredPhones;
    }

    private static <T> Predicate<Phone> matches(T value, Function<Phone, T> getter) {
        return value == null ? phone -> true : phone -> value.equals(getter.apply(phone));
    }
}
