package no.uis.repositories;

import no.uis.players.ScoreData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreBoardRepository extends CrudRepository<ScoreData, Long> {
    List<ScoreData> findAllByScoreAfter(Integer integer);
}
