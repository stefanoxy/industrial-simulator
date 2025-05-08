package it.stefano.machinesimulator.machine.boiler;

import it.stefano.machinesimulator.machine.MachineType;
import it.stefano.machinesimulator.mqtt.AbstractCommandMessage;
import lombok.Getter;
import lombok.ToString;

// TODO commentare uso Lombok
@Getter
@ToString(callSuper = true)
public class BoilerCommandMessage extends AbstractCommandMessage
{
	private boolean	poweredOn;
	private double	temperature;
	private double	pressure;

	public BoilerCommandMessage(MachineType machineType, String machineId, boolean poweredOn, double temperature, double pressure) {
		super(machineType, machineId);
		this.poweredOn = poweredOn;
		this.temperature = temperature;
		this.pressure = pressure;
	}

	// Costruttore vuoto necessario per deserializzare i messaggi provenienti da MQTT
	public BoilerCommandMessage() {
		super(null, null);
	}
}
