package it.stefano.machinesimulator.machine.refrigerator;

import it.stefano.machinesimulator.mqtt.AbstractTelemetryMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class RefrigeratorTelemetryMessage extends AbstractTelemetryMessage
{
	private double temperature;

	public RefrigeratorTelemetryMessage(Refrigerator refrigerator, double temperature) {
		super(refrigerator);
		this.temperature = temperature;
	}
	
	// Costruttore vuoto necessario per deserializzare i messaggi provenienti da MQTT
	public RefrigeratorTelemetryMessage() {
		super(null);
	}
}
