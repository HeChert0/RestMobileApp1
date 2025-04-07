package app.dao;

import app.models.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String name);

    @EntityGraph(attributePaths = {"orders"})
    Optional<User> findWithOrdersById(Long id);


    @Query("select distinct u from User u "
            + "join u.orders o "
            + "join o.smartphones s "
            + "where (:minTotal is null or o.totalAmount >= :minTotal) "
            + "and (:phoneBrand is null or lower(s.brand) = lower(:phoneBrand))"
            + "and (:date is null or o.orderDate = :date)")
    List<User> findUsersByOrderAndPhoneCriteriaJpql(@Param("minTotal") Double minTotal,
                                                @Param("phoneBrand") String phoneBrand,
                                                @Param("date") LocalDate date);

    @Query(value = "select distinct u.* from users u "
            + "join orders o on u.id = o.user_id "
            + "join order_smartphone os on o.id = os.order_id "
            + "join smartphones s on os.smartphone_id = s.id "
            + "where (:minTotal is null or o.total_amount >= :minTotal) "
            + "and (:date is null or  o.orderDate = :date"
            + "and (:phoneBrand is null or lower(s.brand) = lower(:phoneBrand))",
            nativeQuery = true)
    List<User> findUsersByOrderAndPhoneCriteriaNative(@Param("minTotal") Double minTotal,
                                                      @Param("phoneBrand") String phoneBrand,
                                                      @Param("date") LocalDate date);
}
