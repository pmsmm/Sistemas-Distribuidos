package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import java.security.spec.*;
import java.security.cert.Certificate;

import org.komparator.security.CryptoUtil;

import org.junit.*;
import static org.junit.Assert.*;

import static javax.xml.bind.DatatypeConverter.printHexBinary;


public class CryptoUtilTest {

    private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding"; /**Algoritmo de cifra/Algoritmo de processamento de blocos/Algoritmo de padding*/

    private static final String CERTIFICATE = "example.cer"; /**Certificado digital da chave publica*/

    private static final String KEYSTORE = "example.jks"; /**Ficheiro que guarda a chave privada*/
    private static final String KEYSTORE_PASSWORD = "1nsecure"; /**Store password*/

    private static final String KEY_ALIAS = "example";
    private static final String KEY_PASSWORD = "ins3cur3"; /**Key password*/
    private static final String SIGNATURE_ALGO = "SHA256withRSA";

    /** ASYM_ALGO e ASYM_KEY_SIZE sao usadas exclusivamente para gerar pares de chaves para testes com chaves incorrectas*/
    /** Asymmetric cryptography algorithm. */
    private static final String ASYM_ALGO = "RSA";
    /** Asymmetric cryptography key size. */
    private static final int ASYM_KEY_SIZE = 2048;

    /** Plain text to digest. */
    private final String plainText = "This is the plain text!";
    /** Plain text bytes. */
    private final byte[] plainBytes = plainText.getBytes();

    private static PrivateKey privateKey; /**Ira guardar a chave privada*/
    private static PublicKey publicKey; /**Ira guardar a chave publica*/
    private static final String certificateFilePath = "src/test/resources/example.cer"; /**File path of the certificate with the public key*/
    private static final String keyStoreFilePath = "src/test/resources/example.jks"; /**File path of the key store with the private key*/
    //saves the nonces 
    private static ArrayList<String> nonces = new ArrayList<String>();
    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {

    CryptoUtil aux = new CryptoUtil();

    //get Certificate from Certificate File Path
    Certificate certificate = aux.getX509CertificateFromFile(certificateFilePath);
    
    //get public key from certificate
    publicKey = certificate.getPublicKey();

    //get private key from certificate
    privateKey = aux.getPrivateKeyFromKeyStoreFile(keyStoreFilePath,KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray()); 

   
    }
    @AfterClass
    public static void oneTimeTearDown() {
        // runs once after all tests in the suite
    }

    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() {

        // runs before each test
    }

    @After
    public void tearDown() {
        // runs after each test
    }

    // tests
    @Test
public void testCipherPublicDecipherPrivate() throws Exception {
        System.out.print("TEST ");
        System.out.print(ASYM_CIPHER);
        System.out.println(" cipher with public, decipher with private");

        System.out.print("Text: ");
        System.out.println(plainText);
        System.out.print("Bytes: ");
        System.out.println(printHexBinary(plainBytes));

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance(ASYM_CIPHER);

        System.out.println("Ciphering  with public key...");
        // encrypt the plain text using the public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherBytes = cipher.doFinal(plainBytes);

        System.out.println("Ciphered bytes:");
        System.out.println(printHexBinary(cipherBytes));

        System.out.println("Deciphering  with private key...");
        // decipher the ciphered digest using the private key
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decipheredBytes = cipher.doFinal(cipherBytes);
        System.out.println("Deciphered bytes:");
        System.out.println(printHexBinary(decipheredBytes));

        System.out.print("Text: ");
        String newPlainText = new String(decipheredBytes);
        System.out.println(newPlainText);

        assertEquals(plainText, newPlainText);
    }
    @Test
    public void testCipherPrivateDecipherPublic() throws Exception {
        System.out.print("TEST ");
        System.out.print(ASYM_CIPHER);
        System.out.println(" cipher with private, decipher with public");

        System.out.print("Text: ");
        System.out.println(plainText);
        System.out.print("Bytes: ");
        System.out.println(printHexBinary(plainBytes));

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance(ASYM_CIPHER);

        System.out.println("Ciphering with private key...");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] cipherBytes = cipher.doFinal(plainBytes);

        System.out.println("Ciphered bytes:");
        System.out.println(printHexBinary(cipherBytes));

        System.out.println("Deciphering with public key...");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decipheredBytes = cipher.doFinal(cipherBytes);
        System.out.println("Deciphered bytes:");
        System.out.println(printHexBinary(decipheredBytes));

        System.out.print("Text: ");
        String newPlainText = new String(decipheredBytes);
        System.out.println(newPlainText);

        assertEquals(plainText, newPlainText);
    }

    @Test(expected=javax.crypto.BadPaddingException.class)
    public void privateKeyWrongMessageWrong() throws Exception {
        System.out.print("TEST ");
        System.out.print(ASYM_CIPHER);
        System.out.println(" cipher with right public, decipher with wrong private");

        String wrongMessage = "This is a wrong message!";
        byte[] wrongBytes = wrongMessage.getBytes();

        System.out.print("Wrong message to send: ");
        System.out.println(wrongMessage);
        System.out.print("Bytes: ");
        System.out.println(printHexBinary(wrongBytes));

        // generate an RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYM_ALGO);
        keyGen.initialize(ASYM_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance(ASYM_CIPHER);

        System.out.println("Ciphering with public key...");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherBytes = cipher.doFinal(wrongBytes);

        System.out.println("Ciphered bytes:");
        System.out.println(printHexBinary(cipherBytes));

        System.out.println("Deciphering with public key...");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decipheredBytes = cipher.doFinal(cipherBytes);
        System.out.println("Deciphered bytes:");
        System.out.println(printHexBinary(decipheredBytes));

        System.out.print("Text: ");
        String newPlainText = new String(decipheredBytes);
        System.out.println(newPlainText);

        assertFalse(Objects.equals(plainText, newPlainText));
    }

    @Test(expected=javax.crypto.BadPaddingException.class)
    public void privateKeyWrongMessageRight() throws Exception {
        System.out.print("TEST ");
        System.out.print(ASYM_CIPHER);
        System.out.println(" cipher with right public, decipher with wrong private");

        System.out.print("Text: ");
        System.out.println(plainText);
        System.out.print("Bytes: ");
        System.out.println(printHexBinary(plainBytes));

        // generate an RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYM_ALGO);
        keyGen.initialize(ASYM_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance(ASYM_CIPHER);

        System.out.println("Ciphering with public key...");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherBytes = cipher.doFinal(plainBytes);

        System.out.println("Ciphered bytes:");
        System.out.println(printHexBinary(cipherBytes));

        System.out.println("Deciphering with public key...");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decipheredBytes = cipher.doFinal(cipherBytes);
        System.out.println("Deciphered bytes:");
        System.out.println(printHexBinary(decipheredBytes));

        System.out.print("Text: ");
        String newPlainText = new String(decipheredBytes);
        System.out.println(newPlainText);

        assertFalse(Objects.equals(plainText, newPlainText));
    }

    @Test
    public void privateKeyRightMessageWrong() throws Exception {
        System.out.print("TEST ");
        System.out.print(ASYM_CIPHER);
        System.out.println(" cipher with right public, decipher with right private");

        String wrongMessage = "This is a wrong message!";
        byte[] wrongBytes = wrongMessage.getBytes();

        System.out.print("Wrong message to send: ");
        System.out.println(wrongMessage);
        System.out.print("Bytes: ");
        System.out.println(printHexBinary(wrongBytes));

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance(ASYM_CIPHER);

        System.out.println("Ciphering with public key...");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherBytes = cipher.doFinal(wrongBytes);

        System.out.println("Ciphered bytes:");
        System.out.println(printHexBinary(cipherBytes));

        System.out.println("Deciphering with public key...");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decipheredBytes = cipher.doFinal(cipherBytes);
        System.out.println("Deciphered bytes:");
        System.out.println(printHexBinary(decipheredBytes));

        System.out.print("Text: ");
        String newPlainText = new String(decipheredBytes);
        System.out.println(newPlainText);

        if(!plainText.equals(newPlainText)) {
            System.out.println("The messages are different!");
        }

        assertFalse(plainText.equals(newPlainText));
    }

}
