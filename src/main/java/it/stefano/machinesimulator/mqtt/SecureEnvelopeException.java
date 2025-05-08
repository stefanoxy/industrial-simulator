package it.stefano.machinesimulator.mqtt;

public class SecureEnvelopeException extends Exception
{
	private static final long serialVersionUID = 8872136742079903565L;

	public SecureEnvelopeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecureEnvelopeException(String message) {
		super(message);
	}
}
