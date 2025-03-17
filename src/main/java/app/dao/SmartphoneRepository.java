package app.dao;

import app.models.Smartphone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmartphoneRepository extends JpaRepository<Smartphone, Long> {

    @Query("SELECT s FROM Smartphone s JOIN s.order o WHERE o.customerName = :customerName")
    List<Smartphone> findByCustomerNameJPQL(@Param("customerName") String customerName);

    @Query(value = "SELECT s.* FROM smartphones s JOIN orders o ON s.order_id = o.id " +
            "WHERE o.customer_name = :customerName", nativeQuery = true)
    List<Smartphone> findByCustomerNameNative(@Param("customerName") String customerName);
}
