package it.stefano.machinesimulator.machine.boiler;

import it.stefano.machinesimulator.machine.MachineType;
import it.stefano.machinesimulator.mqtt.AbstractCommandMessage;
import lombok.Getter;
import lombok.ToString;

/**
 * Messaggio di comando per il Boiler, inviato dalla Control Room, con valori di temperatura, pressione e stato (acceso/spento) da impostare sul macchinario
 */
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
