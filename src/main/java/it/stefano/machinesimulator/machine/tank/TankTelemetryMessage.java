package it.stefano.machinesimulator.machine.tank;

import it.stefano.machinesimulator.mqtt.AbstractTelemetryMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class TankTelemetryMessage extends AbstractTelemetryMessage
{
	private double level;

	public TankTelemetryMessage(Tank tank, double level) {
		super(tank);
		this.level = level;
	}
	
	// Costruttore vuoto necessario per deserializzare i messaggi provenienti da MQTT
	public TankTelemetryMessage() {
		super(null);
	}
}
