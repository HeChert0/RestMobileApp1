package app.dao;

import app.models.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.user.username = :username")
    List<Order> findByUserUsernameJpql(@Param("username") String username);

    @Query(value = "SELECT o.* FROM orders o JOIN users u ON o.user_id ="
            + " u.id WHERE u.username = :username", nativeQuery = true)
    List<Order> findByUserUsernameNative(@Param("username") String username);

    @Query("select distinct o from Order o "
            + "join o.smartphones s "
            + "where (:brand is null or lower(s.brand) = lower(:brand)) "
            + "and (:model is null or lower(s.model) = lower(:model)) "
            + "and (:minPrice is null or s.price >= :minPrice) "
            + "and (:maxPrice is null or s.price <= :maxPrice)")
    List<Order> findOrdersBySmartphoneCriteriaJpql(@Param("brand") String brand,
                                               @Param("model") String model,
                                               @Param("minPrice") Double minPrice,
                                               @Param("maxPrice") Double maxPrice);

    @Query(value = "select distinct o.* from orders o "
            + "join order_smartphone os on o.id = os.order_id "
            + "join smartphones s on os.smartphone_id = s.id "
            + "where (:brand is null or lower(s.brand) = lower(:brand)) "
            + "and (:model is null or lower(s.model) = lower(:model)) "
            + "and (:minPrice is null or s.price >= :minPrice) "
            + "and (:maxPrice is null or s.price <= :maxPrice)", nativeQuery = true)
    List<Order> findOrdersBySmartphoneCriteriaNative(@Param("brand") String brand,
                                                     @Param("model") String model,
                                                     @Param("minPrice") Double minPrice,
                                                     @Param("maxPrice") Double maxPrice);

}
