package it.stefano.machinesimulator.machine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lombok.extern.slf4j.Slf4j;
// TODO qui usate reflection
@Slf4j
public class MachineSimulator
{
	private AbstractMachine abstractMachine;

	public MachineSimulator(MachineType machineType, String id, String mqttBroker, String mqttUsername, String mqttPassword, String keystoreFile, String keystorePassword, String keyAlias, String keyPassword) {
		Class<? extends AbstractMachine> amclass = machineType.getMachineClass();
		log.info("Starting "+amclass.getSimpleName()+" ("+id+") connecting to "+mqttBroker);
		try {
			Constructor<? extends AbstractMachine> con = amclass.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class);
			abstractMachine = con.newInstance(id, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword, keyAlias, keyPassword);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			log.error("Cannot create instance of " + machineType, e);
		}
	}

	public void startMachine()
	{
		abstractMachine.start();
	}
}
