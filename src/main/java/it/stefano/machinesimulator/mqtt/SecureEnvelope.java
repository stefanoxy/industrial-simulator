package it.stefano.machinesimulator.mqtt;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import it.stefano.machinesimulator.helper.CryptoHelper;
import it.stefano.machinesimulator.helper.JsonHelper;
import lombok.Getter;
import lombok.ToString;

/**
 * Busta sicura per i messaggi con meccanismo trasparente per l'utilizzatore di controllo dell'integrità dl contenuto
 */
@Getter
@ToString
public class SecureEnvelope
{
	private final String										messageJson;
	private final Class<? extends AbstractMiddlewareMessage>	messageClass;
	private final byte[]										signedHash;

	// Costruttore vuoto necessario per deserializzazione JSON
	public SecureEnvelope()  {
		messageJson = null;
		messageClass = null;
		signedHash = null;
	}

	/**
	 * Incapsula un generico AbstractMiddlewareMessage in una secure envelope firmata per garantire integrità dei dati trasferiti
	 * 
	 * @param mqttMessage messaggio da trasferire
	 * @param encryptionKey chiave privata del mittente con cui viene firmato l'hash del messaggio
	 */
	public SecureEnvelope(AbstractMiddlewareMessage mqttMessage, Key encryptionKey) throws SecureEnvelopeException {
		try {
			this.messageClass = mqttMessage.getClass();
			// messaggio trasformato in JSON
			this.messageJson = JsonHelper.objectToJson(mqttMessage, false);
			// hash del messaggio
			byte[] messageHash = CryptoHelper.generateHash(messageJson.getBytes(StandardCharsets.UTF_8));
			// hash del messaggio firmato con chiave privata del mittente
			this.signedHash = CryptoHelper.encrypt(messageHash, encryptionKey);
		}
		catch (Exception e) {
			throw new SecureEnvelopeException("Exception encoding message");
		}
	}

	/**
	 * Estrae il messaggio contenuto nella secure envelope verificando che la firma corrisponda
	 */
	public AbstractMiddlewareMessage decodeMessage(Key decryptionKey) throws SecureEnvelopeException
	{
		try {
			// messaggio estratto dalla secure envelope
			AbstractMiddlewareMessage amm = JsonHelper.jsonToObject(messageJson, messageClass);
			// hash del messaggio appena estratto
			byte[] expectedHash = CryptoHelper.generateHash(messageJson.getBytes(StandardCharsets.UTF_8));

			// hash decrittato con chiave pubblica del mittente
			byte[] hash = CryptoHelper.decrypt(signedHash, decryptionKey);
			// verifica di corrispondenza degli hash per garantire integrità del contenuto delle secure envelope
			if (!CryptoHelper.hashEquals(expectedHash, hash)) throw new SecureEnvelopeException("Envelope has been tampered, hashes don't match");
			
			return amm;
		}
		catch(SecureEnvelopeException e) {
			throw e;
		}
		catch(Exception e)
		{
			throw new SecureEnvelopeException("Exception decoding message");
		}
	}
}
