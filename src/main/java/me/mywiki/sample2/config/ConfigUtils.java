package me.mywiki.sample2.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Verify;

/**
 * Config utilities, that also represent some minimal design choices and 
 * conventions for consuming packages with regards to:
 * - where to store configuration files -- prefer $XDG_CONFIG_HOME
 * - how to store secrets in source code
 */
public class ConfigUtils {
    

    /**
     * 
     */
    public static ToBeAESDecrypted aesDecryptionOf( String val_ ) {
        return new ToBeAESDecrypted(val_);
    }

    /**
     * a partially bound lambda, waiting for the key to decrypt
     */
    public static class ToBeAESDecrypted {
        final byte[] iv;
        final byte[] toDecrypt;
        
        private ToBeAESDecrypted(String strVal) {
            int idx= strVal.indexOf('.');
            Verify.verify( idx > -1, "Encrypted string was not encrypted with our scheme, "
                                    + " missing base32 encoded initialization vector %$s", strVal );
            iv= b32.decode( strVal.substring(0, idx));
            toDecrypt=b32.decode(strVal.substring(idx+1));
        }

        public String byPasswordDerivedKey( String passwd, String domain, String configKeyName ) {
            return CryptoUtils.decryptSecretValue(toDecrypt, passwd, domain, configKeyName, iv );
        }
    }

    private final static Base32 b32= new Base32();

    /**
     * Initializer of XDG conventions
     */
    public static class XDG {
        static final String XDG_CONFIG_HOME; static {
            XDG_CONFIG_HOME= System.getenv("XDG_CONFIG_HOME");
            //TODO: maybe initialize to $HOME/XDG/config
            //TODO: maybe initialize with XDG_CONFIG_DIRS
            if (XDG_CONFIG_HOME == null) {
                throw new RuntimeException("XDG_CONFIG_HOME env variable is  undefined");
            }
        }
    }
    public static File xdgConfigFile(String domain, String fileName) {
        File result= new File(new File(XDG.XDG_CONFIG_HOME,domain), fileName);
        if (! result.exists()) {
            throw new RuntimeException("File does not exist: %1$s" +result.getPath());
        }
        return result;
    }
    

    public static String contentsAsString(File xdgConfigFile) {
        try {
            // we need to trim, to avoid inadvertent spaces and \n
            return IOUtils.toString(new URL("file:"+xdgConfigFile.getAbsolutePath())).trim();
        }
        catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
