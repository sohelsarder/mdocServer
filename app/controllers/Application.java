package controllers;

import play.*;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.*;
import utils.QuestionComparator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.xml.internal.messaging.saaj.soap.impl.ElementFactory;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;

import models.*;

/**
 * Application Controller -	Default controller of the web application
 */
@With(Deadbolt.class)
public class Application extends Controller {

    /**
     * Index.
     */
	@ExternalRestrictions("Dashboard")
    public static void index() {
    	List<Data> allData = Data.find("byIsExtracted", true).fetch();
    	Play.configuration.getProperty("aggregate.googleApiKey");
    	render(allData);
    }

	@ExternalRestrictions("Edit Form")
    public static void parseBuildJson(Long id, String body) {

    	Form form = Form.findById(id);

    	// Logger.info(body);

    	// Parse JSON to XML
    	JsonObject json = null;
		try {
			json = (JsonObject)new JsonParser().parse(URLDecoder.decode(body, "application/json"));
		} catch (JsonSyntaxException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

    	Map<String, Question> unSortedQuestions = new HashMap<String, Question>();

    	for(Entry<String, JsonElement> jEntry: json.entrySet()) {
    		// String type = jEntry.getValue().getAsJsonObject().get("qtype").toString();
    		Question q = new Gson().fromJson(jEntry.getValue(), Question.class);
    		unSortedQuestions.put(jEntry.getKey(), q);
    	}

    	// Set inNode count
    	for(Question q: unSortedQuestions.values()) {
    		for(Branch b: q.getBranches()) {
    			if(!b.getNextq().equals("disconnect")) {
    				unSortedQuestions.get(b.getNextq()).incInNode();
    			}
    		}
    	}

    	// Find Root
    	String root = null;
    	int rootCount = 0;
    	List<String> invalidNodes = new ArrayList<String>();
    	for(String key: unSortedQuestions.keySet()) {
    		Question q = unSortedQuestions.get(key);
    		// Root check
    		if(q.getInNode() == 0) {
    			root = key;
    			rootCount++;
    			invalidNodes.add(key);
    		}
    	}

    	Logger.info("Number of roots: %d", rootCount);
    	if(rootCount != 1 || root == null) {
    		// Exception
    		new Exception("Invalid Question Set, It has multiple possible starting nodes");
    		Logger.error("Invalid Question Set, It has multiple possible starting nodes");
    		renderText("Invalid Question Set, It has multiple possible starting nodes.<br />Please check Question - " + StringUtils.join(invalidNodes, ", ") + ".");
    	}

    	// Evaluated branches
    	Question.updateRelevant(root, null, unSortedQuestions);

    	QuestionComparator qc = new QuestionComparator(unSortedQuestions);
    	Map<String, Question> questions = new TreeMap<String, Question>(qc);
    	questions.putAll(unSortedQuestions);

    	// Generate the XML
    	DocumentFactory factory = DocumentFactory.getInstance();

    	Element model = factory.createElement("model");
    	Element hBody = factory.createElement("h:body");

    	// Temporary
    	List<Element> binds = new ArrayList<Element>();

    	// Question Loop
    	Element data = factory.createElement("data").addAttribute("id", "" + form.id);
    	Element translation = factory.createElement("translation").addAttribute("lang", "eng");

    	// Start time
    	Element statTime = factory.createElement("bind")
    			.addAttribute("nodeset", "/data/i_start_time")
    			.addAttribute("jr:preload", "timestamp")
    			.addAttribute("jr:preloadParams", "start")
    			.addAttribute("type", "dataTime");
    	binds.add(statTime);

    	// Add start to instance
    	data.add(factory.createElement("i_start_time"));

    	for(String key: questions.keySet()) {
    		Question q = questions.get(key);
    		Element tmp;

    		// Data
    		tmp = factory.createElement(q.getQname());
    		if(q.getDefaultvalue() != null) {
    			tmp.addText(q.getDefaultvalue());
    		}
        	data.add(tmp);

        	// Translation
        	Element value;

        	// Label
        	tmp = factory.createElement("text").addAttribute("id", "/data/" + q.getQname() + ":label");
        	value = factory.createElement("value").addText(q.getCaption());
        	tmp.add(value);
        	translation.add(tmp);

        	// Hint
        	tmp = factory.createElement("text").addAttribute("id", "/data/" + q.getQname() + ":hint");
        	value = factory.createElement("value");
        	if(q.getHint() != null) {
        		value.addText(q.getHint());
        	}
        	tmp.add(value);
        	translation.add(tmp);

        	// Select & Radio
        	if(q.getQtype().equals("select") || q.getQtype().equals("select1")) {
        		for(Option op: q.getOptions()) {
        			tmp = factory.createElement("text").addAttribute("id", "/data/" + q.getQname() + ":" + op.getValue());
                	value = factory.createElement("value").addText(op.getCaption());
                	tmp.add(value);
                	translation.add(tmp);
        		}
        	}


        	// Bind
        	String qType = q.getQtype();
        	if(qType.equals("int") && q.getNumType().equals("dec")) {
        		qType = "decimal";
        	}
        	tmp = factory.createElement("bind").addAttribute("nodeset", "/data/" + q.getQname()).addAttribute("type", qType);
        	if(q.isRequired()) {
        		tmp.addAttribute("required", "true()");
        	}
        	if(q.isReadonly()) {
        		tmp.addAttribute("readonly", "true()");
        	}
        	if(q.getRelevant() != null) {
        		tmp.addAttribute("relevant", q.getRelevant());
        	}

        	// Add Validations to Bind
        	List<String> validationList = new ArrayList<String>();
        	String minLen = "", maxLen = "";
        	for(Validation v: q.getValidations()) {
        		String vType = v.getValidationType();
        		if(q.getQtype().equals("string")) {
	        		if(vType.equals("minLen")) {
	        			// validation.add("length(.) >= " + v.getValue());
	        			minLen = v.getValue();
	        		}
	        		else if(vType.equals("maxLen")) {
	        			// validation.add("length(.) <= " + v.getValue());
	        			maxLen = v.getValue();
	        		}
        		} else if(q.getQtype().equals("int")) {
        			if(vType.equals("min")) {
        				validationList.add(". >= " + v.getValue());
	        		}
	        		else if(vType.equals("max")) {
	        			validationList.add(". <= " + v.getValue());
	        		}
        		} else if(q.getQtype().equals("date")) {
        			if(vType.equals("range")) {
        				if(v.getBaseDate().equals("")) {
        					validationList.add("(. - today()) <= " + v.getValue());
        				}
	        		}
        		}
        		if(vType.equals("customRegex")) {
        			validationList.add("regex(., \"" + v.getValue() + "\")");
        		}
        	}

        	if(q.getQtype().equals("string") && (!minLen.equals("") || !maxLen.equals(""))) {
        		validationList.add(String.format("regex(., \"^.{%s,%s}$\")", minLen, maxLen));
    		}

        	if(validationList.size() > 0) {
        		tmp.addAttribute("constraint", "(" + StringUtils.join(validationList, " and ") + ")");
        		tmp.addAttribute("jr:constraintMsg", q.getValidationMessage());
        	}

        	binds.add(tmp);


        	// Body
        	if(q.getQtype().equals("select") || q.getQtype().equals("select1")) {
        		tmp = factory.createElement(q.getQtype()).addAttribute("ref", "/data/" + q.getQname());

        		Element label = factory.createElement("label").addAttribute("ref", "jr:itext('/data/" + q.getQname() + ":label')");
        		Element hint = factory.createElement("hint").addAttribute("ref", "jr:itext('/data/" + q.getQname() + ":hint')");
        		tmp.add(label);
        		tmp.add(hint);

        		Element item;
        		for(Option op: q.getOptions()) {
        			item = factory.createElement("item");
            		label = factory.createElement("label").addAttribute("ref", "jr:itext('/data/" + q.getQname() + ":" + op.getValue() + "')");
                	value = factory.createElement("value").addText(op.getValue());
                	item.add(label);
                	item.add(value);
                	tmp.add(item);
        		}
        		hBody.add(tmp);
        	}
        	else if(q.getQtype().equals("binary")) {
        		tmp = factory.createElement("upload").addAttribute("ref", "/data/" + q.getQname()).addAttribute("mediatype", q.getMediaType() + "/*");

        		Element label = factory.createElement("label").addAttribute("ref", "jr:itext('/data/" + q.getQname() + ":label')");
        		Element hint = factory.createElement("hint").addAttribute("ref", "jr:itext('/data/" + q.getQname() + ":hint')");
        		tmp.add(label);
        		tmp.add(hint);

        		hBody.add(tmp);
        	}
        	else {
        		tmp = factory.createElement("input").addAttribute("ref", "/data/" + q.getQname());

        		Element label = factory.createElement("label").addAttribute("ref", "jr:itext('/data/" + q.getQname() + ":label')");
        		Element hint = factory.createElement("hint").addAttribute("ref", "jr:itext('/data/" + q.getQname() + ":hint')");
        		tmp.add(label);
        		tmp.add(hint);

        		hBody.add(tmp);
        	}

    	}

    	// End time
    	Element endTime = factory.createElement("bind")
    			.addAttribute("nodeset", "/data/i_end_time")
    			.addAttribute("jr:preload", "timestamp")
    			.addAttribute("jr:preloadParams", "end")
    			.addAttribute("type", "dataTime");
    	binds.add(endTime);

    	// Add start to instance
    	data.add(factory.createElement("i_end_time"));

    	Element instance = factory.createElement("instance");
    	instance.add(data);

    	Element itext = factory.createElement("itext");
    	itext.add(translation);

    	// Add to model
    	model.add(instance);
    	model.add(itext);
    	// ODK collect *** :P
    	for(Element e: binds) {
    		model.add(e);
    	}

    	Element title = factory.createElement("h:title").addText(form.title);

    	Element head = factory.createElement("h:head");
    	head.add(title);
    	head.add(model);

    	Element html = factory.createElement("h:html");

    	Namespace nsDefault = factory.createNamespace("", "http://www.w3.org/2002/xforms");
    	Namespace nsH = factory.createNamespace("h", "http://www.w3.org/1999/xhtml");
    	Namespace nsEv = factory.createNamespace("ev", "http://www.w3.org/2001/xml-events");
    	Namespace nsJr = factory.createNamespace("jr", "http://openrosa.org/javarosa");
    	Namespace nsXsd = factory.createNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    	html.add(nsDefault);
    	html.add(nsH);
    	html.add(nsEv);
    	html.add(nsJr);
    	html.add(nsXsd);
    	html.add(head);
    	html.add(hBody);

    	Document xml = factory.createDocument();
    	xml.add(html);

    	form.xml = xml.asXML().getBytes();
    	form.json = body.getBytes();
    	form.save();

    	// renderXml(xml.asXML());
    	renderText("ok");
    }

}