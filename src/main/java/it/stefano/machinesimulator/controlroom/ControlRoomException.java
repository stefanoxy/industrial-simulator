package it.stefano.machinesimulator.controlroom;

/**
 * Eccezione sollevata dalla Control Room 
 */
public class ControlRoomException extends Exception
{
	private static final long serialVersionUID = 2614145728990251578L;

	public ControlRoomException(String message, Throwable cause) {
		super(message, cause);
	}
}
