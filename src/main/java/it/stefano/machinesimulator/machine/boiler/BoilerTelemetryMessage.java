package it.stefano.machinesimulator.machine.boiler;

import it.stefano.machinesimulator.mqtt.AbstractTelemetryMessage;
import lombok.Getter;
import lombok.ToString;


/**
 * Messaggio di telemetria del Boiler con due valori, temperatura e pressione
 */
@Getter
@ToString(callSuper = true)
public class BoilerTelemetryMessage extends AbstractTelemetryMessage
{
	private double temperature;
	private double pressure;
	
	public BoilerTelemetryMessage(Boiler boiler, double temperature, double pressure) {
		super(boiler);
		this.temperature = temperature;
		this.pressure = pressure;
	}

	// Costruttore vuoto necessario per deserializzare i messaggi provenienti da MQTT
	public BoilerTelemetryMessage() {
		super(null);
	}
}
