package app.service;


import app.dao.PhoneDao;
import app.entities.Phone;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;


@Service
public class PhoneServiceLocal implements PhoneService {

    private final PhoneDao phoneDao;

    public PhoneServiceLocal(PhoneDao phoneDao) {
        this.phoneDao = phoneDao;
    }

    @Override
    public Phone getPhoneById(long id) {
        return phoneDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("There is no phone with id " + id));
    }

    @Override
    public List<Phone> getFilteredPhones(String brand, String model, Integer price) {
        List<Phone> phones = phoneDao.findAll();

        return phones.stream()
                .filter(phone -> brand == null || phone.getBrand().equalsIgnoreCase(brand))
                .filter(phone -> model == null || phone.getModel().equalsIgnoreCase(model))
                .filter(phone -> price == null || phone.getPrice().equals(price))
                .toList();
    }
}
//        if (brand == null && price == null && model == null) {
//            throw new NullPointerException("Params are null");
//        }