package it.stefano.machinesimulator.machine;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import it.stefano.machinesimulator.helper.CryptoHelper;
import it.stefano.machinesimulator.helper.JsonHelper;
import it.stefano.machinesimulator.mqtt.AbstractMiddlewareMessage;
import it.stefano.machinesimulator.mqtt.AbstractTelemetryMessage;
import it.stefano.machinesimulator.mqtt.MqttManager;
import it.stefano.machinesimulator.mqtt.QOS;
import it.stefano.machinesimulator.mqtt.SecureEnvelope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMachine extends Thread implements IMqttMessageListener
{
	private final String	machineId;
	private final String	mqttClientId;
	private final String	outputTopic;
	private final String	subscriptionTopic;

	private final MqttManager mqttManager;

	private final KeyStore		keyStore;
	private final PrivateKey	privateKey;

	protected AbstractMachine(String machineId, String mqttBroker, String mqttUsername, String mqttPassword, String keystoreFile, String keystorePassword, String keyAlias, String keyPassword) throws MachineException {
		log.info("Starting " + this.getClass().getSimpleName() + " with id=" + machineId);
		this.machineId = machineId;
		outputTopic = MachineType.getOutputTopicByMachine(this);
		subscriptionTopic = MachineType.getInputTopicByMachine(this);
		mqttClientId = this.getClass().getSimpleName() + "-" + machineId;

		try {
			mqttManager = new MqttManager(mqttClientId, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword);
			mqttManager.subscribe(subscriptionTopic, this);
			log.info(mqttClientId + ": subscribed topic " + subscriptionTopic);

			this.keyStore = CryptoHelper.loadKeyStore(keystoreFile, keystorePassword);
			this.privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
		}
		catch (Exception e) {
			throw new MachineException("Exception starting machine " + machineId, e);
		}

	}

	public String getMachineId()
	{
		return machineId;
	}

	public abstract void handleCommand(String topic, AbstractMiddlewareMessage message);

	@Override
	public void messageArrived(String topic, MqttMessage message)
	{
		try {
			log.info(mqttClientId + ": received message " + message + " on topic " + topic);

			// ci attendiamo una secure envelope
			SecureEnvelope envelope = JsonHelper.jsonToObject(new String(message.getPayload(), StandardCharsets.UTF_8), SecureEnvelope.class);
			// ci attendiamo che i comandi arrivino solo dalla Control Room
			// quindi proviamo a decodificare il messaggio dalla envelope usando il certificato
			// della Control Room per verificare la firma dell'hash
			handleCommand(topic, envelope.decodeMessage(keyStore.getCertificate(MachineType.CONTROL_ROOM.getCertificateAlias()).getPublicKey()));
		}
		catch (Exception e) {
			log.error("Exception receiving message on topic " + topic, e);
		}
	}

	protected void sendTelemetry(AbstractTelemetryMessage telemetryMessage) throws MachineException
	{
		try {
			log.info(mqttClientId + ": sending message " + telemetryMessage + " to topic " + outputTopic);

			SecureEnvelope envelope = new SecureEnvelope(telemetryMessage, privateKey);
			// mandiamo dati di telemetria con QoS 1, quindi accettiamo duplicati
			mqttManager.sendMessage(envelope, outputTopic, QOS.AT_LEAST_ONCE, false, false);
		}
		catch (Exception e) {
			throw new MachineException(mqttClientId + ": exception sending msg " + telemetryMessage + " to MQTT Broker", e);
		}
	}

	@Override
	public abstract void run();
}
