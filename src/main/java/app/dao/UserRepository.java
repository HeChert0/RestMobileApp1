package app.dao;

import app.models.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String name);

    @EntityGraph(attributePaths = {"smartphones"},
            value = "User.withSmartphones", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findWithSmartphonesById(Long id);

    @EntityGraph(attributePaths = {"orders"})
    Optional<User> findWithOrdersById(Long id);
}
