package it.stefano.machinesimulator.machine.refrigerator;

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
public class Refrigerator extends AbstractMachine
{
	private static final long	MAX_SLEEP				= 5000;
	private static final double	DATAERROR_PERCENTAGE	= 0.5d;

	public static final double	MIN_TEMPERATURE	= 2.0d;
	public static final double	MAX_TEMPERATURE	= 4.0d;

	public Refrigerator(String clientId, String mqttBroker, String mqttUsername, String mqttPassword, String keystoreFile, String keystorePassword, String keyAlias, String keyPassword) throws MachineException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		super(clientId, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword, keyAlias, keyPassword);
	}

	@Override
	public void run()
	{
		RefrigeratorTelemetryMessage rt = null;

		for (;;) {
			try {
				// attendiamo un tempo casuale fra 0 e MAX_SLEEP
				Thread.sleep((long) (Math.random() * MAX_SLEEP));

				// calcoliamo una temperatura casuale che nel DATAERROR_PERCENTAGE% dei casi è superiore al massimo
				double delta = Math.random() * (MAX_TEMPERATURE - MIN_TEMPERATURE);
				double temperature = Math.random() < 1.0d - DATAERROR_PERCENTAGE ? MIN_TEMPERATURE + delta : MAX_TEMPERATURE + delta;
				rt = new RefrigeratorTelemetryMessage(this, temperature);
				sendTelemetry(rt);
			}
			catch (Exception e) {
				log.error("exception sending telemetry message " + rt + " to Control Room via MQTT", e);
			}
		}
	}

	@Override
	public void handleCommand(String topic, AbstractMiddlewareMessage message)
	{
		try {
			RefrigeratorCommandMessage rcommand = (RefrigeratorCommandMessage)message;
			log.info("Control Room set temperature=" + rcommand.getTemperature() + " and powerOn=" + rcommand.isPoweredOn() + " on machine " + rcommand.getMachineType() + " " + rcommand.getMachineId());
		}
		catch (Exception e) {
			log.error("Exception decoding message " + message + " from topic " + topic + ". Expected "+RefrigeratorCommandMessage.class, e);
		}
	}
}
