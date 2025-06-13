package it.stefano.machinesimulator.mqtt;

/**
 * Elenco dei Quality of Service previsti dal protocollo MQTT 
 */
public enum QOS
{
	AT_MOST_ONCE(0),
	AT_LEAST_ONCE(1),
	EXACTLY_ONCE(2);

	private int value;

	QOS(int value) {
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}
