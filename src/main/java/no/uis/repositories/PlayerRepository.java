package no.uis.repositories;

import no.uis.players.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface PlayerRepository extends CrudRepository<User, String> {
    User findByUsername(String username);
    User findById(Long id);
}