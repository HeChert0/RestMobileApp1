package app.service;

import app.dao.OrderRepository;
import app.dto.SmartphoneDTO;
import app.mapper.SmartphoneMapper;
import app.models.Order;
import app.models.Smartphone;
import app.dao.SmartphoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SmartphoneService {

    private final SmartphoneRepository smartphoneRepository;
    private final SmartphoneMapper smartphoneMapper;
    private final OrderRepository orderRepository;

    @Autowired
    public SmartphoneService(SmartphoneRepository smartphoneRepository,
                             OrderRepository orderRepository,
                             SmartphoneMapper smartphoneMapper) {
        this.smartphoneRepository = smartphoneRepository;
        this.orderRepository = orderRepository;
        this.smartphoneMapper = smartphoneMapper;
    }

    public List<Smartphone> getAllSmartphones() {
        return smartphoneRepository.findAll();
    }

    public Optional<Smartphone> getSmartphoneById(Long id) {
        return smartphoneRepository.findById(id);
    }

    public Smartphone createSmartphone(SmartphoneDTO smartphoneDTO) {
        Smartphone smartphone = smartphoneMapper.toEntity(smartphoneDTO);

        if (smartphoneDTO.getOrderId() != null) {
            Optional<Order> orderOpt = orderRepository.findById(smartphoneDTO.getOrderId());
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order with id " + smartphoneDTO.getOrderId() + " does not exist!");
            }
            Order order = orderOpt.get();
            smartphone.setOrder(order);
        }

        return smartphoneRepository.save(smartphone);
    }

    public void deleteSmartphone(Long id) {
        smartphoneRepository.deleteById(id);
    }

    public List<Smartphone> getFilteredSmartphones(String brand, String model, Double price) {
        return smartphoneRepository.findAll().stream()
                .filter(s -> brand == null || s.getBrand().equalsIgnoreCase(brand))
                .filter(s -> model == null || s.getModel().equalsIgnoreCase(model))
                .filter(s -> price == null || s.getPrice() == price)
                .toList();
    }

    public Smartphone updateSmartphone(Long id, SmartphoneDTO smartphoneDTO) {
        Smartphone smartphone = smartphoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Smartphone with id " + id + " does not exist!"));

        // Обновляем только переданные поля
        if (smartphoneDTO.getBrand() != null) {
            smartphone.setBrand(smartphoneDTO.getBrand());
        }
        if (smartphoneDTO.getModel() != null) {
            smartphone.setModel(smartphoneDTO.getModel());
        }
        if (smartphoneDTO.getPrice() != 0.0) {
            smartphone.setPrice(smartphoneDTO.getPrice());
        }

        // Проверяем, указан ли новый orderId
        if (smartphoneDTO.getOrderId() != null) {
            Order order = orderRepository.findById(smartphoneDTO.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Order with id " + smartphoneDTO.getOrderId() + " does not exist!"));
            smartphone.setOrder(order);
        }

        return smartphoneRepository.save(smartphone);
    }


    public List<Smartphone> getPhonesByCustomerNameJPQL(String customerName) {
        return smartphoneRepository.findByCustomerNameJPQL(customerName);
    }

    public List<Smartphone> getPhonesByCustomerNameNative(String customerName) {
        return smartphoneRepository.findByCustomerNameNative(customerName);
    }
}
