package it.stefano.machinesimulator.machine.boiler;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import it.stefano.machinesimulator.machine.AbstractMachine;
import it.stefano.machinesimulator.machine.MachineException;
import it.stefano.machinesimulator.mqtt.AbstractMiddlewareMessage;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementazione del simulatore di un Boiler 
 */
@Slf4j
public class Boiler extends AbstractMachine
{
	private static final long	MAX_SLEEP				= 5000;
	private static final double	DATAERROR_PERCENTAGE	= 0.5d;

	public static final double	MIN_TEMPERATURE	= 90.0d;
	public static final double	MAX_TEMPERATURE	= 150.0d;

	public static final double	MIN_PRESSURE	= 5.0d;
	public static final double	MAX_PRESSURE	= 7.0d;

	public Boiler(String clientId, String mqttBroker, String mqttUsername, String mqttPassword, String keystoreFile, String keystorePassword, String keyAlias, String keyPassword)
			throws MachineException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		super(clientId, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword, keyAlias, keyPassword);
	}

	/**
	 * Il macchinario, a intervalli casuali, invia un messaggio di telemetria con valori fuori soglia nel DATAERROR_PERCENTAGE% dei casi
	 * per attivare la reazione della Control Room
	 */
	@Override
	public void run()
	{
		for (;;) {
			BoilerTelemetryMessage bt = null;
			try {
				// attendiamo un tempo casuale fra 0 e MAX_SLEEP
				Thread.sleep((long) (Math.random() * MAX_SLEEP));

				// calcoliamo una temperatura casuale che nel DATAERROR_PERCENTAGE% dei casi è superiore al massimo
				double delta = Math.random() * (MAX_TEMPERATURE - MIN_TEMPERATURE);
				double temperature = Math.random() < 1.0d - DATAERROR_PERCENTAGE ? MIN_TEMPERATURE + delta : MAX_TEMPERATURE + delta;

				// calcoliamo una pressione casuale che nel DATAERROR_PERCENTAGE% dei casi è inferiore al minimo
				delta = Math.random() * (MAX_PRESSURE - MIN_PRESSURE);
				double pressure = Math.random() < 1.0d - DATAERROR_PERCENTAGE ? MIN_PRESSURE + delta : MIN_PRESSURE - delta;

				bt = new BoilerTelemetryMessage(this, temperature, pressure);
				sendTelemetry(bt);
			}
			catch (Exception e) {
				log.error("Exception sending telemetry message " + bt + " to Control Room via MQTT", e);
			}
		}
	}
	
	/**
	 * Gestisce i comandi arrivati dalla Control Room.
	 * Nel concreto, viene solo inserita una riga nel log.
	 */
	@Override
	public void handleCommand(String topic, AbstractMiddlewareMessage message)
	{
		try {
			BoilerCommandMessage bcommand = (BoilerCommandMessage) message;
			log.info("Control Room set temperature=" + bcommand.getTemperature() + " and pressure=" + bcommand.getPressure() + " on machine " + bcommand.getMachineType() + " " + bcommand.getMachineId());
		}
		catch (Exception e) {
			log.error("Exception decoding message " + message + " from topic " + topic + ". Expected " + BoilerCommandMessage.class, e);
		}
	}
}
