package app.service;

import app.dao.SmartphoneRepository;
import app.models.Smartphone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SmartphoneService {

    private final SmartphoneRepository smartphoneRepository;

    @Autowired
    public SmartphoneService(SmartphoneRepository smartphoneRepository) {
        this.smartphoneRepository = smartphoneRepository;
    }

    public List<Smartphone> getAllSmartphones() {
        return smartphoneRepository.findAll();
    }

    public Optional<Smartphone> getSmartphoneById(Long id) {
        return smartphoneRepository.findById(id);
    }

    public Smartphone saveSmartphone(Smartphone smartphone) {
        return smartphoneRepository.save(smartphone);
    }

    public Smartphone updateSmartphone(Long id, Smartphone updatedSmartphone) {
        return smartphoneRepository.findById(id).map(existingSmartphone -> {
            existingSmartphone.setBrand(updatedSmartphone.getBrand());
            existingSmartphone.setModel(updatedSmartphone.getModel());
            existingSmartphone.setPrice(updatedSmartphone.getPrice());
            return smartphoneRepository.save(existingSmartphone);
        }).orElse(null);
    }

    public void deleteSmartphone(Long id) {
        smartphoneRepository.deleteById(id);
    }
}
