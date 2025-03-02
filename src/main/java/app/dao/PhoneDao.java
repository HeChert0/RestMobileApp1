package app.dao;

import app.entities.Phone;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;


@Repository
public class PhoneDao {

    private final List<Phone> phoneList = new ArrayList<>();
    private long idCounter;

    public PhoneDao() {
        idCounter = 1;

        phoneList.add(new Phone(idCounter++, "Apple", "12 Pro", 1000));
        phoneList.add(new Phone(idCounter++, "Huiwai", "X32", 666));
        phoneList.add(new Phone(idCounter++, "Realmi", "6i", 228));
    }

    public Optional<Phone> findById(long id) {
        return phoneList.stream()
                .filter(product -> product.getId() == id)
                .findFirst();
    }

    public List<Phone> findAll() {
        return new ArrayList<>(phoneList);
    }
}
