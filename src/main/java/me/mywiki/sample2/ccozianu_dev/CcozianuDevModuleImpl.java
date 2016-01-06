package me.mywiki.sample2.ccozianu_dev;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.FilterConfig;

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
                                            .oidClientSecret("iCbtCg1qPXM7eY8reDd8X4KS")
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
}
