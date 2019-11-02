package no.uis.players;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ScoreBoard")
public class ScoreData {

    @Id
    @Column(name="id")
    Long id;

    @Column(name="proposername")
    String proposerName;

    @Column(name="guessername")
    String guesserName;

    @Column(name="score")
    Integer score;

    public ScoreData() {

    }

    public ScoreData(Long partyId, String proposerName, String guesserName, Integer score) {
        this.id = partyId;
        this.proposerName = proposerName;
        this.guesserName = guesserName;
        this.score = score;
    }

    @Override
    public String toString() {
        return proposerName + " and " + guesserName + " scored " + score + " together!";
    }
}
