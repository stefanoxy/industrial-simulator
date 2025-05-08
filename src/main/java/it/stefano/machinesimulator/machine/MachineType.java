package it.stefano.machinesimulator.machine;

import it.stefano.machinesimulator.machine.boiler.Boiler;
import it.stefano.machinesimulator.machine.refrigerator.Refrigerator;
import it.stefano.machinesimulator.machine.tank.Tank;
import lombok.Getter;

@Getter
public enum MachineType
{
	CONTROL_ROOM(0, null, "controlroom"),
	BOILER(1, Boiler.class, "boiler"),
	REFRIGERATOR(2, Refrigerator.class, "refrigerator"),
	TANK(3, Tank.class, "tank");

	private final int								type;
	private final Class<? extends AbstractMachine>	machineClass;
	private final String							certificateAlias;

	private String	inputRootTopic;
	private String	outputRootTopic;

	public static final String	OUTPUT_ROOT_TOPIC	= "telemetry";
	public static final String	INPUT_ROOT_TOPIC	= "command";

	private MachineType(int type, Class<? extends AbstractMachine> machineClass, String certificateAlias) {
		this.type = type;
		this.machineClass = machineClass;
		this.certificateAlias = certificateAlias;

		if (machineClass != null) {
			this.inputRootTopic = INPUT_ROOT_TOPIC + "/" + machineClass.getSimpleName();
			this.outputRootTopic = OUTPUT_ROOT_TOPIC + "/" + machineClass.getSimpleName();
		}
	}

	public static final MachineType getTypeById(int id)
	{
		for (MachineType mt : MachineType.values()) {
			if (id == mt.getType()) return mt;
		}

		return null;
	}

	public static final MachineType getTypeByMachine(AbstractMachine machine)
	{
		if (machine == null) return null;

		for (MachineType mt : MachineType.values()) {
			if (machine.getClass() == mt.getMachineClass()) return mt;
		}

		return null;
	}

	public static MachineType getTypeByTopic(String topic)
	{
		if (topic == null) return null;

		for (MachineType mt : MachineType.values()) {
			String ort = mt.getOutputRootTopic();
			if (ort == null) continue;
			if (topic.startsWith(ort)) return mt;
		}

		return null;
	}

	public static final String getOutputTopicByMachine(AbstractMachine machine)
	{
		if (machine == null) return null;

		String topic = "no-output-topic-for-" + machine.getClass().getSimpleName();
		Class<? extends AbstractMachine> machineClass = machine.getClass();

		for (MachineType mt : MachineType.values()) {
			if (machineClass == mt.getMachineClass()) topic = mt.getOutputRootTopic() + "/" + machine.getMachineId();
		}

		return topic;
	}

	public static final String getInputTopicByMachine(AbstractMachine machine)
	{
		String topic = "no-subscription-topic-for-" + machine.getClass().getSimpleName();
		Class<? extends AbstractMachine> machineClass = machine.getClass();

		for (MachineType mt : MachineType.values()) {
			if (machineClass == mt.getMachineClass()) topic = mt.getInputRootTopic() + "/" + machine.getMachineId();
		}

		return topic;
	}

	public static final String getInputTopicByMachine(Class<? extends AbstractMachine> machineClass, String machineId)
	{
		String topic = "no-subscription-topic-for-" + machineClass.getSimpleName();

		for (MachineType mt : MachineType.values()) {
			if (machineClass == mt.getMachineClass()) topic = mt.getInputRootTopic() + "/" + machineId;
		}

		return topic;
	}
}
