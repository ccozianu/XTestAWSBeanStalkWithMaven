package me.mywiki.sample2.ccozianu_dev;

import javax.servlet.FilterConfig;


import me.mywiki.sample2.oidc.OidcClientModule;
import me.mywiki.sample2.oidc.impl.OidcSimpleImpl;



/**
 * Simple straightforward implementation of an oidc client
 * @author Costin Cozianu (ccozianu@acm.org)
 */
public class OidcClientImpl implements OidcClientModule {

    @Override
    public OidcRequestHandler initialize(FilterConfig filterConfig) {
        return new OidcSimpleImpl( filterConfig,
                             DevOidcConfigs.WellKnown.TestMywikiMe_LocalDev
                            );
    }
}
