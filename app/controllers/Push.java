package controllers;

import models.appointments;
import models.patients;
import play.Logger;
import play.mvc.Controller;

public class Push extends Controller {

	public static void presDone(long rmpId, long appId) {
		appointments app = appointments.findById(appId);
		String[] pres = appointments.getPrescriptionUrl(appId);
		patients pat = patients.findById(app.patient_id);
		if (pres != null) {

			String msg = "{";
			msg += "\"type\":" + 0 + ",";
			msg += "\"presUrl\":\"" + pres[0] + "\",";
			msg += "\"patName\":\"" + pat.name + "\",";
			msg += "\"pdfName\":\"" + pres[1] + "\"";
			msg += "}";
			Logger.info("JSON Formatted: " + msg + "TOPIC:" + "amdoc/not/"
					+ rmpId);
			String topic = "amdoc/not/" + rmpId;
			Logger.info(topic);
			PushServer.sendPush(topic, msg);
		}
	}

	public static void callStart(long rId, String rmpId, String docId,
			String doctorName, String patientName, int frameRate) {
		Logger.info("HIT");
		String msg = "{";
		msg += "\"type\":" + 1 + ",";
		msg += "\"rmpId\":\"" + rmpId + "\",";
		msg += "\"docId\":\"" + docId + "\",";
		msg += "\"docName\":\"" + doctorName + "\",";
		msg += "\"patName\":\"" + patientName + "\",";
		msg += "\"frame\":\"" + frameRate + "\"";
		msg += "}";
		Logger.info("JSON Formatted: " + msg + "TOPIC:" + "amdoc/not/" + rId);
		String topic = "amdoc/not/" + rId;
		Logger.info(topic);
		PushServer.sendPush(topic, msg);
	}

}
