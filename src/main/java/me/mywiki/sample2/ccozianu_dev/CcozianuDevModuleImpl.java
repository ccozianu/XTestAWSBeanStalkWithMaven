package me.mywiki.sample2.ccozianu_dev;

import javax.servlet.FilterConfig;

import me.mywiki.sample2.config.ReflectiveConfigurator;
import me.mywiki.sample2.oidc.OidcClientModule;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration.Builder;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration.ProviderConfig;
import me.mywiki.sample2.oidc.impl.OidcSimpleImpl;



/**
 * Simple straightforward implementation of an oidc client
 * @author Costin Cozianu (ccozianu@acm.org)
 */
public class CcozianuDevModuleImpl implements OidcClientModule {

    public static class WellKnownConfigs {
        public static final OidcClientConfiguration 
            // local to google
            TestMywikiMe_LocalDev  = ReflectiveConfigurator.configBuilderFor( OidcClientConfiguration.class, 
                                                                              OidcClientConfiguration.Builder.class)
                                        .providerCfg(
                                                ReflectiveConfigurator.configBuilderFor( OidcClientConfiguration.ProviderConfig.class, 
                                                        OidcClientConfiguration.ProviderConfig.Builder.class)
                                            .oidClientId("147310632664-rsdq8c1u42a1e4c11ipjteuti8ebduaf.apps.googleusercontent.com")
                                            //TODO: pick this up from ServletContext
                                            // we should be able to serve different hostnames from the same webapp instance as well
                                            .oidClientRedirectURL("/test2-openid-redirect")
                                            .oidClientSecret("iCbtCg1qPXM7eY8reDd8X4KS")
                                            .tokenServerURL("https://accounts.google.com/o/oauth2/token")
                                            .userinfoURL("https://www.googleapis.com/plus/v1/people/me/openIdConnect")
                                            .oidProviderBaseURL("https://accounts.google.com/o/oauth2/auth")
                                            .done()
                                            ).done();  
    }

    @Override
    public OidcRequestHandler initialize(FilterConfig filterConfig) {
        return new OidcSimpleImpl( filterConfig,
                             WellKnownConfigs.TestMywikiMe_LocalDev
                            );
    }
}
