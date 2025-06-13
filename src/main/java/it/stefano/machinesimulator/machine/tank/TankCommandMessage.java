package it.stefano.machinesimulator.machine.tank;

import it.stefano.machinesimulator.machine.MachineType;
import it.stefano.machinesimulator.mqtt.AbstractCommandMessage;
import lombok.Getter;
import lombok.ToString;

/**
 * Messaggio di comando per il Tank, inviato dalla Control Room, con valore di livello del liquido contenuto da impostare sul macchinario
 */
@Getter
@ToString(callSuper = true)
public class TankCommandMessage extends AbstractCommandMessage
{
	private double level;

	public TankCommandMessage(MachineType machineType, String machineId, double level) {
		super(machineType, machineId);
		this.level = level;
	}
	
	// Costruttore vuoto necessario per deserializzare i messaggi provenienti da MQTT
	public TankCommandMessage() {
		super(null, null);
	}
}
