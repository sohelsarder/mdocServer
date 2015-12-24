package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;

public class Question {
	private String id;
	private String qname;
	private String qtype;
	private String caption;
	private String hint;
	private String defaultval;
	private boolean required = false;
	private boolean readonly = false;
	private List<Validation> validations = new ArrayList<Validation>();
	private List<Branch> branches;
	private List<Option> options;
	private int inNode = 0;
	private String relevant;
	private String validationMsg;

	// For Integer Only
	private String numType;

	// For Binary Only
	private String mediaType;

	// Internal
	private int visited = 0;
	private int position;
	private static int lastPosition = 0;

	// DFS
	public static void updateRelevant(String node, String relevant, Map<String, Question> questions) {

		if(node.equals("disconnect")) {
			return;
		}

		Question q = questions.get(node);

		if(q == null) {
			return;
		}

		q.setPosition(lastPosition++);
		q.incVisited();

		if(relevant != null) {
			if(q.getRelevant() != null) {
				relevant = "(" + q.getRelevant() + " or " + relevant + ")";
				q.setRelevant(relevant);
			} else {
				q.setRelevant(relevant);
			}
		}

		if(q.getVisited() < q.getInNode()) {
			return;
		}

		List<Branch> branches =  q.getBranches();
		// any is the first item
		List<String> allConditions = new ArrayList<String>();

		if(branches.size() > 0 && branches.get(0).getRule().equals("any")) {
            Branch any = branches.get(0);
            branches.remove(0);
            branches.add(any);
		}

		for(Branch br: branches) {
		// for(int i = branches.size() - 1; i >= 0; --i) {
			// Branch br = branches.get(i);

			String nextRelevant, curRelevant = null;
			if(relevant != null) {
				if(q.required) {
					nextRelevant = "(" + "not(/data/" + q.getQname() + " = null)" + ")" + " and ";
				} else {
					nextRelevant = "(" + relevant + ")" + " and ";
				}
			} else {
				nextRelevant = "";
			}

			// Logger.info(br.getRule());
			// Select
			if((q.getQtype().equals("select") || q.getQtype().equals("select1")) && !br.getRule().equals("any")) {

				List<String> tmpConditions = new ArrayList<String>();
				if(!br.getValue().equals("")) {
					String[] options = StringUtils.split(br.getValue(), " ");
					for(String option: options) {
						tmpConditions.add("selected(/data/" + q.getQname() + ", '" + option + "')");
					}
				}

				if(!br.getCalcValue().equals("")) {
					tmpConditions.add(br.getCalcValue());
				}

				if(tmpConditions.size() > 0) {
					curRelevant = "(" + StringUtils.join(tmpConditions, " and ") + ")";
				}
			}
			// String, Integer
			else if(br.getRule().equals("is")) {
				curRelevant = "/data/" + q.getQname() + " = '" + br.getValue() + "'";
			}
			// String, Integer
			else if(br.getRule().equals("not")) {
				curRelevant = "/data/" + q.getQname() + " != '" + br.getValue() + "'";
			}
			// Integer
			else if(br.getRule().equals("greater")) {
				curRelevant = "/data/" + q.getQname() + " > '" + br.getValue() + "'";
			}
			// Integer
			else if(br.getRule().equals("less")) {
				curRelevant = "/data/" + q.getQname() + " < '" + br.getValue() + "'";
			}
			// Advance for All
			else if(br.getRule().equals("calc")) {
				curRelevant = br.getValue();
			}
			// Others
			else {
				if(branches.size() > 1 && allConditions.size() > 0) {
					nextRelevant += "not(" + StringUtils.join(allConditions, " or ") + ")";
				} else {
					nextRelevant = relevant;
				}
			}

			if(curRelevant != null) {
				nextRelevant += curRelevant;
				allConditions.add(curRelevant);
			}

			Question.updateRelevant(br.getNextq(), nextRelevant, questions);
		}

		return;
	}

	public String toString() {
		return String.format("{postion: %s, name: %s, type: %s, caption: %s, relevant: %s}", position, qname, qtype, caption, relevant);
	}

	public String getQname() {
		return qname;
	}

	public void setQname(String qname) {
		this.qname = qname;
	}

	public String getQtype() {
		return qtype;
	}

	public void setQtype(String qtype) {
		this.qtype = qtype;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public String getDefaultvalue() {
		return defaultval;
	}

	public void setDefaultvalue(String defaultvalue) {
		this.defaultval = defaultvalue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public List<Validation> getValidations() {
		return validations;
	}

	public void setValidations(List<Validation> validations) {
		this.validations = validations;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public void setBranches(List<Branch> branches) {
		this.branches = branches;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public int getInNode() {
		return inNode;
	}

	public void setInNode(int inNode) {
		this.inNode = inNode;
	}

	public void incInNode() {
		this.inNode++;
	}

	public String getRelevant() {
		return relevant;
	}

	public void setRelevant(String relevant) {
		this.relevant = relevant;
	}

	public int getVisited() {
		return visited;
	}

	public void setVisited(int visited) {
		this.visited = visited;
	}

	public void incVisited() {
		this.visited++;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getNumType() {
		return numType;
	}

	public void setNumType(String numType) {
		this.numType = numType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getValidationMessage() {
		return validationMsg;
	}

	public void setValidationMessage(String validationMessage) {
		this.validationMsg = validationMessage;
	}

}