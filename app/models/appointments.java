package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Query;

import org.hibernate.engine.NamedQueryDefinition;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class appointments extends Model{

		@Required
		public int pulse;
		
		// 120 / 80
		@Required
		public String bp;
		
		@Required
		public float temperature;
		
		@Required
		public int respiration;
		
		@Required
		public int appearance;
		
		@Required
		public int color;
		
		@Required
		public int consciousness;
		
		@Required
		public int edema;
		
		@Required
		public int dehydration;
		
		public String diseaseImage = null;
		
		@Required
		public String weight;
		
		@Required
		public long    rmp_id;
		
		@Required
		public long   patient_id;
		
		// Status 0 =  Not Prescribed
		// Status 1 =  Prescribed
		// Status 2 =  Downloaded
		@Required
		public int    status = 0;
		
		@Required
		public Date   created;
		
		@Required
		public Date   modified;
		
		@Required
		public long   lockby;
		
		@Override
		public String toString(){
			String output = "Pulse ="+pulse;
			output += "\n"+"bp ="+bp;
			output += "\n"+"temperature ="+temperature;
			output += "\n"+"respiration ="+respiration;
			output += "\n"+"appearance ="+appearance;
			output += "\n"+"color ="+color;
			output += "\n"+"consciousness ="+consciousness;
			output += "\n"+"edema ="+edema;
			output += "\n"+"dehydration ="+dehydration;
			output += "\n"+"weight ="+weight;
			output += "\n"+"patient_id ="+patient_id;
			output += "\n"+"rmp_id ="+rmp_id;
			output += "\n"+"status ="+status;
			output += "\n"+"created ="+created;
			output += "\n"+"modified ="+modified;
			return output;
		}
	
		
		public static appointments findByRmpAndPatient(long rmpId, long patientId){
			return appointments.find("byPatient_idAndRmp_id", patientId, rmpId).first();
		}
		
		public static List<appointments> findByPatient(long patientId){
			return appointments.find("byPatient_id", patientId).fetch();
		}

		public static List<appointments> findByPending(long rmpId){
			return appointments.find("byStatusAndRmp_id", 1, rmpId).fetch();
		}
		
		// id = Appointment Id
		public static String[] getPrescriptionUrl(long id) {
			
			String [] ret = new String [2];
			String presUrl = "http://115.127.27.3:40/prescription/Prescriptions/downloadpdf/"; 
			presUrl += id;
			ret[0] = presUrl;
			ret[1] = prescriptions.getPrescriptionUrl(id);
			return ret;
		}
		
}
