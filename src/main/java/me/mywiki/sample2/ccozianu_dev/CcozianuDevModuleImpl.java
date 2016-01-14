package me.mywiki.sample2.ccozianu_dev;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.FilterConfig;

import me.mywiki.sample2.config.ConfigUtils;
import me.mywiki.sample2.config.ReflectiveConfigurator;
import me.mywiki.sample2.oidc.OidcClientModule;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration.Builder;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration.ProviderConfig;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration.WebAppComponentConfig;
import me.mywiki.sample2.oidc.impl.OidcSimpleImpl;



/**
 * Simple straightforward implementation of an oidc client
 * @author Costin Cozianu (ccozianu@acm.org)
 */
public class CcozianuDevModuleImpl implements OidcClientModule {
    
    static { 
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").
            getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE); 
        }
        catch(Exception ex) {
            if (ex instanceof RuntimeException) { throw ((RuntimeException) ex);}
            else                                { throw new RuntimeException(ex); }
        }
    } 
    
    //hostname hack, always useful
    public static final  String hostname; static {
        try {
            hostname= InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class WellKnownConfigs {
        public static final OidcClientConfiguration 
            // local to developer desktop
            TestMywikiMe_LocalDev  = ReflectiveConfigurator.configBuilderFor( OidcClientConfiguration.class, 
                                                                              OidcClientConfiguration.Builder.class)
                                        .providerCfg(
                                                ReflectiveConfigurator.configBuilderFor( OidcClientConfiguration.ProviderConfig.class, 
                                                        OidcClientConfiguration.ProviderConfig.Builder.class)
                                            .oidClientId("147310632664-rsdq8c1u42a1e4c11ipjteuti8ebduaf.apps.googleusercontent.com")
                                            //TODO: pick this up from ServletContext
                                            // we should be able to serve different hostnames from the same webapp instance as well
                                            .oidClientRedirectURL("/test2-openid-redirect")
                                            // this is not the real secret, the real secret is different
                                            //.oidClientSecret("qd4K5xRXHNIVK6zVlUiYFflu")
                                            .oidClientSecret(
                                                ConfigUtils.aesDecryptionOf(
                                                                "CXILH5AZRHJ4O5YRS2XLZRCOMGKDQDECOVAC73NKSNVQ2RDGDJCA===="
                                                                 +'.'
                                                                 +"Z6PJSWDORYWLE3YV5NIOGD46IBVDPUL4DW6GHEL4XS3KUS3J67ZOSCN2IHMWNIES")
                                                            .byPasswordDerivedKey( 
                                                                    ConfigUtils.contentsAsString(
                                                                            ConfigUtils.xdgConfigFile( "me.mywiki",
                                                                                           "software_master_password"))
                                                                    ,"me.mywiki","com.google/me.mywiki/clientSecret"
                                                                    )) 
                                            .tokenServerURL("https://accounts.google.com/o/oauth2/token")
                                            .userinfoURL("https://www.googleapis.com/plus/v1/people/me/openIdConnect")
                                            .oidProviderBaseURL("https://accounts.google.com/o/oauth2/auth")
                                            .done()
                                            )
                                        .webAppComponentConfig(
                                                ReflectiveConfigurator.configBuilderFor( WebAppComponentConfig.class,
                                                                                         WebAppComponentConfig.Builder.class )
                                                    .needsHttps(false)
                                                    .done())
                                        .done();
        
             
           // temporarily until we migrate to secret based crap
           public static final OidcClientConfiguration 
               TestMywikiMe_AWS = TestMywikiMe_LocalDev.cloneBuilder()
                                       .webAppComponentConfig( 
                                               TestMywikiMe_LocalDev.webAppComponentConfig() .cloneBuilder()
                                                   .needsHttps(true).done() )
                                       .done();
    }

    @Override
    public OidcRequestHandler initialize(FilterConfig filterConfig) 
    {
        return new OidcSimpleImpl( filterConfig,
                                   hostname.startsWith("ip-") ? 
                                           WellKnownConfigs.TestMywikiMe_AWS
                                           : WellKnownConfigs.TestMywikiMe_LocalDev );
    }
    
    public static void main(String [] args ) {
        try { 
            System.out.println(WellKnownConfigs.TestMywikiMe_LocalDev);
        } 
        catch (Exception ex) {
                System.err.println(ex);
                ex.printStackTrace();
                System.exit(-1);
        }
    }
}
