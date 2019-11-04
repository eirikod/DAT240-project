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
    public String id;

    @Column(name = "proposername")
    public String proposerName;

    @Column(name = "guessername")
    public String guesserName;

    @Column(name = "score")
    public Integer score;

    @Column(name ="imagename")
    public String imagename;

    public ScoreData() {

    }

    public ScoreData(String partyId, String proposerName, String guesserName, Integer score, String imagename) {
        this.id = partyId;
        this.proposerName = proposerName;
        this.guesserName = guesserName;
        this.score = score;
        this.imagename = imagename;
    }

    @Override
    public String toString() {
        return proposerName + " & " + guesserName + " cored " + score + " on image " + imagename + ".";
    }
}
