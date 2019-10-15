package no.uis.players;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
	public String name;
	@Id
	public long id;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "User [name=" + name + ", id=" + id + "]";
	}
	
	
}
