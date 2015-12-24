package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Query;

import org.hibernate.engine.NamedQueryDefinition;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class prescriptions extends Model{
	
	public String complain;
	
	public int doctor_id;
	
	public int disease_id;
	
	public String prescription;
	
	public String advice;
	
	public String investigations;
	
	public String pdf;
	
	public int system_id;
	
	public long appointment_id;
	
	public Date modified;
	
	public Date created;
	
	public static String getPrescriptionUrl(long appointment_id){
		prescriptions pres = prescriptions.find("byAppointment_id", appointment_id).first();
		if (pres == null)
			return null;
		
		return pres.pdf;
	}
	
	
}
