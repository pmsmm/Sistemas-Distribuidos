package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;


//function that help creating security in soap messages, cifhering, decophering, 
//digital singature and helper methods to get private keys and certificates
public class CryptoUtil {

	/**Algoritmo de cifra/Algoritmo de processamento de blocos/Algoritmo de padding*/
	private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding"; 
	// alghoritm used in digital signature
    private static final String SIGNATURE_ALGO = "SHA256withRSA";

  
	//saves the nonces 
    private static ArrayList<String> nonces = new ArrayList<String>();

	/** print some error messages to standard error. */
	public static boolean outputFlag = true;


	//cipher bytes with public key
    public static byte[] asymCipher(byte[] plainBytes, PublicKey publicKey) {

       	
       	try{
       		Cipher cipher = Cipher.getInstance(ASYM_CIPHER); 
    		cipher.init(Cipher.ENCRYPT_MODE, publicKey); 
			byte [] cipheredBytes = cipher.doFinal(plainBytes); 
			return cipheredBytes;
       	}catch(NoSuchAlgorithmException | NoSuchPaddingException|BadPaddingException|InvalidKeyException|IllegalBlockSizeException e) {
       		System.out.println("Caught exception while chiphering credit card " + e);  
    	}
    	return null;

    }
    //decipherer bytes with private key
    public static byte[] asymDecipher(byte[] cipherBytes, PrivateKey privateKey) {

    	try{
    		Cipher cipher = Cipher.getInstance(ASYM_CIPHER); 
    		cipher.init(Cipher.DECRYPT_MODE, privateKey); 
    		byte [] decipheredBytes = cipher.doFinal(cipherBytes); 
    		return decipheredBytes;
    	}catch(NoSuchAlgorithmException | BadPaddingException| NoSuchPaddingException|InvalidKeyException|IllegalBlockSizeException e) {
       		System.out.println("Caught exception while chiphering credit card " + e);
    	}
    	return null;
    	
   }

   // -------Save the NONCES and verify if they are valid and unique
   public static ArrayList<String> getNonceList(){
   	return nonces;
   }

   public static boolean existNonce(String nonce){
   		return nonces.contains(nonce);
   }
   public static void insertNonce(String nonce){
   		nonces.add(nonce);
   }

	////////////////////////////////////////////////////////////////////////

    
    //Returns the public key from a certificate.
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	} 

	//Gets a Certificate object from an input stream.
	public static Certificate getX509CertificateFromStream(InputStream in) throws CertificateException {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			Certificate cert = certFactory.generateCertificate(in);
			return cert;
		} finally {
			closeStream(in);
		}
	}

	//Converts a byte array to a Certificate object. Returns null if the bytes
	//do not correspond to a certificate.
	public static Certificate getX509CertificateFromBytes(byte[] bytes) throws CertificateException {
		InputStream in = new ByteArrayInputStream(bytes);
		return getX509CertificateFromStream(in);
	}

	//Returns a Certificate object given a string with a certificate in the PEM format.
	public static Certificate getX509CertificateFromPEMString(String certificateString) throws CertificateException {
		byte[] bytes = certificateString.getBytes(StandardCharsets.UTF_8);
		return getX509CertificateFromBytes(bytes);
	}

	//Reads a certificate from a resource (included in the applicationpackage).
	public static Certificate getX509CertificateFromResource(String certificateResourcePath)
			throws IOException, CertificateException {
		InputStream is = getResourceAsStream(certificateResourcePath);
		return getX509CertificateFromStream(is);
	}

	//Reads a certificate from a file.
	public static Certificate getX509CertificateFromFile(File certificateFile)
			throws FileNotFoundException, CertificateException {
		FileInputStream fis = new FileInputStream(certificateFile);
		return getX509CertificateFromStream(fis);
	}

	//Reads a certificate from a file path.
	public static Certificate getX509CertificateFromFile(String certificateFilePath)
			throws FileNotFoundException, CertificateException {
		File certificateFile = new File(certificateFilePath);
		return getX509CertificateFromFile(certificateFile);
	}

	//Reads a collection of certificates from a file.
	public static Collection<Certificate> getX509CertificatesFromFile(File certificateFile)
			throws FileNotFoundException, CertificateException {
		FileInputStream fis = new FileInputStream(certificateFile);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		@SuppressWarnings("unchecked")
		Collection<Certificate> c = (Collection<Certificate>) cf.generateCertificates(fis);
		return c;

	}

	//Reads a collection of certificates from a file path.
	public static Collection<Certificate> getX509CertificatesFromFile(String certificateFilePath)
			throws FileNotFoundException, CertificateException {
		File certificateFile = new File(certificateFilePath);
		return getX509CertificatesFromFile(certificateFile);
	}



	// (private) key store ----------------------------------------------------


	//Reads a PrivateKey from a key-store.
	public static PrivateKey getPrivateKeyFromKeyStore(String keyAlias, char[] keyPassword, KeyStore keystore)
			throws KeyStoreException, UnrecoverableKeyException {
		PrivateKey key;
		try {
			key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyStoreException(e);
		}
		return key;
	}
						

	//Reads a PrivateKey from a key-store resource.
	public static PrivateKey getPrivateKeyFromKeyStoreResource(String keyStoreResourcePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword)
			throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore = readKeystoreFromResource(keyStoreResourcePath, keyStorePassword);
		return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
	}

	//Reads a PrivateKey from a key-store file.
	public static PrivateKey getPrivateKeyFromKeyStoreFile(File keyStoreFile, char[] keyStorePassword, String keyAlias,
			char[] keyPassword) throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore = readKeystoreFromFile(keyStoreFile, keyStorePassword);
		return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
	}

	//Reads a PrivateKey from a key store in given file path.
	public static PrivateKey getPrivateKeyFromKeyStoreFile(String keyStoreFilePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword)
			throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		return getPrivateKeyFromKeyStoreFile(new File(keyStoreFilePath), keyStorePassword, keyAlias, keyPassword);
	}

	//Reads a KeyStore from a stream.
	private static KeyStore readKeystoreFromStream(InputStream keyStoreInputStream, char[] keyStorePassword)
			throws KeyStoreException {
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		try {
			keystore.load(keyStoreInputStream, keyStorePassword);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeyStoreException("Could not load key store", e);
		} finally {
			closeStream(keyStoreInputStream);
		}
		return keystore;
	}

	//Reads a KeyStore from a resource.
	public static KeyStore readKeystoreFromResource(String keyStoreResourcePath, char[] keyStorePassword)
			throws KeyStoreException {
		InputStream is = getResourceAsStream(keyStoreResourcePath);
		return readKeystoreFromStream(is, keyStorePassword);
	}

	//Reads a KeyStore from a file.
	private static KeyStore readKeystoreFromFile(File keyStoreFile, char[] keyStorePassword)
			throws FileNotFoundException, KeyStoreException {
		FileInputStream fis = new FileInputStream(keyStoreFile);
		return readKeystoreFromStream(fis, keyStorePassword);
	}

	//Reads a KeyStore from a file path.
	public static KeyStore readKeystoreFromFile(String keyStoreFilePath, char[] keyStorePassword)
			throws FileNotFoundException, KeyStoreException {
		return readKeystoreFromFile(new File(keyStoreFilePath), keyStorePassword);
	}



	// digital signature ------------------------------------------------------

	//Signs the input bytes with the private key and returns the bytes. If
	//anything goes wrong, null is returned (swallows exceptions).
	public static byte[] makeDigitalSignature(final String signatureMethod, final PrivateKey privateKey,
			final byte[] bytesToSign) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initSign(privateKey);
			sig.update(bytesToSign);
			byte[] signatureResult = sig.sign();
			return signatureResult;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while making signature: " + e);
				System.err.println("Returning null.");
			}
			return null;
		}
	}


	//Verify signature of bytes with the public key. If anything goes wrong,
	//returns false (swallows exceptions).
	public static boolean verifyDigitalSignature(final String signatureMethod, PublicKey publicKey,
			byte[] bytesToVerify, byte[] signature) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initVerify(publicKey);
			sig.update(bytesToVerify);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying signature " + e);
				System.err.println("Returning false.");
			}
			return false;
		}
	}

	//Verify signature of bytes with the public key contained in the
	//certificate. If anything goes wrong, returns false (swallows exceptions).
	public static boolean verifyDigitalSignature(final String signatureMethod, Certificate publicKeyCertificate,
			byte[] bytesToVerify, byte[] signature) {
		return verifyDigitalSignature(signatureMethod, publicKeyCertificate.getPublicKey(), bytesToVerify, signature);
	}

	//Checks if the certificate was properly signed by the CA with the provided
	//public key.
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying certificate with CA public key : " + e);
				System.err.println("Returning false.");
			}
			return false;
		}
		return true;
	}

	//Checks if the certificate was properly signed by the CA with the provided certificate.
	public static boolean verifySignedCertificate(Certificate certificate, Certificate caCertificate) {
		return verifySignedCertificate(certificate, caCertificate.getPublicKey());
	}



	// resource stream helpers ------------------------------------------------

	//Method used to access resource.
	private static InputStream getResourceAsStream(String resourcePath) {
		// uses current thread's class loader to also work correctly inside
		// application servers
		// reference: http://stackoverflow.com/a/676273/129497
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		return is;
	}

	//Do the best effort to close the stream, but ignore exceptions. */
	private static void closeStream(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			// ignore
		}
	}



}
