package me.mywiki.sample2.config;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

import com.google.common.base.Verify;

public class CryptoUtils {

    public static SecretKey generateKeyFromPassword( String password, String domainName, String keyName) 
    {
      try {
        // This is JDK dependent and can break decryption when migrating from one to another
        // in order to fix that we need a has of byte arrays and a pseudorandom
        Random r = new Random();
        int hash1= Arrays.hashCode(domainName.getBytes(StandardCharsets.US_ASCII));
        int hash2= Arrays.hashCode(keyName.getBytes(StandardCharsets.US_ASCII));
        r.setSeed(hash1 + (((long)hash2) << 32));
        byte[] secretKeySaltBytes= new byte[32];
        r.nextBytes(secretKeySaltBytes);
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), 
                secretKeySaltBytes, 
                10, 
                256 );
 
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec skeySpec=  new SecretKeySpec(secretKey.getEncoded(), "AES");
        return skeySpec;
      }
      catch (Exception ex) {
          if (ex instanceof RuntimeException) { throw ((RuntimeException) ex); }
                                         else { throw new RuntimeException(ex); }
      }
    }
    
    
    
    private static final Base32 b32=new Base32();
    /**
     * In order to store a secret to be later decrypted with a secret key,
     * we also need the salt.
     * This is a simple format to allow to have these two in a single piece:
     * stored_secret ::= <salt_encoded_base2> "." <encrypted_text_encoded_base32>
     */
    public static String toBase32DotBase32( byte[] salt, byte[] encryptedMaterial ) {
        return b32.encodeAsString(salt) + "."+b32.encodeAsString(encryptedMaterial);
    }

    public static String encryptSecretValue(String secretVal, String password, String domain) {
        return encryptSecretValue(secretVal, password, domain , "");
    }
    
    public static String encryptSecretValue( String secretVal, String password, String domain, String varName ) 
    { 
        try {
            SecretKey key= CryptoUtils.generateKeyFromPassword(password, domain, varName);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            
            byte[] iv= new byte[32];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec myParams = new GCMParameterSpec(128, iv);
            
            c.init(Cipher.ENCRYPT_MODE, key, myParams);
    
            byte[] result= c.doFinal( secretVal.getBytes(StandardCharsets.UTF_8));
            return b32.encodeAsString(iv) +'.' + b32.encodeAsString(result);
        }
        catch (Exception ex) {
            if (ex instanceof RuntimeException) { throw ((RuntimeException) ex); }
                                           else { throw new RuntimeException(ex); }
        }
    }
    
    /**
     * returns a decrypted secret value, expects encryption was performed with the equivalent
     * <code>encryptSecretValue( String secretVal, String password, String domain, String varName )</code>  
     */
    public static String decryptSecretValue( String strVal, String passwd, String domain, String varName) {
        try {
            
            int idx= strVal.indexOf('.');
            Verify.verify( idx > -1, "Encrypted string was not encrypted with our scheme, "
                                    + " missing base32 encoded initialization vector %$s", strVal );
            byte[] iv= b32.decode( strVal.substring(0, idx));
            byte[] encrypted= b32.decode(strVal.substring(idx+1));

            return decryptSecretValue(encrypted,  generateKeyFromPassword(passwd, domain, varName), iv);
        }
        catch (Exception ex) {
            if (ex instanceof RuntimeException) { throw ((RuntimeException) ex); }
                                           else { throw new RuntimeException(ex); }
        }
    }
    
    
    /**
     * This fixes the Cipher to be "AES/GCM/NoPadding"
     * The key can be 128 or 256 bytes, IV the same
     */

    public static String decryptSecretValue(  byte[] encrypted , 
                                              SecretKey key , 
                                              byte[] iv )
    {
        try {
            
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec myParams = new GCMParameterSpec(128, iv);
            c.init(Cipher.DECRYPT_MODE, key, myParams);
            byte[] decrypted= c.doFinal(encrypted);
    
            return new String(decrypted,StandardCharsets.UTF_8);
        }
        catch (Exception ex) {
            if (ex instanceof RuntimeException) { throw ((RuntimeException) ex); }
                                           else { throw new RuntimeException(ex); }
        }
    }
 
    public static String decryptSecretValue( byte[] toDecrypt, 
                                             String passwd, String domain, String configKeyName,
                                             byte[] iv) {
        return decryptSecretValue(toDecrypt, generateKeyFromPassword(passwd, domain, configKeyName), iv);
    }
    
    // This is for test only, no secret was ever generated with BlahBlahBlah
    public static String MYTESTPASSWORD="BlahBlahBlah";
    public String domain, name;

    /**
     * Just for quick testing/debugging
     */
    public static void main(String[] args) {
        try {
            
            //for lack of policy files
            try { 
                Field field = Class.forName("javax.crypto.JceSecurity").
                getDeclaredField("isRestricted");
                field.setAccessible(true);
                field.set(null, java.lang.Boolean.FALSE); 
            } 
            catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
            }
            
            //take the encryption password from the first arg
            if (args.length > 1) { MYTESTPASSWORD= args[0]; }
            
            String toEncrypt= "qd4K5xRXHNIVK6zVlUiYFflu";
            String encryptedText= encryptSecretValue( toEncrypt, MYTESTPASSWORD, "me.mywiki", "com.google/me.mywiki/clientSecret");
            System.out.println("Encrypted text: " + encryptedText);
            String decryptedText= decryptSecretValue( encryptedText, MYTESTPASSWORD, "me.mywiki", "com.google/me.mywiki/clientSecret");
            System.out.println("Decrypted text: " + decryptedText);
        }
        catch(Exception ex) {
            ex.printStackTrace(System.err);
        }
        
    }

   

}
