package controllers;


import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import play.Play;

public class PushServer {

	private static BlockingConnection connection;

	private static short KEEP_ALIVE_TIME = 1800;
	
	static {
		String host = Play.configuration.getProperty("mqtt.host");
		Integer port = Integer.parseInt(Play.configuration
				.getProperty("mqtt.port"));

		MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(host, port);
			mqtt.setKeepAlive(KEEP_ALIVE_TIME);
			connection = mqtt.blockingConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendPush(String hwUserName, String message) {
		play.Logger.info("sendPush start: " + hwUserName);
		if (!connection.isConnected()) {
			try {
				connection.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			connection.publish(hwUserName, message.getBytes(),
					QoS.EXACTLY_ONCE, false);
					play.Logger.info("sendPush end");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void notifyHLP(String hwUserName) {
		play.Logger.info("notifyHLP call");
		sendPush(hwUserName, "HLP");
	}

	public static void notifyLAB_LessThan37(String hwUserName) {
		sendPush(hwUserName, "LABLT37");
	}

	public static void notifyLAB_GreaterThan37(String hwUserName) {
		sendPush(hwUserName, "LABGT37");
	}

	public static void notifyBNT_FirstGreaterThan16(String hwUserName) {
		sendPush(hwUserName, "BNTFGT16");
	}

	public static void notifyBNT_SecondGreaterThan10(String hwUserName) {
		sendPush(hwUserName, "BNTSGT10");
	}

	/*
	 * 
	 * public static void updateActiveSector(String sector) { sendPush(sector,
	 * "activeSectorUpdate"); }
	 * 
	 * public static void updateActiveSector(Integer sector) {
	 * updateActiveSector("" + sector); }
	 * 
	 * public static void updateSchedule(String sector) { sendPush(sector,
	 * "scheduleUpdate"); }
	 * 
	 * public static void updateSchedule(Integer sector) { updateSchedule("" +
	 * sector); }
	 */

}
