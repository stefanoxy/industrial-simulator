package it.stefano.machinesimulator.mqtt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import it.stefano.machinesimulator.helper.CryptoHelper;
import it.stefano.machinesimulator.helper.JsonHelper;
import it.stefano.machinesimulator.machine.MachineException;
import lombok.extern.slf4j.Slf4j;

/**
 * Classe che rappresenta la connessione di un client (macchinario o Control Room) al broker MQTT. 
 */
@Slf4j
public class MqttManager
{
	private final String mqttClientId;
	private final MqttClient mqttClient;

	public MqttManager(String mqttClientId, String mqttBroker, String mqttUsername, String mqttPassword, String keystoreFile, String keystorePassword /* , String keyAlias, String keyPassword */) throws MqttManagerException {

		this.mqttClientId = mqttClientId;
		try {
			log.info("Starting MQTT Client with id=" + mqttClientId);
			mqttClient = getMqtts(mqttClientId, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword);
		}
		catch (Exception e) {
			throw new MqttManagerException("Exception starting MqttManager", e);
		}
	}

	/**
	 * Effettua il subscribe a un topic del broker
	 */
	public void subscribe(String topic,  IMqttMessageListener mqttListener) throws MqttManagerException
	{
		try {
			mqttClient.subscribe(topic, mqttListener);
		}
		catch (Exception e) {
			throw new MqttManagerException("Exception subscribing topic " + topic, e);
		}
	}

	/**
	 * Invia un messaggio contenuto in una SecureEnvelope al broker MQTT
	 */
	public void sendMessage(SecureEnvelope envelope, String topic, QOS qos, boolean retained, boolean prettyJson) throws MachineException
	{
		try {
			log.info(mqttClientId + ": sending message " + envelope + " to topic " + topic);

			String s = JsonHelper.objectToJson(envelope, prettyJson);
			mqttClient.publish(topic, s.getBytes(StandardCharsets.UTF_8), qos.getQOS(), retained);
		}
		catch (Exception e) {
			throw new MachineException(mqttClientId + ": exception sending message " + envelope + " to topic " + topic, e);
		}
	}

	/**
	 * @return SSLSocketFactory per istanziare connessioni TLS protette da certificato 
	 */
	private static SSLSocketFactory getSocketFactory(String keystoreFile, String keystorePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException, IOException
	{
		KeyStore keyStore = CryptoHelper.loadKeyStore(keystoreFile, keystorePassword);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(keyStore);

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

		return sc.getSocketFactory();
	}

	/**
	 * @return Client MQTT con connessione sicura al broker
	 */
	private static MqttClient getMqtts(String clientId, String mqttBroker, String username, String password, String keystoreFile, String keystorePassword) throws MqttException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		MqttClient mqttClient = new MqttClient(mqttBroker, clientId);

		MqttConnectOptions connectOpt = new MqttConnectOptions();
		connectOpt.setSocketFactory(getSocketFactory(keystoreFile, keystorePassword));
		connectOpt.setCleanSession(false);
		connectOpt.setAutomaticReconnect(true);
		connectOpt.setUserName(username);
		connectOpt.setPassword(password.toCharArray());

		mqttClient.connect(connectOpt);

		return mqttClient;
	}
}
