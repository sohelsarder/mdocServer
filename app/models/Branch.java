package models;

public class Branch {
	private String rule;
	private String value = "";
	private String nextq;

	// Select && Select1
	private String calcValue = "";

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getNextq() {
		return nextq;
	}

	public void setNextq(String nextq) {
		this.nextq = nextq;
	}

	public String getCalcValue() {
		return calcValue;
	}

	public void setCalcValue(String calcValue) {
		this.calcValue = calcValue;
	}

	@Override
	public String toString() {
		return this.nextq;
	}
}
