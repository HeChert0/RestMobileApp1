package app.service;

import app.cache.InMemoryCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.models.Order;
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
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Autowired
    public SmartphoneService(SmartphoneRepository smartphoneRepository,
                             OrderRepository orderRepository,
                             OrderService orderService) {
        this.smartphoneRepository = smartphoneRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    public List<Smartphone> getAllSmartphones() {
        long dbCount = smartphoneRepository.count();
        return smartphoneRepository.findAll();
    }

    public Optional<Smartphone> getSmartphoneById(Long id) {
        return smartphoneRepository.findById(id);
    }

    @Transactional
    public Smartphone saveSmartphone(Smartphone smartphone) {
        return smartphoneRepository.save(smartphone);
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Transactional
    public Smartphone updateSmartphone(Long id, Smartphone updatedSmartphone) {
        return smartphoneRepository.findById(id).map(existingSmartphone -> {
            double oldPrice = existingSmartphone.getPrice();
            existingSmartphone.setBrand(updatedSmartphone.getBrand());
            existingSmartphone.setModel(updatedSmartphone.getModel());
            existingSmartphone.setPrice(updatedSmartphone.getPrice());


            Smartphone savedSmartphone = smartphoneRepository.save(existingSmartphone);

            if (oldPrice != savedSmartphone.getPrice()) {
                orderService.updateOrdersTotalBySmartphone(savedSmartphone);
            }

            return savedSmartphone;
        }).orElse(null);
    }

    @Transactional
    public void deleteSmartphone(Long id) {
        Smartphone phone = smartphoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Smartphone with id " + id + " not found."));

        orderRepository.deleteOrderSmartphoneLinks(id);

        smartphoneRepository.flush();

        smartphoneRepository.delete(phone);
    }


    public List<Smartphone> filterSmartphones(String brand, String model, Double price) {
        long dbCount = smartphoneRepository.count();

        List<Smartphone> smartphones;
        smartphones = smartphoneRepository.findAll();

        return smartphones.stream()
                .filter(s -> brand == null || s.getBrand().equalsIgnoreCase(brand))
                .filter(s -> model == null || s.getModel().equalsIgnoreCase(model))
                .filter(s -> price == null || s.getPrice().equals(price))
                .collect(Collectors.toList());
    }
}
