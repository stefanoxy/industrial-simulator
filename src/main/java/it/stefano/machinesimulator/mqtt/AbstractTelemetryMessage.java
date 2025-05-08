package it.stefano.machinesimulator.mqtt;

import it.stefano.machinesimulator.machine.AbstractMachine;
import it.stefano.machinesimulator.machine.MachineType;
import lombok.Getter;

@Getter
public class AbstractTelemetryMessage implements AbstractMiddlewareMessage
{
	private final MachineType	machineType;
	private final String		machineId;

	public AbstractTelemetryMessage(AbstractMachine machine) {
		this.machineType = MachineType.getTypeByMachine(machine);
		this.machineId = machine != null ? machine.getMachineId() : null;
	}

	// Preferito toString esplicito per gestire meglio la visualizzazione dei valori, piuttosto che ricorrere al @ToString di Lombok
	@Override
	public String toString()
	{
		return "[machineType=" + machineType + ", machineId=" + machineId + "]";
	}
}
