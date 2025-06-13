package it.stefano.machinesimulator;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import it.stefano.machinesimulator.controlroom.ControlRoom;
import it.stefano.machinesimulator.controlroom.ControlRoomException;
import it.stefano.machinesimulator.machine.MachineSimulator;
import it.stefano.machinesimulator.machine.MachineType;
import lombok.extern.slf4j.Slf4j;


/**
 * Il simulatore è in grado di istanziare 3 tipi di macchinari diversi (Refrigerator, Boiler, Tank) e la Control Room.
 * La Control Room monitora i messaggi provenienti dalle macchine e, in caso di valori ritenuti fuori soglia, invia comandi
 * alle macchine per riconfigurarle e segnala gli errori su un apposito topic.
 * 
 * Il simulatore prevede 9 parametri in input:
 * 
 * <type> tipo di macchina da istanziare (0-Control Room, 1-Refrigerator, 2-Boiler, 3-Tank)
 * <machine-id> è un id univoco che identifica la macchina
 * <mqtt-broker> indirizzo del broker MQTT (con protocollo tcp: o ssl:)
 * <mqtt-username> username per accesso a broker MQTT
 * <mqtt-password> password per accesso a broker MQTT
 * <keystore-file> file del keystore che contiene certificati e chiavi private
 * <keystore-password> password per l'accesso al keystore
 * <key-alias> alias del certificato per la macchina istanziata
 * <key-password> password per l'accesso alla chiave privata identificata da key-alias
 * 
 */

@Slf4j
public class Main
{
	public static void main(String[] args) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		// Controllo sui parametri ottenuti dalla riga di comando
		if (args.length != 9) {
			log.error("Provided only " + args.length + " parameters (" + Arrays.toString(args) + "), but 9 are required");
			syntax();
			return;
		}

		// estrazione del tipo di macchinario da istanziare
		int type;
		try {
			type = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e) {
			log.error("Invalid type '" + args[0] + "'");
			syntax();
			return;
		}
		
		// estrazione degli altri parametri
		String id = args[1];
		String mqttBroker = args[2];
		String mqttUsername = args[3];
		String mqttPassword = args[4];
		String keystoreFile = args[5];
		String keystorePassword = args[6];
		String keyAlias	= args[7];
		String keyPassword = args[8];

		// identificazione del tipo di macchinario da istanziare
		MachineType mt = null;
		try {
			mt = MachineType.getTypeById(type);
		}
		catch (Exception e) {
			log.error("Unknow machine type " + type);
			syntax();
			return;
		}

		if (mt == null) {
			log.error("Unknow machine type " + type);
			syntax();
			return;
		}

		/*
		 * Avvio del macchinario o della Control Room
		 * 
		 * Essendo un prototipo, nell'implementazione ci si è concessi che l'applicazione termini solo per interruzione dall'esterno.
		 * Ovviamente ciò è accettabile solo in quanto appunto si tratta di un prototipo.
		 * 
		 * Nello specifico, nella classe ControlRoom il metodo che istanzia la connessione verso il broker MQTT lancia internamente dei thread 
		 * che impediscono al processo dell'applicazione di terminare, nonostante il main() si concluda.
		 * 
		 * Nel caso del MachineSimulator, oltre ai thread che gestiscono la connessione verso il broker MQTT, viene anche espressamente lanciato un
		 * thread applicativo nel metodo startMachine() (che invoca start() sulla classe AbstractMachine, che estende Thread)
		 * 
		 * In uno scenario di esercizio, sia i macchinari che la Control Room dovrebbero prevedere un metodo stop() con un'uscita ordinata
		 * e pulita che chiuda le connessioni al broker MQTT e esca dal thread applicativo
		 */
		if (mt == MachineType.CONTROL_ROOM) {
			try {
				ControlRoom cr = new ControlRoom(id, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword, keyAlias, keyPassword);
			}
			catch (ControlRoomException e) {
				log.error("Cannot create instance of ControlRoom", e);
			}
		}
		else {
			MachineSimulator ms = new MachineSimulator(mt, id, mqttBroker, mqttUsername, mqttPassword, keystoreFile, keystorePassword, keyAlias, keyPassword);
			ms.startMachine();
		}
	}

	/**
	 * Informazioni sui parametri attesi sulla riga di comando dell'applicazione
	 */
	private static final void syntax()
	{
		log.info("SYNTAX: MachineSimulator <type> <machine-id> <mqtt-broker> <mqtt-username> <mqtt-password> <keystore-file> <keystore-password> <key-alias> <key-password>");
		log.info("<type>:");
		for (MachineType mt : MachineType.values()) {
			log.error("\t" + mt.getType() + " is " + mt.name());
		}
		log.info("<machine-id> unique machine id");
		log.info("<mqtt-broker> address of MQTT broker");
		log.info("<mqtt-username> machine username to access MQTT broker");
		log.info("<mqtt-password> machine password to access MQTT broker");
		log.info("<keystore-file> keystore file path");
		log.info("<keystore-password> keystore password");
		log.info("<key-alias> certificate/private key alias for this machine");
		log.info("<key-password> private key password for this machine");
	}
}
