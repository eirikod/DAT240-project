package no.uis.players;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ScoreBoard")
public class ScoreData {
    @Column(name = "id")
    private Long gameId;

    @Column(name = "score")
    private Integer scoreValue;

    @Column(name = "proposername")
    private String proposerName;

    @Column(name = "guessername")
    private String guesserName;

    public ScoreData(Long gameId, Integer scoreValue, String proposerName, String guesserName) {
        this.gameId = gameId;
        this.scoreValue = scoreValue;
        this.proposerName = proposerName;
        this.guesserName = guesserName;
    }
}
