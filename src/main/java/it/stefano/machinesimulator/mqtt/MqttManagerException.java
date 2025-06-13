package it.stefano.machinesimulator.mqtt;

/**
 * Eccezione sollevata dal MqttManager
 */
public class MqttManagerException extends Exception
{
	private static final long serialVersionUID = 294953615662502285L;

	public MqttManagerException(String message, Throwable cause) {
		super(message, cause);
	}
}
