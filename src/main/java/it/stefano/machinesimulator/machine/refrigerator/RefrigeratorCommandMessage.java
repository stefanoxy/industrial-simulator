package it.stefano.machinesimulator.machine.refrigerator;

import it.stefano.machinesimulator.machine.MachineType;
import it.stefano.machinesimulator.mqtt.AbstractCommandMessage;
import lombok.Getter;
import lombok.ToString;

/**
 * Messaggio di comando per il Refrigerator, inviato dalla Control Room, con valori di temperatura e stato (acceso/spento) da impostare sul macchinario
 */
@Getter
@ToString(callSuper = true)
public class RefrigeratorCommandMessage extends AbstractCommandMessage
{
	private boolean	poweredOn;
	private double temperature;

	public RefrigeratorCommandMessage(MachineType machineType, String machineId,  boolean poweredOn, double temperature) {
		super(machineType, machineId);
		this.poweredOn = poweredOn;
		this.temperature = temperature;
	}
	
	// Costruttore vuoto necessario per deserializzare i messaggi provenienti da MQTT
	public RefrigeratorCommandMessage() {
		super(null, null);
	}
}
