package app.service;

import app.dao.SmartphoneRepository;
import app.models.Smartphone;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    @Transactional
    public Smartphone saveSmartphone(Smartphone smartphone) {
        return smartphoneRepository.save(smartphone);
    }

    @Transactional
    public Smartphone updateSmartphone(Long id, Smartphone updatedSmartphone) {
        return smartphoneRepository.findById(id).map(existingSmartphone -> {
            existingSmartphone.setBrand(updatedSmartphone.getBrand());
            existingSmartphone.setModel(updatedSmartphone.getModel());
            existingSmartphone.setPrice(updatedSmartphone.getPrice());
            return smartphoneRepository.save(existingSmartphone);
        }).orElse(null);
    }

    @Transactional
    public void deleteSmartphone(Long id) {
        smartphoneRepository.deleteById(id);
    }

    public List<Smartphone> filterSmartphones(String brand, String model, Double price) {
        return smartphoneRepository.findAll().stream()
                .filter(s -> brand == null || s.getBrand().equalsIgnoreCase(brand))
                .filter(s -> model == null || s.getModel().equalsIgnoreCase(model))
                .filter(s -> price == null || s.getPrice().equals(price))
                .collect(Collectors.toList());
    }
}
