package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Query;

import org.hibernate.engine.NamedQueryDefinition;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class rmps extends Model{
	
	@Required
	public long user_id;
	
	@Required
	public String phone;
	
	public String address;
	
	public String age;
	
	public rmps(long id, String phone){
		this.user_id = id;
		this.phone = phone;
	}

}
