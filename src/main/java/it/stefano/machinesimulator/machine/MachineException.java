package it.stefano.machinesimulator.machine;

/**
 * Generica eccezione sollevata dal un AbstractMachine
 */
public class MachineException extends Exception
{
	private static final long serialVersionUID = 2614145728990251578L;

	public MachineException(String message, Throwable cause) {
		super(message, cause);
	}
}
