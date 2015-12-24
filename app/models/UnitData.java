package models;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class UnitData extends Model {

	@Required
	@ManyToOne
	public Data data;

	@ManyToOne
	public Form form;
	
	@ManyToOne
	public Ngo ngo;

	@Required
	public String titleVar;
	@Column(length=1023)
	public String title;
	public String type;
	public String valueVar;
	@Column(length=1023)
	public String value;
	public String extraValue;

	public UnitData(Data data, Form form, Ngo ngo, String titleVar, String title, String type, String valueVar, String value, String extraValue) {
		this.data = data;
		this.form = form;
		this.ngo = ngo;
		this.titleVar = titleVar;
		this.title = title;
		this.type = type;
		this.valueVar = valueVar;
		this.value = value;
		this.extraValue = extraValue;
	}
}
