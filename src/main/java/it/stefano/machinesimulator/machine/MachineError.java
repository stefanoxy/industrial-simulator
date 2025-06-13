package it.stefano.machinesimulator.machine;

/**
 * Elenco dei messaggi di errore che possono essere inviati da un generico AbstractMachine
 * alla Control Room
 */
public enum MachineError
{
	BOILER_TEMPERATURE,
	BOILER_PRESSURE,

	REFRIGERATOR_TEMPERATURE,
	
	TANK_LEVEL;
}
