package it.stefano.machinesimulator.machine;

import it.stefano.machinesimulator.mqtt.AbstractMiddlewareMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ErrorMessage implements AbstractMiddlewareMessage
{
	private final String		machineId;
	private final MachineType	machineType;
	private final MachineError	machineError;

	// generico valore associato all'errore
	// al momento double ma da generalizzare per potere accogliere altri tipi di valori,
	// compresi oggetti complessi
	private final double value;
}
