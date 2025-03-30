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
    @EntityGraph(attributePaths = {"smartphones"})
    Optional<Order> findWithSmartphonesById(Long id);

    @Query("SELECT o FROM Order o WHERE o.user.username = :username")
    List<Order> findByUserUsernameJpql(@Param("username") String username);

    @Query(value = "SELECT o.* FROM orders o JOIN users u ON o.user_id ="
            + " u.id WHERE u.username = :username", nativeQuery = true)
    List<Order> findByUserUsernameNative(@Param("username") String username);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM order_smartphone WHERE smartphone_id = :phoneId",
            nativeQuery = true)
    void deleteOrderSmartphoneLinks(@Param("phoneId") Long phoneId);
}
