package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import models.Data;
import models.Form;
import models.Ngo;
import models.UnitData;
import models.User;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.i18n.Lang;
import play.mvc.*;

@With(Deadbolt.class)
public class Respondents extends Controller {

    @ExternalRestrictions("View Data")
    public static void list() {
    	int currentPage = 1;
		int recordsPerPage = 20;
		int totalPages = 0;
	    int totalRows = 0;

		if (request.params.get("page") != null) {
			currentPage = Integer.parseInt(request.params.get("page"));
		}
        User me = User.find("byLogin", Secure.Security.connected()).first();
        List<Data> data = null;
        Query query = null;
        

      if(me.role.id == 1){
        	//data = Data.find("form_id = 1").fetch();
        	
        	 query = JPA
 					.em()
 					.createQuery(
 							"select d from Data d where form_id=1");
 			
 			totalRows = query.getResultList().size();
 			totalPages = (int) Math.ceil(totalRows * 1.0 / recordsPerPage);
 			
 			data = query.setFirstResult((currentPage - 1) * recordsPerPage).setMaxResults(recordsPerPage)
 					.getResultList();
       }
      else{
          //  data = Data.find("form_id = 1 and ngo in (:ngos) order by id desc").setParameter("ngos", me.ngos).fetch();
            
             query = JPA
					.em()
					.createQuery(
							"select d from Data d where form_id=1 and ngo in (:ngos) order by id desc ")
					.setParameter("ngos", me.ngos);
			
			totalRows = query.getResultList().size();
			totalPages = (int) Math.ceil(totalRows * 1.0 / recordsPerPage);
			
			data = query.setFirstResult((currentPage - 1) * recordsPerPage).setMaxResults(recordsPerPage)
					.getResultList();
        }	
        // Form Names for Export
        List<Form> forms = Form.findAll();
        render(data, forms,totalPages,currentPage,recordsPerPage);
    }
    
    @ExternalRestrictions("View Data")
    public static void view(String id, String lang) {
        
        if(lang == null) {
            lang = "bn";
        }
        Lang.change(lang);
        User me = User.find("byLogin", Secure.Security.connected()).first();
        Data data;
        if(me.role.id == 1){
        	//Logger.info("admin"+id);
        	data = Data.find("respondentId = ?", id).first();
        	//Logger.info(""+data);
        }else{
        	//Logger.info("user");
        	data = Data.find("respondentId = ? and ngo in (:ngos)", id).setParameter("ngos", me.ngos).first();
        }
        notFoundIfNull(data);

        List<UnitData> list = UnitData.find("byData", data).fetch();
        String enableAudioText = Play.configuration.getProperty("aggregate.enableAudioText");
        List<Data> followups = Data.find("respondentId = ? order by id desc", id).fetch();
        
        List<UnitData> images = null;
        if(!followups.isEmpty()) {
            images = UnitData.find("type = 'image' and data in (:list)").setParameter("list", followups).fetch();
        }
        
        render(data, list, enableAudioText, followups, images, lang);
    }
    
    @ExternalRestrictions("View Data")
    public static void data(Long dataId, String lang) {
        
        if(lang == null) {
            lang = "bn";
        }
        Lang.change(lang);
        
        
        Data data = Data.findById(dataId);
        notFoundIfNull(data);

        List<UnitData> list = UnitData.find("byData", data).fetch();
        String enableAudioText = Play.configuration.getProperty("aggregate.enableAudioText");

        render(data, list, enableAudioText, lang);
    }
    
}
