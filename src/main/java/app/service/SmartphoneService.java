package app.service;


import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dto.SmartphoneDto;
import app.mapper.SmartphoneMapper;
import app.models.Order;
import app.models.Smartphone;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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

    public Smartphone createSmartphone(SmartphoneDto smartphoneDto) {
        Smartphone smartphone = smartphoneMapper.toEntity(smartphoneDto);

        if (smartphoneDto.getOrderId() != null) {
            Optional<Order> orderOpt = orderRepository.findById(smartphoneDto.getOrderId());
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException(
                        "Order with id " + smartphoneDto.getOrderId() + " does not exist!");
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

    public Smartphone updateSmartphone(Long id, SmartphoneDto smartphoneDto) {
        Smartphone smartphone = smartphoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Smartphone with id " + id + " does not exist!"));

        // Обновляем только переданные поля
        if (smartphoneDto.getBrand() != null) {
            smartphone.setBrand(smartphoneDto.getBrand());
        }
        if (smartphoneDto.getModel() != null) {
            smartphone.setModel(smartphoneDto.getModel());
        }
        if (smartphoneDto.getPrice() != 0.0) {
            smartphone.setPrice(smartphoneDto.getPrice());
        }

        // Проверяем, указан ли новый orderId
        if (smartphoneDto.getOrderId() != null) {
            Order order = orderRepository.findById(smartphoneDto.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Order with id " + smartphoneDto.getOrderId() + " does not exist!"));
            smartphone.setOrder(order);
        }

        return smartphoneRepository.save(smartphone);
    }

    public List<Smartphone> getPhonesByCustomerNameJpql(String customerName) {
        return smartphoneRepository.findByCustomerNameJpql(customerName);
    }

    public List<Smartphone> getPhonesByCustomerNameNative(String customerName) {
        return smartphoneRepository.findByCustomerNameNative(customerName);
    }
}
