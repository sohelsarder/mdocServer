package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

import models.Data;
import models.Form;
import models.Ngo;
import models.UnitData;
import models.User;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.data.parsing.UrlEncodedParser;
import play.data.validation.Valid;
import play.mvc.Util;
import play.mvc.With;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;

/**
 * Forms Controller - Control forms related requests and APIs.
 */
@With(Deadbolt.class)
public class Forms extends Controller {

    /**
     * List forms.
     */
	@ExternalRestrictions("View Form")
    public static void list() {
		List<Form> forms = Form.findAll();
		render(forms);
	}

    /**
     * List data.
     *
     * @param id the form id
     */
	@ExternalRestrictions("View Data")
    public static void listData(Long id) {
    	List<Data> data = Data.find("Form_id = ? order by id desc", id).fetch();
    	render(data);
    }

    /**
     * View data.
     *
     * @param id the data id
     */
    @ExternalRestrictions("View Data")
    public static void viewData(Long id) {
    	Data data = Data.findById(id);
    	notFoundIfNull(data);

    	List<UnitData> list = UnitData.find("byData", data).fetch();
    	String enableAudioText = Play.configuration.getProperty("aggregate.enableAudioText");

    	render(data, list, enableAudioText);
    }

	/**
	 * Add form page.
	 */
    @ExternalRestrictions("Edit Form")
    public static void create() {
		render();
	}

	/**
	 * Save form.
	 *
	 * @param form the form
	 */
    @ExternalRestrictions("Edit Form")
    public static void save(@Valid Form form) {
		if(validation.hasErrors()) {
			render("@create", form);
		} else {
			form.save();
			list();
		}
	}

    @ExternalRestrictions("Edit Form")
    public static void delete(Long id) {
    	if(request.isAjax()) {
	    	notFoundIfNull(id, "id not provided");
	    	Form form = Form.findById(id);
	    	notFoundIfNull(form, "form not found");
	    	form.delete();
	    	ok();
    	}
    }

    @ExternalRestrictions("Edit Form")
    public static void build(Long id) {
    	Form form = Form.findById(id);
    	String json;
    	if(form.json == null) {
    		json = "{}";
    	}
    	else {
    		json = new String(form.json).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
    	}
    	render(id, json, form);
    }
    
    @ExternalRestrictions("Edit Form")
    public static void exportForm(Long id) {
        Form form = Form.findById(id);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + form.title + ".json\"");
        renderText(new String(form.json));
    }

    @ExternalRestrictions("Export Data")
    public static void exportAllData() {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "Balika" + ".csv\""); 
        //List<UnitData> unitData = UnitData.findAll();//.find("byForm", form).fetch();
        Set<String> titles=new LinkedHashSet<String>();
        // all CSV row stored here
        HashMap<String, List<String>> filteredData = new HashMap<String, List<String>>();
        
        //set of all title 
        List<UnitData> ls = UnitData.find("SELECT u FROM UnitData u ORDER BY form_id,id ASC").fetch();
        for(UnitData title : ls){
            titles.add(""+title.titleVar);
        }       
        
        //each row in a CSV
        List<String> tmpRow = new ArrayList<String>(titles);
        
        for(UnitData unit : ls){
    		if(filteredData.containsKey(unit.data.respondentId)){
    			tmpRow = filteredData.get(unit.data.respondentId);
    		}
    		else{
    			tmpRow = new ArrayList<String>(titles);
    		}
 
    		if(tmpRow.contains(unit.titleVar)){
    			String unitValue ="";
    			if(unit.valueVar!=null){
    				//solution for unwanted new line in value 
    				unitValue = unit.valueVar.replaceAll("\\n", "").replace(",", " ");
    				//',' replace by space because ',' does not support as a value in CSV file
    				//unitValue = unit.valueVar.replace(",", " ");
    			}
    			tmpRow.set( tmpRow.indexOf(unit.titleVar), unitValue);
    		}

    		filteredData.put(unit.data.respondentId, tmpRow);
    	}
    	
    	//String output = "date,sector,received,interviewer,"+StringUtils.join(titles,",")+"\n";
    	String output = "";
        long counter = 0;        
    	for(Entry<String, List<String>> item : filteredData.entrySet()) {
    		Data data = Data.find("byRespondentId",item.getKey()).first();
    		//output += data.date.toString() + "," + data.sector.cId + "," + data.received.toString() + "," + data.interviewer.name + ",";
    		List<String> tmp = item.getValue();
    		//set empty to 'no value' fields
    		for(String s : titles){
    			if(tmp.contains(s)){
    				tmp.set(tmp.indexOf(s), "");
    			}
    		}
 
    		String ngoName = "";
    		if(!data.sender.ngos.isEmpty()) {
    		    ngoName = data.sender.ngos.iterator().next().name;
    		}
            counter++;
    		output += counter+","+data.startTime.toString() + "," + data.endTime.toString() + "," + data.received.toString() + "," + data.sender.login + "," + ngoName + ",";
    		output += StringUtils.join(tmp,",")+ "\n";
    	}
    	
    	List<String> csvTitle = new ArrayList<String>();
    	for (String title : titles){
    		if(title.contains("/")){
    			int index = title.lastIndexOf("/");
    			csvTitle.add(title.substring(index+1));
    			continue;
    		}
    		csvTitle.add(title);
    	}
    	output = "data,startTime,endTime,receivedTime,userID,ngo," + StringUtils.join(csvTitle, ",") + "\n"+output;
    	//Logger.info("title size-"+csvTitle.size());
    	renderText(output);
    }
    
    
    @ExternalRestrictions("Export Data")
    public static void exportData(Long id) {
    	Form form = Form.findById(id);
    	notFoundIfNull(form);
    	response.setHeader("Content-Disposition", "attachment; filename=\"" + form.title + ".csv\"");	
    	List<UnitData> unitData = UnitData.find("byForm", form).fetch();
    	Set<String> titles=new LinkedHashSet<String>();
    	// all CSV row stored here
    	HashMap<Long, List<String>> filteredData = new HashMap<Long, List<String>>();
    	
    	//set of all title 
    	List<String> ls = UnitData.find("SELECT DISTINCT u.titleVar FROM UnitData u WHERE form = ?",form).fetch();
    	for(String title : ls){
    		titles.add(title);
    	}
    	///Test Doing ; title split
    	/*List<UnitData> ls1 = UnitData.find("SELECT u FROM UnitData u WHERE form = ?  ORDER BY u.id",form).fetch();
    	for(UnitData unit : ls1){
    		if(unit.type.equals("select")){
    			if(unit.valueVar!=null){
	    			String[] values = unit.valueVar.split("\\s+");
	    			int numOfVal = values!=null?values.length:1;
	    			for(int i = 1; i <= numOfVal; i++){
	    				titles.add(unit.titleVar+i);
	    			}
    			}
    		}
    		else{
    			titles.add(unit.titleVar);
    		}
	   	}*/  	
	
    	
    	/*for(UnitData unit : unitData){
    		titles.add(unit.titleVar);
    	} */   	
    	
    	//each row in a CSV
    	List<String> tmpRow = new ArrayList<String>(titles);
    	
    	for(UnitData unit : unitData){
    		if(filteredData.containsKey(unit.data.id)){
    			tmpRow = filteredData.get(unit.data.id);
    		}
    		else{
    			tmpRow = new ArrayList<String>(titles);
    		}
 
    		if(tmpRow.contains(unit.titleVar)){
    			String unitValue ="";
    			if(unit.valueVar!=null){
    				//solution for unwanted new line in value 
    				unitValue = unit.valueVar.replaceAll("\\n", "").replace(",", " ");
    				//',' replace by space because ',' does not support as a value in CSV file
    				//unitValue = unit.valueVar.replace(",", " ");
    			}
    			tmpRow.set( tmpRow.indexOf(unit.titleVar), unitValue);
    		}

    		/*if(unit.type.equals("select") && unit.valueVar!=null){
    			String[] values = unit.valueVar.split("\\s+");
    			Logger.info(values.length+"dsd"+unit.valueVar);
    			int i = 1;
    			for(String val : values){
    				if(tmpRow.contains(unit.titleVar+i)){
    					Logger.info(unit.titleVar+i);
    					tmpRow.set( tmpRow.indexOf(unit.titleVar+(i++)), val);
    				}
    				titles.add(unit.titleVar+i);
    			}
    		}*/
    		
    		filteredData.put(unit.data.id, tmpRow);
    	}
    	
    	//String output = "date,sector,received,interviewer,"+StringUtils.join(titles,",")+"\n";
    	String output = "";
    	long counter = 0;
        for(Entry<Long, List<String>> item : filteredData.entrySet()) {
    		Data data = Data.findById(item.getKey());
    		//output += data.date.toString() + "," + data.sector.cId + "," + data.received.toString() + "," + data.interviewer.name + ",";
    		List<String> tmp = item.getValue();
    		//set empty to 'no value' fields
    		for(String s : titles){
    			if(tmp.contains(s)){
    				tmp.set(tmp.indexOf(s), "");
    			}
    		}
 
    		String ngoName = "";
    		if(!data.sender.ngos.isEmpty()) {
    		    ngoName = data.sender.ngos.iterator().next().name;
    		}
            counter++;
    		output += counter+","+data.startTime.toString() + "," + data.endTime.toString() + "," + data.received.toString() + "," + data.sender.login + "," + ngoName + ",";
    		output += StringUtils.join(tmp,",")+ "\n";
    	}
    	
    	List<String> csvTitle = new ArrayList<String>();
    	for (String title : titles){
    		if(title.contains("/")){
    			int index = title.lastIndexOf("/");
    			csvTitle.add(title.substring(index+1));
    			continue;
    		}

    		csvTitle.add(title);
    	}

    	output = "data,startTime,endTime,receivedTime,userID,ngo," + StringUtils.join(csvTitle, ",") + "\n"+output;
    	//Logger.info("title size-"+csvTitle.size());
    	renderText(output);
    }
    
    /**This method is currenlty unused - alternative(new) method is 'exportData'*/
    @ExternalRestrictions("Export Data")
    public static void exportCSV(Long id) {
    	Form form = Form.findById(id);
    	response.setHeader("Content-Disposition", "attachment; filename=\"" + form.title + ".csv\"");
    	User me = User.find("byLogin", Secure.Security.connected()).first();
        List<UnitData> listData;
        if(me.role.id == 1){
        	listData = UnitData.find("form = ?", form).fetch();
        }
        else{
        	listData = UnitData.find("form = ? and ngo in (:ngos)", form).setParameter("ngos", me.ngos).fetch();
        }
    	HashMap<Long, List<String>> filteredData = new HashMap<Long, List<String>>();
    	List<String> titles = new ArrayList<String>();
    	int taken = 0;
    	for(UnitData unit: listData) {
    		List<String> tmpList = null;
    		if(filteredData.containsKey(unit.data.id)) {
    			tmpList = filteredData.get(unit.data.id);
    		} else {
    			tmpList = new ArrayList<String>();
    			taken++;
    		}
    		if(taken == 1) {
				titles.add(unit.titleVar);
			}
    		tmpList.add(unit.valueVar);
    		filteredData.put(unit.data.id, tmpList);
    	}

    	String output = "startTime,endTime,receivedTime,userID,ngo," + StringUtils.join(titles, ",") + "\n";
    	for(Entry<Long, List<String>> item : filteredData.entrySet()) {
    		Data tData = Data.findById(item.getKey());
    		String ngoName = "";
    		if(!tData.sender.ngos.isEmpty()) {
    		    ngoName = tData.sender.ngos.iterator().next().name;
    		}
    		output += tData.startTime.toString() + "," + tData.endTime.toString() + "," + tData.received.toString() + "," + tData.sender.login + "," + ngoName + ",";
    		output += StringUtils.join(item.getValue(), ",") + "\n";
    	}
    	
    	renderText(output);
    }

    @ExternalRestrictions({"Edit Data", "View Data"})
    public static void updateAudioData() {
    	Map<String, String[]> items = UrlEncodedParser.parseQueryString(request.body);
    	Long dataId = Long.parseLong(items.get("id")[0]);
    	if(dataId > 0) {
    		Data data = Data.findById(dataId);
    		if(data != null) {
    			data.audioData = items.get("audioText");
    			data.save();
		    	ok();
    		}
    	}
    	error();
    }

}
