package models;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Ngo extends Model{
	
	@Required
	public String name;
	
	public String toString() {
        return name;
	}
	
	public int compareTo(Ngo otherNgo) {
        return id.compareTo(otherNgo.id);
    }
}
