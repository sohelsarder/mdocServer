/*
 * Copyright (C) 2011 mPower Health
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.joda.time.DateTime;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.mvc.Util;

/**
 * Data Model - Definition of survey form data.
 */
@Entity
public class Data extends Model {

	/** The form. */
	@Required
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    public Form form;

	/** The xml. */
	@Required
	@Lob
	@Basic(fetch = FetchType.LAZY)
	public byte[] xml;

	@Required
	@ManyToOne
	public User sender;

	@Required
    @ManyToOne
    public Ngo ngo;
	
	@Required
//	@Length(min=12, max=12)
//	public String householdId;
	public String respondentId;
	public String respondentNumber;
	public String respondentName;

	/** The items, will not be persist. Will be used in View*/
	@Transient
	public List items;

	/** The received. */
	public Date received;

	public Date startTime;
	public Date endTime;

	public String[] audioData;

	// Extra Data for Dashboard
	public Double latitude;
	public Double longitude;
	public Double accuracy;
	public String image;
	public Boolean isExtracted = false;

	/**
	 * Instantiates a new data.
	 *
	 * @param form the form
	 * @param xml the xml
	 */
	public Data(Form form, byte[] xml) {
		this.form = form;
		this.xml = xml;
	}

	/**
	 * Before insert.
	 */
	@PrePersist
    protected void beforeInsert() {
        this.received = new Date();
        this.form.lastReceived = this.received;
        this.form.dataCount++;
    }

	/**
	 * Before remove.
	 */
	@PreRemove
    protected void beforeRemove() {
        this.form.dataCount--;
    }
	
	public int countData(){
		//Data keyData = Data.findById(id);
		List<Data> data = Data.find("byRespondentId", this.respondentId).fetch();
		return data.size();
	}
}
