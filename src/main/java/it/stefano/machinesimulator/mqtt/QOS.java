package it.stefano.machinesimulator.mqtt;

public enum QOS
{
	AT_MOST_ONCE(0),
	AT_LEAST_ONCE(1),
	EXACTLY_ONCE(2);

	private int qos;

	QOS(int qos) {
		this.qos = qos;
	}

	public int getQOS()
	{
		return qos;
	}
}
