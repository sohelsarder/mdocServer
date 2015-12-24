package models;

public class Validation {
	private String validationType;
	private String value;
	
	// For Date
	private String baseDate;
	
	public String getValidationType() {
		return validationType;
	}
	
	public void setValidationType(String validationType) {
		this.validationType = validationType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getBaseDate() {
		return baseDate;
	}

	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}
	
}
