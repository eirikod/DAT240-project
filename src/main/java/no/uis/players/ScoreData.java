package no.uis.players;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ScoreBoard")
public class ScoreData {
    @Id
    @Column(name = "id")
    public Long id;

    @Column(name = "proposername")
    public String proposerName;

    @Column(name = "guessername")
    public String guesserName;

    @Column(name = "score")
    public Integer score;

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
        return proposerName + " & " + guesserName + ". Scored " + score + ".";
    }
}
