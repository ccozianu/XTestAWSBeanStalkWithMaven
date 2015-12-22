package me.mywiki.sample2.oidc;

import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

/**
 * This interface defines the mechanism to instantiate 
 * and use an open id client in a J2EE Servlet context (WebApp).
 */
public interface OidcClientModule {
	
	public static String SESSION_NAME_FOR_USER_OBJECT="OIDC_USER_OBJECT";
	public static String CUSTOM_NAME_FOR_USER_OBJECT="SESSION_NAME_FOR_USER_OBJECT";
	
	/**
	 * To be fully functional an OpenIDConnect client
	 * needs to have the following:
	 * 1) a hook into the webapp (as provided by the FilterConfig
	 * 2) configuration as a client to an OpenIDProvider
	 * 3) a request "parser" (deciding which URLs need to be authenticated and how)
	 * 	 that is specific to the web app in which open id connect is integrated
	 */
	public OidcContext initialize( FilterConfig cfg_,
	                               OidcLoginDecider requestFilter );
	
	public interface OidcContext {
	    
	}

	/**
	 * The concrete servlet filter that can be installed
	 * to preprocess HttpRequest 
	 */
	public interface OidcClientFilter extends Filter {
		
	}
	

	/**
	 * This interface defines what configuration values are needed
	 * for an open id client to inter-operate with a openid provider
	 */
	public interface OidcClientConfiguration {
		
	}
	
	
	enum LoginDecision {
		PERFORM_OIDC_LOGIN_REDIRECT,
		PROCESS_PROVIDER_CALLBACK,
		LET_IT_THROUGH ;
	}
	
	public interface OidcLoginDecider {
		boolean loginDispatch(HttpServletRequest htRequest, OidcContext ctx);
	}

}
