package it.stefano.machinesimulator.mqtt;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import it.stefano.machinesimulator.helper.CryptoHelper;
import it.stefano.machinesimulator.helper.JsonHelper;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SecureEnvelope
{
	private final String										messageJson;
	private final Class<? extends AbstractMiddlewareMessage>	messageClass;
	//TODO eliminare!
	private final byte[]										messageHash;
	private final byte[]										signedHash;

	// Necessario per deserializzazione messaggi JSON
	public SecureEnvelope()  {
		messageJson = null;
		messageClass = null;
		messageHash = null;
		signedHash = null;
	}

	public SecureEnvelope(AbstractMiddlewareMessage mqttMessage, Key entryptionKey) throws SecureEnvelopeException {
		try {
			this.messageClass = mqttMessage.getClass();
			this.messageJson = JsonHelper.objectToJson(mqttMessage, false);
			this.messageHash = CryptoHelper.generateHash(messageJson.getBytes(StandardCharsets.UTF_8));
			this.signedHash = CryptoHelper.encrypt(messageHash, entryptionKey);
		}
		catch (Exception e) {
			throw new SecureEnvelopeException("Exception encoding message");
		}
	}

	public AbstractMiddlewareMessage decodeMessage(Key decryptionKey) throws SecureEnvelopeException
	{
		try {
			AbstractMiddlewareMessage amm = JsonHelper.jsonToObject(messageJson, messageClass);
			byte[] expectedHash = CryptoHelper.generateHash(messageJson.getBytes(StandardCharsets.UTF_8));

			byte[] hash = CryptoHelper.decrypt(signedHash, decryptionKey);
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
