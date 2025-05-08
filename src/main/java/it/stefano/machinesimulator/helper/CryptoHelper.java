package it.stefano.machinesimulator.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoHelper
{
	private CryptoHelper() {
	}

	public static KeyStore loadKeyStore(String keystoreFile, String keystorePassword) throws FileNotFoundException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		KeyStore keyStore;

		try (FileInputStream fis = new FileInputStream(keystoreFile);) {
			keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(fis, keystorePassword.toCharArray());
		}

		return keyStore;
	}

	public static byte[] encrypt(byte[] data, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	public static byte[] decrypt(byte[] encryptedData, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(encryptedData);
	}

	public static byte[] generateHash(byte[] input) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(input);
	}

	public static String toBase64(byte[] input) throws NoSuchAlgorithmException
	{
		return Base64.getEncoder().encodeToString(input);
	}
	
	public static boolean hashEquals(byte[] hash1, byte[] hash2)
	{
		return Arrays.equals(hash1, hash2);
	}

	public static boolean hashEquals(String hash1, String hash2)
	{
		return hash1.compareTo(hash2) == 0;
	}
}
