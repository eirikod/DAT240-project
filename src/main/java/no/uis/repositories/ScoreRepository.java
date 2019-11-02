package no.uis.repositories;

import no.uis.players.ScoreData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScoreRepository extends CrudRepository<ScoreData, Integer> {
    List<ScoreData> findAll();
}
