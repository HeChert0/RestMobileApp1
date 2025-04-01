package app.dao;

import app.models.Smartphone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmartphoneRepository extends JpaRepository<Smartphone, Long> {

    @Query("SELECT s FROM Smartphone s "
            + "WHERE (:brand IS NULL OR lower(s.brand) = lower(:brand)) "
            + "AND (:model IS NULL OR lower(s.model) = lower(:model)) "
            + "AND (:price IS NULL OR s.price = :price)")
    List<Smartphone> filterSmartphonesJPQL(@Param("brand") String brand,
                                           @Param("model") String model,
                                           @Param("price") Double price);

    @Query(value = "SELECT * FROM smartphones "
            + "WHERE (:brand IS NULL OR lower(brand) = lower(:brand)) "
            + "AND (:model IS NULL OR lower(model) = lower(:model)) "
            + "AND (:price IS NULL OR price = :price)", nativeQuery = true)
    List<Smartphone> filterSmartphonesNative(@Param("brand") String brand,
                                             @Param("model") String model,
                                             @Param("price") Double price);
}
