package me.mywiki.sample2.oidc.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import me.mywiki.sample2.oidc.OidcClientModule;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientFilter;
import me.mywiki.sample2.oidc.OidcClientModule.OidcContext;
import me.mywiki.sample2.oidc.OidcClientModule.OidcLoginDecider;

import static me.mywiki.sample2.oidc.OidcClientModule.*;

/**
 * Simple straightforward implementation of an oidc client
 * @author Costin Cozianu (ccozianu@acm.org)
 */
public class OidcClientImpl implements OidcClientModule {


	@Override
	public OidcContext initialize( FilterConfig cfg_, 
	                               OidcLoginDecider requestFilter ) 
	{
	    throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * The actual meat and bones of the implementation
	 */
	public static class OidcImpl implements OidcClientFilter {
		
		private FilterConfig cfg;
		private OidcClientConfiguration oidcClientCfg;
		private OidcLoginDecider loginFilter;

		public OidcImpl( FilterConfig cfg_,
						 OidcClientConfiguration oidcClientCfg_,
						 OidcLoginDecider loginFilter_ ) 
		{
			this.cfg= cfg_;
			this.oidcClientCfg= oidcClientCfg_;
			this.loginFilter= loginFilter_;
		}
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
		    throw new UnsupportedOperationException("Shit niot implemented yet");
		}
		
		@Override
		public void destroy() {
		    //really nothing to do here, yet
		}
		
		@Override
		public void doFilter( ServletRequest request_, 
							  ServletResponse response_, 
							  FilterChain filterChain )
							  throws IOException, ServletException 
		{
		       HttpServletRequest htRequest= (HttpServletRequest) request_;
		       HttpServletResponse htResponse= (HttpServletResponse) response_;
		       
		       
		       //if this request is a redirect from OIDC server
		       /*
		       if ( htRequest.getRequestURL().toString().startsWith(SampleClients.THISAPPS_OPENID_REDIRECT )) {
		           String oidcProviderName;
		           OpenIDClient oidClient;
		           if (htRequest.getSession() == null) {
		               sendToAuthError( htRequest, htResponse);
		               return;
		           }
		           
		           oidcProviderName = (String) htRequest.getSession().getAttribute("OIDC_PROVIDER");
		           if (oidcProviderName == null) {
		               sendToAuthError( htRequest, htResponse);
		               return;
		           }
		           
		           oidClient= CONFIGURED_CLIENTS.get(oidcProviderName);
		           if (oidClient == null) {
		               sendToAuthError( htRequest, htResponse);
		               return;
		           }
		           if ( oidClient.performProfileRetrieval(htRequest))
		           {
		               sendToDefaultPage(htRequest, htResponse);
		           }
		           else {
		               sendToAuthError( htRequest, htResponse);
		           }
		       }
		       else if (htRequest.getParameter(LOGIN_ACTION_PARAM) != null) {
		           handleLoginRequest(htRequest, htResponse);
		       }
		       else if ( isAuthOptional(htRequest) ) {
		           chain.doFilter(htRequest, htResponse );
		       }
		       else {
		           HttpSession session = htRequest.getSession(false);
		           Object authObject= session != null 
		                                       ? session.getAttribute(AUTH_OBJECT_SESSION_NAME)
		                                       : null;
		           //TODO: validate
		           if (authObject != null ) {
		               logger.info("Auth object obtained, getting throug to:" + htRequest.getRequestURI());
		               chain.doFilter(htRequest, htResponse );
		           }
		           else {
		               sendToAuthError(htRequest, htResponse);
		           }
		       } 
		       */

		}

		private void processProviderCallback( HttpServletRequest htRequest, 
		                                      HttpServletResponse htResponse,
		                                      FilterChain filterChain ) 
		{
		    throw new UnsupportedOperationException("Not implemented yet.");
        }
		
	}
}
