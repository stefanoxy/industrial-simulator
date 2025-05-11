package it.stefano.machinesimulator.machine.tank;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import it.stefano.machinesimulator.machine.AbstractMachine;
import it.stefano.machinesimulator.machine.MachineException;
import it.stefano.machinesimulator.mqtt.AbstractMiddlewareMessage;
import it.stefano.machinesimulator.mqtt.QOS;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tank extends AbstractMachine
{
	private static final long	MAX_SLEEP				= 5000;
	private static final double	DATAERROR_PERCENTAGE	= 0.5d;

	public static final double	MIN_LEVEL	= 0.0d;
	public static final double	MAX_LEVEL	= 30.0d;

	public Tank(String clientId, String mqttBroker, String mqttUsername, String mqttPassword, String keystoreFile, String keystorePassword, String keyAlias, String keyPassword) throws MachineException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		super(clientId, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword, keyAlias, keyPassword);
	}

	@Override
	public void run()
	{
		for (;;) {
			TankTelemetryMessage tt = null;
			try {
				// attendiamo un tempo casuale fra 0 e MAX_SLEEP
				Thread.sleep((long) (Math.random() * MAX_SLEEP));

				// calcoliamo un livello del serbatoio che nel DATAERROR_PERCENTAGE% dei casi è superiore al massimo
				double delta = Math.random() * (MAX_LEVEL - MIN_LEVEL);
				double level = Math.random() < 1.0d - DATAERROR_PERCENTAGE ? MIN_LEVEL + delta : MAX_LEVEL + delta;
				tt = new TankTelemetryMessage(this, level);
				sendTelemetry(tt);
			}
			catch (Exception e) {
				log.error("exception sending telemetry message " + tt + " to Control Room via MQTT", e);
			}
		}
	}

	@Override
	public void handleCommand(String topic, AbstractMiddlewareMessage message)
	{
		try {
			TankCommandMessage tcommand = (TankCommandMessage)message;
			log.info("Control Room set level=" + tcommand.getLevel() + " on machine " + tcommand.getMachineType() + " " + tcommand.getMachineId());
		}
		catch (Exception e) {
			log.error("Exception decoding message " + message + " from topic " + topic + ". Expected "+TankCommandMessage.class, e);
		}
	}
}
