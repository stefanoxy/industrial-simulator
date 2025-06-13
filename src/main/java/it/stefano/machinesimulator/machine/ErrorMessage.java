package it.stefano.machinesimulator.machine;

import it.stefano.machinesimulator.mqtt.AbstractMiddlewareMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Messaggio di errore generato dalla Control Room per segnalare delle anomalie
 */
@AllArgsConstructor
@Getter
@ToString
public class ErrorMessage implements AbstractMiddlewareMessage
{
	private final String		machineId;
	private final MachineType	machineType;
	private final MachineError	machineError;

	/* 
	 * Generico valore associato all'errore. 
	 * Al momento è un double ma andrebbe generalizzato per potere accogliere altri tipi di valori, 
	 * compresi oggetti complessi
	*/
	private final double value;
}
