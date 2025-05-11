package it.stefano.machinesimulator.controlroom;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import it.stefano.machinesimulator.helper.CryptoHelper;
import it.stefano.machinesimulator.helper.JsonHelper;
import it.stefano.machinesimulator.machine.ErrorMessage;
import it.stefano.machinesimulator.machine.MachineError;
import it.stefano.machinesimulator.machine.MachineType;
import it.stefano.machinesimulator.machine.boiler.Boiler;
import it.stefano.machinesimulator.machine.boiler.BoilerCommandMessage;
import it.stefano.machinesimulator.machine.boiler.BoilerTelemetryMessage;
import it.stefano.machinesimulator.machine.refrigerator.Refrigerator;
import it.stefano.machinesimulator.machine.refrigerator.RefrigeratorCommandMessage;
import it.stefano.machinesimulator.machine.refrigerator.RefrigeratorTelemetryMessage;
import it.stefano.machinesimulator.machine.tank.Tank;
import it.stefano.machinesimulator.machine.tank.TankCommandMessage;
import it.stefano.machinesimulator.machine.tank.TankTelemetryMessage;
import it.stefano.machinesimulator.mqtt.AbstractCommandMessage;
import it.stefano.machinesimulator.mqtt.AbstractTelemetryMessage;
import it.stefano.machinesimulator.mqtt.MqttManager;
import it.stefano.machinesimulator.mqtt.QOS;
import it.stefano.machinesimulator.mqtt.SecureEnvelope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControlRoom implements IMqttMessageListener
{
	public static final String ERROR_TOPIC = "machine-errors";

	private final String		mqttClientId;
	private final KeyStore		keyStore;
	private final PrivateKey	privateKey;

	private final MqttManager mqttManager;

	public ControlRoom(String machineId, String mqttBroker, String mqttUsername, String mqttPassword, String keystoreFile, String keystorePassword, String keyAlias, String keyPassword) throws ControlRoomException {

		this.mqttClientId = this.getClass().getSimpleName() + "-" + machineId;

		try {
			mqttManager = new MqttManager(mqttClientId, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword);
			String subscriptionTopic = MachineType.OUTPUT_ROOT_TOPIC + "/#";
			log.info(mqttClientId + ": subscribing topics " + subscriptionTopic);
			mqttManager.subscribe(subscriptionTopic, this);

			this.keyStore = CryptoHelper.loadKeyStore(keystoreFile, keystorePassword);
			this.privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
		}
		catch (Exception e) {
			throw new ControlRoomException("Exception starting Control Room", e);
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage message)
	{
		log.debug(mqttClientId + ": MQTT message - TOPIC: " + topic + " MSG: " + message);
		try {
			MachineType mt = MachineType.getTypeByTopic(topic);
			if (mt == null) {
				log.error("Unknown machine type for topic " + topic);
				return;
			}
			// expecting an envelope
			SecureEnvelope envelope = JsonHelper.jsonToObject(new String(message.getPayload(), StandardCharsets.UTF_8), SecureEnvelope.class);
			// preleviamo dal keystore la public key del mittente con cui verificheremo l'integrità del messaggio
			Certificate cert = keyStore.getCertificate(mt.getCertificateAlias());
			AbstractTelemetryMessage telemetry = (AbstractTelemetryMessage) envelope.decodeMessage(cert.getPublicKey());
			handleTelemetry(mt, telemetry);
		}
		catch (Exception e) {
			log.error("Exception decoding message " + message + " from topic " + topic, e);
		}
	}

	private void handleTelemetry(MachineType type, AbstractTelemetryMessage telemetry) throws ControlRoomException
	{
		log.info("Analysing telemetry: " + telemetry);
		switch (type) {
			case CONTROL_ROOM:
				// no telemetry from Control Room
				// inserted only to avoid warning on switch/case not completed
				break;
			case BOILER:
				BoilerTelemetryMessage bt = (BoilerTelemetryMessage) telemetry;
				double btemperature = bt.getTemperature();
				double bpressure = bt.getPressure();

				if (!isValidRange(type, btemperature, "temperature", Boiler.MIN_TEMPERATURE, Boiler.MAX_TEMPERATURE)) {
					sendError(bt.getMachineId(), type, MachineError.BOILER_TEMPERATURE, btemperature);
					btemperature = (Boiler.MIN_TEMPERATURE + Boiler.MAX_TEMPERATURE) / 2.0d;
					log.warn("Boiler temperature out of range. Setting temperature=" + btemperature + " and pressure=" + bpressure + " on " + type + " " + telemetry.getMachineId());
					BoilerCommandMessage bc = new BoilerCommandMessage(type, bt.getMachineId(), true, btemperature, bpressure);
					sendCommand(bc, type, telemetry.getMachineId());
				}
				if (!isValidRange(type, bpressure, "pressure", Boiler.MIN_PRESSURE, Boiler.MAX_PRESSURE)) {

					sendError(bt.getMachineId(), type, MachineError.BOILER_PRESSURE, bpressure);
					bpressure = Boiler.MIN_PRESSURE;
					log.warn("Boiler pressure out of range. Setting temperature=" + btemperature + " and pressure=" + bpressure + " on " + type + " " + telemetry.getMachineId());
					BoilerCommandMessage bc = new BoilerCommandMessage(type, bt.getMachineId(), true, btemperature, bpressure);
					sendCommand(bc, type, telemetry.getMachineId());
				}
				break;
			case REFRIGERATOR:
				RefrigeratorTelemetryMessage rt = (RefrigeratorTelemetryMessage) telemetry;
				double rtemperature = rt.getTemperature();
				if (!isValidRange(type, rtemperature, "temperature", Refrigerator.MIN_TEMPERATURE, Refrigerator.MAX_TEMPERATURE)) {

					sendError(rt.getMachineId(), type, MachineError.REFRIGERATOR_TEMPERATURE, rtemperature);
					rtemperature = (Refrigerator.MIN_TEMPERATURE + Refrigerator.MAX_TEMPERATURE) / 2;
					log.warn("Refrigerator temperature out of range. Setting temperature=" + rtemperature + " on " + type + " " + telemetry.getMachineId());
					RefrigeratorCommandMessage rc = new RefrigeratorCommandMessage(type, rt.getMachineId(), true, rtemperature);
					// mandiamo comando con QoS 1, quindi accettiamo duplicati
					sendCommand(rc, type, telemetry.getMachineId());
				}
				break;
			case TANK:
				TankTelemetryMessage tt = (TankTelemetryMessage) telemetry;
				double tlevel = tt.getLevel();
				if (!isValidRange(type, tlevel, "level", Tank.MIN_LEVEL, Tank.MAX_LEVEL)) {
					sendError(tt.getMachineId(), type, MachineError.TANK_LEVEL, tlevel);
					tlevel = (Tank.MIN_LEVEL + Tank.MAX_LEVEL) / 2.0d;
					log.warn("Tank level out of range. Setting level=" + tlevel + " on " + type + " " + telemetry.getMachineId());
					TankCommandMessage tc = new TankCommandMessage(type, tt.getMachineId(), tlevel);
					// mandiamo comando con QoS 1, quindi accettiamo duplicati
					sendCommand(tc, type, telemetry.getMachineId());
				}
				break;
			default:
				// TODO commentare questo default
				// useful log to discover unmanaged cases of new machine types
				log.error("Unimplemented telemetry case '" + type + "'. Please check MachineType");
				break;
		}
	}

	// semplificazione: solo valori double
	private boolean isValidRange(MachineType machineType, double value, String typeOfValue, double min, double max)
	{
		if ((value < min) || (value > max)) {
			log.warn(machineType + " " + typeOfValue + " " + value + " is out of range (" + min + "," + max + ")");
			return false;
		}
		else {
			log.debug(machineType + " " + typeOfValue + " " + value + " is in range (" + min + "," + max + ")");
			return true;
		}
	}

	private void sendError(String machineId, MachineType machineType, MachineError machineError, double value) throws ControlRoomException
	{
		ErrorMessage error = null;

		try {
			error = new ErrorMessage(machineId, machineType, machineError, value);
			log.info("Sending error message " + error + " to topic " + ERROR_TOPIC);
			SecureEnvelope envelope = new SecureEnvelope(error, privateKey);
			// mandiamo gli alert con QoS 2, quindi esattamente una volta: vogliamo essere certi di non creare duplicati per non creare falsi allarmi
			mqttManager.sendMessage(envelope, ERROR_TOPIC, QOS.EXACTLY_ONCE, false, false);
		}
		catch (Exception e) {
			throw new ControlRoomException("Exception sending error " + error + " to topic " + ERROR_TOPIC, e);
		}
	}

	private void sendCommand(AbstractCommandMessage message, MachineType machineType, String machineId) throws ControlRoomException
	{
		// il topic su cui scrive la ControlRoom è, dal punto di vista dei macchinari, il loro input topic
		String outputTopic = MachineType.getInputTopicByMachine(machineType.getMachineClass(), machineId);

		try {
			log.info("Sending message " + message + " to topic " + outputTopic);
			SecureEnvelope envelope = new SecureEnvelope(message, privateKey);
			// mandiamo comandi con QoS 1, quindi accettiamo duplicati: consideriamo accettabile mandare eventualmente più di una volta lo stesso comando
			mqttManager.sendMessage(envelope, outputTopic, QOS.AT_LEAST_ONCE, false, false);
		}
		catch (Exception e) {
			throw new ControlRoomException("Exception sending msg " + message + " to MQTT topic " + outputTopic, e);
		}
	}
}
