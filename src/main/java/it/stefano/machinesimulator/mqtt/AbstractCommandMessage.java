package it.stefano.machinesimulator.mqtt;

import it.stefano.machinesimulator.machine.MachineType;
import lombok.Getter;

/**
 * Generico messaggio di comando inviato dalla Control Room ai macchinari
 */
@Getter
public class AbstractCommandMessage implements AbstractMiddlewareMessage
{
	private final MachineType	machineType;
	private final String		machineId;

	public AbstractCommandMessage(MachineType machineType, String machineId) {
		this.machineType = machineType;
		this.machineId = machineId;
	}

	// Preferito toString esplicito per gestire meglio la visualizzazione dei valori, piuttosto che ricorrere al @ToString di Lombok
	@Override
	public String toString()
	{
		return "[machineType=" + machineType + ", machineId=" + machineId + "]";
	}
	
	
}
