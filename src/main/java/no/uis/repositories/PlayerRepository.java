package no.uis.repositories;

import java.util.List;

import no.uis.players.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player, String> {
    List<Player> findByUsername(String username);
}