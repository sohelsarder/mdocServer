package models;

import java.util.Date;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;


/**
 * The table which links the ODK Data with the Doctor Dashboard
 * This data will be used by Doctor Dashboard for fetching patient
 * information.
 * 
 * Not the appointment info but the patient's Profile information
 * 
 * Naming convention changed to maintain support with Doctor Dashboard
 * */

@Entity
public class patients extends Model {

	
	/** The name of the patient */
	
	@Required
	public String  name;
	
	/** Patient's Gender in this format
	 *  XX year YY month
	 * */
	
	@Required
	public String  age;
	
	/** Patient's Gender in this format
	 *  1 = Male
	 *  2 = Female
	 * */
	
	@Required
	public int  gender;
	
	/** Patient's phone number */
	
	@Required
	public String  phone;
	
	/** Entry creation date */
	
	@Required
	public Date    created;
	
	/** Entry modified date */
	
	@Required
	public Date    modified;
	
	/** Status of the user in this format
	 * 1 = Active
	 * 0 = Inactive
	 *  */
	
	@Required
	public int status = 1;
	
	@Required
	@Unique
	public String code;
	
	@Required
	public String image;
	
	/**
	 * Default Constructor
	 * */
	public patients(){}
	
	/**
	 * Standard public constructor
	 * @param name
	 * @param age
	 * @param gender
	 * @param phone
	 * @param created
	 * @param modified
	 * @param status
	 * */
	public patients (String name, String age, int gender, String phone, Date created, Date modified, boolean status, String code){
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.phone = phone;
		this.created = created;
		this.modified = modified;
		// 1 if true 0 if false
		this.status = (status)?1:0;
		this.code = code;
	}
	
	public int compareTo(patients otherPatient){
		return this.id.compareTo(otherPatient.id);
	}
	
	public static patients findByCode(String code){
		return patients.find("byCode", code).first();
	}
	
	@Override
	public String toString(){
		String output = "name ="+name;
		output += "\nage ="+age;
		output += "\ngender ="+gender;
		output += "\nphone ="+phone;
		output += "\ncreated ="+created;
		output += "\nmodified ="+modified;
		output += "\nstatus ="+status;
		output += "\ncode ="+code;
		return output;
		
	}
	
}
