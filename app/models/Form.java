package models;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Form extends Model {

	/** The title. */
	@Required
	public String title;

	/** The version. */
	@Required
	@Min(1)
	public int version = 1;

	/** The XML. */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	public byte[] xml;

	/** The JSON. */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	public byte[] json;

	/** The created. */
	public Date created;

	/** The data count. */
	public long dataCount;

	/** The last received time. */
	public Date lastReceived;

	// Extra for future use
	/** The is active. */
	public boolean isActive;

	/**
	 * Instantiates a new form.
	 *
	 * @param title the title
	 */
	public Form(String title) {
		this.title = title;
	}

	/**
	 * Instantiates a new form.
	 *
	 * @param title the title of the form
	 * @param version the version of the form
	 */
	public Form(String title, int version) {
		this(title);
		this.version = version;
	}

	/**
	 * Before insert.
	 */
	@PrePersist
    protected void beforeInsert() {
        this.created = new Date();
        this.isActive = true;
    }

	/**
	 * After load.
	 */
	@PostLoad
	protected void afterLoad() {
		// Force Fix
		/*
		this.dataCount = Data.count("form_id = ?", this.id);
		this.lastReceived = Data.find("SELECT received FROM Data d WHERE form_id = ? ORDER BY received DESC", this.id).first();
		this.save();
		*/
	}

	/**
	 * Find form by title.
	 *
	 * @param title the title
	 * @return the form
	 */
	public static Form findByTitle(String title) {
		return Form.find("byTitle", title).first();
	}

}
