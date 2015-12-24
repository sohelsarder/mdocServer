package controllers;

import java.io.File;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.persistence.Entity;

import jobs.ExtractData;
import models.Data;
import models.Form;
import models.User;
import models.appointments;
import models.patients;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;

import controllers.Users.LoginResponse;

import play.Logger;
import play.Play;
import play.data.FileUpload;
import play.db.jpa.Model;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Before;
import play.mvc.Controller;
import utils.CommonUtil;

/**
 * Mobile Controller - Mobile API end points handlers.
 */
public class Mobile extends Controller {

	@Before
	static void checkDigestAuth() {
		if (!DigestRequest.isAuthorized(request)) {
			throw new UnauthorizedDigest("Super Secret Stuff");
		}
	}

	/**
	 * List forms as XML (API end point).
	 */
	@annotations.Mobile
	public static void listAsXml() {
		List<Form> forms = Form.findAll();
		request.format = "xml";
		// response.setHeader("X-OpenRosa-Version", "1.0");

		render(forms);
	}

	/**
	 * View the form as XML (API end point).
	 * 
	 * @param id
	 *            the id
	 * @throws Exception
	 *             the exception
	 */
	@annotations.Mobile
	public static void viewAsXml(Long id) throws Exception {
		Form form = Form.findById(id);
		if (form == null) {
			throw new Exception();
		}
		String xmlText = new String(form.xml);
		renderXml(xmlText);
	}

	/**
	 * Handle the form data (API end point).
	 * 
	 * @param xml_submission_file
	 *            name of the incoming XML, to notify the method that it should
	 *            take multipart/data
	 */
	@annotations.Mobile
	public static void submit(File xml_submission_file) {
		if (request.method.equalsIgnoreCase("POST")) {
			// Supported MIME Types
			Set<String> mime_types = new HashSet<String>(
					Arrays.asList(new String[] { "text/xml", "image/jpeg",
							"audio/3gpp", "video/3gpp", "video/mp4",
							"text/csv", "audio/amr", "application/vnd.ms-excel" }));
			// Supported file extensions
			Set<String> extensions = new HashSet<String>(
					Arrays.asList(new String[] { "xml", "jpg", "3gpp", "3gp",
							"mp4", "csv", "amr", "xls", "3ga" }));
			// Get the default upload directory from application.
			String path = Play.configuration.getProperty("aggregate.uploadDir")
					+ File.separator;

			// Get response files
			List<FileUpload> uploads = (List<FileUpload>) request.args
					.get("__UPLOADS");

			// Check if it has any file or not
			if (uploads.size() > 0 && xml_submission_file != null) {
				long ret = 0;
				Boolean isSuccessful = false;
				for (int i = 0; i < uploads.size(); ++i) {
					FileUpload tmpFile = uploads.get(i);
					String fileName = tmpFile.getFileName();
					// First file should be the 'xml_submission_file' file
					if (i == 0) {
						if (tmpFile.getFieldName()
								.equals("xml_submission_file")) {
							Document dataXml = XML
									.getDocument(xml_submission_file);
							Long formId = Long.parseLong(XPath.selectText(
									"/data/@id", dataXml));
							Form form = Form.findById(formId);
							Data data = new Data(form, tmpFile.asBytes());

							String iStartTime = XPath.selectText(
									"//data/i_start_time", dataXml);
							String iEndTime = XPath.selectText(
									"//data/i_end_time", dataXml);
							// String householdId =
							// XPath.selectText("//data/HH_ID", dataXml);
							/*
							 * String respondentId =
							 * XPath.selectText("//data/Respondent_ID",
							 * dataXml);
							 */
							// String respondentId =
							// XPath.selectText("//data/Response_ID", dataXml);
							String respondentName = XPath.selectText(
									"//data/firstPage/Patient_Name", dataXml);
							String respondentId = XPath.selectText(
									"//data/Patient_ID", dataXml);
							// String respondentNumber =
							// XPath.selectText("//data/Respondent_Number",
							// dataXml);
							Logger.info("respondent-" + respondentId);
							notFoundIfNull(respondentId);

							data.sender = User.findByLogin(session
									.get("apiUser"));

							// Respondent id check
							Data test = Data.find("byRespondentId",
									respondentId).first();
							if (form.title.equals("Patient Registration")) {
								if (test != null) {
									forbidden("Patient is already registered.");
								}
							} else if (form.title.equals("Patient Session")) {
								if (test == null) {
									forbidden("Patient code is invalid.");
								}
								patients pat = patients
										.findByCode(respondentId);
								Logger.info("Patient Id: " + pat.id);
								Logger.info("Sender Id: " + data.sender.id);
								appointments pending = appointments
										.findByRmpAndPatient(data.sender.id,
												pat.id);
								if (pending != null && pending.status == 0) {
									forbidden("This patient already has an appointment.");
								}
							}
							/*
							 * else { if(test == null) {
							 * forbidden("Id not found!"); } else{ Data
							 * testChild = Data.find("byRespondentIdAndForm",
							 * respondentId, Form.findById(formId)).first();
							 * if(testChild != null){
							 * forbidden("Form Already Submitted!"); } } }
							 */
							DateTimeFormatter dtf = ISODateTimeFormat
									.dateTime(); // ISO8601 (XML) Date/time
							data.startTime = dtf.parseDateTime(iStartTime)
									.toDate();

							data.endTime = dtf.parseDateTime(iEndTime).toDate();

							// data.householdId = householdId;
							data.respondentId = respondentId;
							data.respondentName = respondentName;
							// data.respondentNumber = respondentNumber;

							// Logger.info("data.sender :" + data.sender);
							data.ngo = data.sender.ngos.iterator().next();
							data = data.save();
							// Run as a Job to give quick response in mobile
							new ExtractData(data).now();
							ret = data.id;
							isSuccessful = true;
						} else {
							// Invalid File
							Logger.info("XML file expected but got '%s'",
									tmpFile.getFieldName());
							break;
						}
					} else {
						String extension = CommonUtil.getExtension(fileName);
						// Save only allowed files
						if (mime_types.contains(tmpFile.getContentType())
								&& extensions.contains(extension)) {
							tmpFile.asFile(path + fileName);

						} else {
							Logger.info("Unsupported Content: %s %s",
									tmpFile.getContentType(),
									CommonUtil.getExtension(fileName));
						}
					}
				}
				if (isSuccessful) {
					response.status = 201;
					response.setHeader("Location", "http://" + request.host);
					// renderText("" + ret);
					renderText("<h1 id=\"success\">Form Data Sent Successfully</h1>");
				}
			}
		} else {
			response.status = 204;
		}
	}

	@annotations.Mobile
	public static void getPending(long rmpId) {
		List<appointments> apps = appointments.findByPending(rmpId);
		PendingPrescriptionResponse pres = new PendingPrescriptionResponse(apps);
		renderJSON(pres);
	}

	public static class PendingPrescriptionResponse {

		public String[] urls;
		public String[] names;

		public PendingPrescriptionResponse(List<appointments> prescriptions) {
			urls = new String[prescriptions.size()];
			names = new String[prescriptions.size()];
			for (int i = 0; i < urls.length; i++) {
				appointments app = prescriptions.get(i);
				String[] data = app.getPrescriptionUrl(app.id);
				urls[i] = data[0];
				names[i] = data[1];
			}
		}

	}

	@annotations.Mobile
	public static void loginPatient(String code) {
		patients pat = patients.findByCode(code);
		Logger.info("Passed Code = " + code);
		if (pat == null)
			forbidden("Incorrect Code. Please Enter a valid Code.");

		renderJSON(new LoginResponse(pat));
	}

	@annotations.Mobile
	public static void getPrescriptions(long patId) {
		patients pat = patients.findById(patId);
		List<appointments> appList = appointments.findByPatient(pat.id);

		renderJSON(new PrescriptionList(appList));
	}

	public static class PrescriptionList {
		public String[] appointmentId;
		public String[] appointmentDate;
		public String[] status;
		public String[] prescriptionUrl;
		public String[] filename;

		public PrescriptionList(List<appointments> appList) {
			appointmentId = new String[appList.size()];
			appointmentDate = new String[appList.size()];
			status = new String[appList.size()];
			prescriptionUrl = new String[appList.size()];
			filename = new String[appList.size()];
			int i = 0;
			Iterator<appointments> iter = appList.iterator();
			while (iter.hasNext()) {
				appointments app = iter.next();
				Logger.info("Appointment Id=" + app.id);
				appointmentId[i] = "" + app.id;
				appointmentDate[i] = app.created.toString();
				status[i] = (app.status == 0) ? "New" : "Archived";
				String[] res = appointments.getPrescriptionUrl(app.id);
				prescriptionUrl[i] = res[0];
				filename[i] = res[1];
				i++;
			}
		}
	}

	public static class LoginResponse {
		public String name;
		public String age;
		public String gender;
		public String phone;
		public String image;
		public long id;

		public LoginResponse(patients pat) {
			this.name = pat.name;
			this.age = pat.age;
			this.gender = (pat.gender == 1) ? "Male" : "Female";
			this.phone = pat.phone;
			this.image = "" + pat.image;
			this.id = pat.id;
			Logger.info("\n " + name + "\n " + age + "\n " + gender + "\n "
					+ phone + "\n " + image + "\n " + id);
		}
	}

	@annotations.Mobile
	public static void getTableData(String d) {
		List<User> users = User.findAll();
		response.setHeader("Content-Disposition",
				"attachment; filename=\"Users.csv\"");
		StringBuilder sb = new StringBuilder();
		int i = 0;
		ArrayList<String> results = new ArrayList<String>();
		while (i < users.size()) {
			User u = users.get(i);
			sb.append(u.name).append(", ").append(u.phone).append(", ")
					.append(u.age);
			sb.append("\n");
			i++;
		}
		renderText(sb.toString());
	}

	@annotations.Mobile
	public static void createModel(String d) {
		String model_imports = "package models;"
				+ "import javax.persistence.Entity;"
				+ "import play.data.validation.Required;"
				+ "import play.data.validation.Unique;"
				+ "import play.db.jpa.GenericModel;"
				+ "import play.db.jpa.Model;";

		String class_definition = "\n@Entity\npublic class GeneratedModel extends Model { "
				+ "public GeneratedModel(){" + "this.name=\"Test\"; " + "}";

		String fields = "public String name;public String role;";

		String end = "}";
		String root = Play.applicationPath.getAbsolutePath() + "/app/models/";
		File f = new File(root + "GeneratedModel.java");
		try {
			// f.delete();
			f.createNewFile();
			PrintWriter out = new PrintWriter(f);
			out.write(model_imports);
			out.write(class_definition);
			out.write(fields);
			out.write(end);
			out.close();
		} catch (Exception e) {
		}
		try {
			Class m = Class.forName("models.GeneratedModel");
			m.asSubclass(Class.forName("play.db.jpa.Model"));
			Logger.info("Generated Model Found " + m.getName());
			Constructor<?> c = m.getConstructor();
			Model instance = (Model) c.newInstance();
			Method method = m.getMethod("findAll", null);
			List<Model> list = (List<Model>) method.invoke(m, null);

			for (int i = 0; i < 1; i++) {
				Class model = list.get(i).getClass();
				Field ff = model.getField("name");
				// ff.
				Logger.info("Dynamic class Instantiation and data retrieval :"
						+ (String) ff.get(instance));
				instance.save();
			}
		} catch (Exception e) {
			Logger.info("Class Exception " + e.getMessage(), "");
		}
	}
}
