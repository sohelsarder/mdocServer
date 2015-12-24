package jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import models.Data;
import models.UnitData;
import models.appointments;
import models.patients;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.XML;
import play.libs.XPath;
import utils.CommonUtil;

public class ExtractData extends Job {

	public enum MediaType {
		AUDIO, VIDEO
	}

	private Data data;

	public ExtractData(Data data) {
		this.data = data;
	}

	@Override
	public void doJob() throws Exception {
		Document formXml = XML.getDocument(new String(this.data.form.xml));
		Document dataXml = XML.getDocument(new String(this.data.xml));
		
		// ##### DEBUG
		// This is how you get the Form's Name (Assuming its the best way to
		// recognize forms)
		Logger.info("Form Name: " + this.data.form.title
				+ ":: Now Entering Extraction");
		String formName = this.data.form.title;
		patients pat = null;
		appointments app = null;
		String patCode = null;
		long rmpId;
		if (formName.equals("Patient Registration")){
			pat = new patients();
			pat.status = 1;
			pat.created = new Date();
			pat.modified = pat.created;
		}else if (formName.equals("Patient Session")){
			app = new appointments();
			app.status = 0;
			app.created = new Date();
			app.modified = app.created;
			Logger.info(this.data.respondentId +" =:= "+ this.data.respondentNumber);
			//app.rmp_id = Integer.parseInt(this.data.respondentId);
		}
		
		for (Node root : XPath.selectNodes("//bind", formXml)) {
			if (root.hasAttributes()) {
				// Variable Name Associated with the data
				String nodeId = root.getAttributes().getNamedItem("nodeset")
						.getNodeValue().substring(6);
				Logger.info("Node Id: " + nodeId);
				String type = root.getAttributes().getNamedItem("type") != null ? root
						.getAttributes().getNamedItem("type").getNodeValue()
						: "group";
				Logger.info("Type : " + type);
				String title = XPath.selectText("//text[contains(@id, '/data/"
						+ nodeId + ":label')]/value", formXml);
				Logger.info("Title : " + title);
				// for cascade data
				if (title == null) {
					title = XPath.selectText("//select1[contains(@ref,'/data/"
							+ nodeId + "')]/label", formXml);
				}
				// Value Associated with the variable name
				// Unfiltered
				String value = XPath.selectText("//" + nodeId, dataXml);
				Logger.info("Value : " + value);
				String valueVar = value;
				if (type.equals("group")) {
					Node dataNode = XPath.selectNode("//" + nodeId, dataXml);
					int dataCounter = 0;
					if (dataNode != null && dataNode.hasChildNodes()) {
						dataCounter = dataNode.getChildNodes().getLength();
					}
					for (Node child : XPath.selectNodes("//" + nodeId + "/*",
							dataXml)) {
						String s = child.getNodeName();
						if (s.indexOf("table_list") != -1
								|| s.indexOf("field_list") != -1
								|| s.indexOf("table_list") != -1) {
							dataCounter--;
						}
					}

					value = "" + dataCounter;
					valueVar = "" + dataCounter;
				}

				if (nodeId.contains("i_start_time")
						|| nodeId.contains("i_end_time")
						|| nodeId.contains("deviceid")) {
					continue;
				}

				String extraValue = null;
				if (value != null && value.length() > 0) {
					// Binary files like Audio, Video, Images
					if (type.equals("binary")) {
						String extension = CommonUtil.getExtension(value);
						if (extension.equals("jpg")) {
							type = "image";
						} else if (extension.equals("3gp")
								|| extension.equals("mp4")) {
							type = "video";
							if (extension.equals("3gp")) {
								value = convert(value, "mp4");
							}
						} else if (extension.equals("3gpp")
								|| extension.equals("3ga")
								|| extension.equals("m4a")
								|| extension.equals("amr")) {
							type = "audio";
							if (extension.equals("amr")) {
								value = convert(value, "mp3", MediaType.AUDIO);
							}
						}
					}
					// Location (Latitude and Longitude)
					else if (type.equals("geopoint")) {
						type = "gmap";
						String[] geo = value.split(" ");
						value = geo[0] + "," + geo[1];

						HttpResponse res = WS.url(
								"http://maps.googleapis.com/maps/api/geocode/xml?latlng="
										+ value + "&sensor=false").get();
						if (res.success()) {
							Document xml = res.getXml();
							extraValue = XPath
									.selectText(
											"GeocodeResponse/result[1]/formatted_address",
											xml);
						}
					}
					// Checkbox
					else if (type.equals("select")) {
						String[] tokens = value.split(" ");
						value = "";
						for (String token : tokens) {
							value += XPath.selectText(
									"//text[contains(@id, '/data/" + nodeId
											+ ":" + token + "')]/value",
									formXml)
									+ "\n";
						}
					}
					// Radio Button
					else if (type.equals("select1")) {
						value = XPath.selectText("//text[contains(@id, '/data/"
								+ nodeId + ":" + value + "')]/value", formXml);
						// code for mTikka form - cascade data extract
						if (value == null) {
							String ref = getRefference(nodeId);
							value = XPath.selectText("//text[contains(@id, '"
									+ ref + "')]/value", formXml);
						}
					}
				}
				

				// split values for multiple select and saves multiple UnitData
				if (type.equals("select")) {
					if (valueVar != null && value != null) {
						String[] values = value.split("\\n");
						String[] valueVars = valueVar.split(" ");
						Logger.info(">>>" + StringUtils.join(values) + " "
								+ StringUtils.join(valueVars));

						for (int i = 0; i < valueVars.length; i++) {
							String tmpValue = "", tmpValueVar = "";
							tmpValueVar = valueVars[i];
							if (i >= values.length) {
								tmpValue = "";
							} else {
								tmpValue = values[i];
							}
							new UnitData(this.data, this.data.form,
									this.data.ngo, nodeId + (i + 1), title,
									type, tmpValueVar, tmpValue, extraValue)
									.save();
						}
					}
				} else {
					new UnitData(this.data, this.data.form, this.data.ngo,
							nodeId, title, type, valueVar, value, extraValue)
							.save();
					if (formName.equals("Patient Registration") && nodeId.equals("Patient_Image")){
						pat.image = value;
					}else if (formName.equals("Patient Registration") && nodeId.equals("firstPage/Patient_Name")){
						pat.name = value;
					}else if (formName.equals("Patient Registration") && nodeId.equals("firstPage/Patient_MobileNo")){
						pat.phone = value;
					}else if (formName.equals("Patient Registration") && nodeId.equals("SecondPage/Patient_Age_Year")){
						pat.age = value + " Year(s)";
					}else if (formName.equals("Patient Registration") && nodeId.equals("SecondPage/Patient_Age_Month")){
						if (value != null)
							pat.age += " " + value + " Month(s)";
					}else if (formName.equals("Patient Registration") && nodeId.equals("SecondPage/Patient_Age_Day")){
						if (value != null)
							pat.age += " " + value + " Day(s)";
					}else if (formName.equals("Patient Registration") && nodeId.equals("SecondPage/Patient_Gender")){
						pat.gender = Integer.parseInt(valueVar);
					}else if (formName.equals("Patient Registration") && nodeId.equals("Patient_ID")){
						pat.code = value;
					}else if (formName.equals("Patient Session") && nodeId.equals("Patient_ID")){
						patCode = value;
					}else if (formName.equals("Patient Session") && nodeId.equals("Second_page/Patient_Weight_kg")){
						app.weight = value + " Kg ";
					}else if (formName.equals("Patient Session") && nodeId.equals("Third_page/Patient_PulseRate")){
						app.pulse = Integer.parseInt(value);
					}else if (formName.equals("Patient Session") && nodeId.equals("Third_page/Patient_BP_Systolic")){
						app.bp = value + " / ";
					}else if (formName.equals("Patient Session") && nodeId.equals("Third_page/Patient_BP_Diastolic")){
						app.bp += value;
					}else if (formName.equals("Patient Session") && nodeId.equals("Second_page/Patient_Temperature")){
						app.temperature = Float.parseFloat(value);
					}else if (formName.equals("Patient Session") && nodeId.equals("Third_page/Patient_RespiratoryRate")){
						app.respiration = Integer.parseInt(valueVar);
					}else if (formName.equals("Patient Session") && nodeId.equals("Fourth_page/Patient_Appearance")){
						app.appearance = Integer.parseInt(valueVar);
					}else if (formName.equals("Patient Session") && nodeId.equals("Fourth_page/Patient_Color")){
						app.color = Integer.parseInt(valueVar);
					}else if (formName.equals("Patient Session") && nodeId.equals("Fourth_page/Patient_Consciousness")){
						app.consciousness = Integer.parseInt(valueVar);
					}else if (formName.equals("Patient Session") && nodeId.equals("Fifth_page/Patient_Edema")){
						app.edema = Integer.parseInt(valueVar);
					}else if (formName.equals("Patient Session") && nodeId.equals("Fifth_page/Patient_Dehydration")){
						app.dehydration = Integer.parseInt(valueVar);
					}else if (formName.equals("Patient Session") && nodeId.equals("Patient_picture")){
						if (value != null)
						app.diseaseImage = value;
					}
				}
			}
		}
		// ##### THIS IS WHERE I ENTER DATA PERIOD
		// ENTER WHATEVER DATA NOW
		if (this.data.form.title.equals("Patient Registration") && pat != null) {
			Logger.info(pat.toString());
			pat.save();
		} else if (this.data.form.title.equals("Patient Session") && app != null) {
			Logger.info(app.toString());
			patients patCheck = patients.findByCode(patCode);
			rmpId = data.sender.id;
			app.rmp_id = rmpId;
			if (patCheck == null)
				throw new Exception("QR Code not recognized");
			app.patient_id = patCheck.id;
			app.save();
		}
		Logger.info("Data-%d extracted", this.data.id);
		/*
		 * List<Data> allData =
		 * Data.find("isExtracted is false or isExtracted is null").fetch();
		 * if(allData.size() > 0) { String locationVar =
		 * Play.configuration.getProperty("aggregate.locationMapping");
		 * if(locationVar != null) { for(Data data: allData) { Document dataXml
		 * = XML.getDocument(new String(data.xml)); String locationString =
		 * XPath.selectText("//data/" + locationVar, dataXml); String
		 * locationArray[] = locationString.split(" "); // ignore altitude
		 * data.latitude = Double.parseDouble(locationArray[0]); data.longitude
		 * = Double.parseDouble(locationArray[1]); data.accuracy =
		 * Double.parseDouble(locationArray[3]); // TODO : fix manual data.image
		 * = XPath.selectText("//data/picMother", dataXml); data.isExtracted =
		 * true; data.save(); } }
		 * 
		 * 
		 * 
		 * Logger.info("%d location info extracted successfully :)",
		 * allData.size()); }
		 */
	}

	private String convert(String fileName, String extension) {
		return convert(fileName, extension, MediaType.VIDEO);
	}

	private String convert(String fileName, String extension, MediaType type) {
		// Get the default upload directory from application.
		String path = Play.applicationPath.getAbsolutePath()
				+ File.separator
				+ Play.configuration.getProperty("aggregate.uploadDir",
						"uploads") + File.separator;
		String oldFileName = fileName;
		fileName = CommonUtil.getName(oldFileName) + "_cc." + extension;

		if (!new File(path + fileName).exists()) {
			Runtime runtime = Runtime.getRuntime();
			Process process = null;
			try {
				// `libx264-baseline.ffpreset` file must be available in the
				// `$HOME/.ffmpeg/` folder or comment out the following line
				// Don't run from eclipse, video conversion will not work
				String[] cmdArray;
				if (type == MediaType.AUDIO) {
					cmdArray = new String[] { "ffmpeg", "-i",
							path + oldFileName, "-ar 22050", path + fileName };
				} else {
					cmdArray = new String[] { "ffmpeg", "-i",
							path + oldFileName, "-vcodec libx264",
							"-vpre baseline", "-crf 24", "-g 25",
							"-acodec libfaac", "-ab 192k", "-ar 44100",
							path + fileName };
				}
				String cmd = StringUtils.join(cmdArray, " ");
				process = runtime.exec(cmd);

				// Wait for ffmpeg to complete
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (process.exitValue() != 0) {
					Logger.info(cmd);
					BufferedReader errorReader = new BufferedReader(
							new InputStreamReader(process.getErrorStream()));
					System.out.println("Error:  " + errorReader.readLine());
					Logger.info(
							"Error - %d, ffmpeg conversion of %s to %s was unsuccesful",
							process.exitValue(), path + oldFileName, path
									+ fileName);
				} else {
					Logger.info("File saved in %s", path);
				}

			} catch (Exception e) {
				e.printStackTrace();
				Logger.info("ffmpeg conversion could not start");
			} finally {
				if (process != null) {
					process.destroy();
				}
			}
		}
		return fileName;
	}

	/**
	 * 
	 * @param nodeId
	 *            - node of data xml
	 * @return text_id - id of text node in form xml
	 * */
	public String getRefference(String nodeId) {
		Document formXml = XML.getDocument(new String(this.data.form.xml));
		Document dataXml = XML.getDocument(new String(this.data.xml));
		// String nodeId = "upazila";
		String text_id = null;

		java.util.List<org.w3c.dom.Node> items = XPath.selectNodes(
				"//instance[contains(@id, '" + nodeId + "')]/root/item",
				formXml);
		for (org.w3c.dom.Node item : items) {
			String valueVar = null;
			String reff = null;
			for (org.w3c.dom.Node childNode : XPath.selectNodes("*", item)) {
				if (!childNode.getNodeName().equals("name")
						&& !childNode.getNodeName().equals("itextId")) {
					if (childNode.getTextContent().equals(
							XPath.selectText("//" + childNode.getNodeName(),
									dataXml))) {
						continue;
					}
					break;
				} else if (childNode.getNodeName().equals("name")
						&& childNode.getTextContent().equals(
								XPath.selectText("//" + nodeId, dataXml))) {
					valueVar = childNode.getTextContent();
					reff = XPath.selectText("itextId", item);
				}
			}
			if (reff != null) {
				text_id = reff;
				break;
			}
		}

		return text_id;
	}
}
