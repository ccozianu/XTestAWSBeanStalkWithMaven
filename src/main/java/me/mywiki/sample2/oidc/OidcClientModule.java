package me.mywiki.sample2.oidc;

import java.net.URI;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.mywiki.sample2.webapp.TestMywikiMe;



/**
 * This interface defines the mechanism to instantiate 
 * and use an open id client in a J2EE Servlet context (WebApp).
 */
public interface OidcClientModule {
    
    public static String SESSION_NAME_FOR_USER_OBJECT = "OIDC_USER_OBJECT";
	public static String CUSTOM_NAME_FOR_USER_OBJECT  =  "SESSION_NAME_FOR_USER_OBJECT";
    public static String DEFAULT_USERDATA_SESSION_NAME = "me.mywiki.oidc.Oidc_UserData";
    public static String DEFAULT_IDCLAIMS_SESION_NAME = "me.mywiki.oidc.Oidc_IDClaims";
	
	/**
	 * To be fully functional an OpenIDConnect client
	 * needs to have the following:
	 * 1) a hook into the webapp (as provided by the FilterConfig
	 * 2) configuration as a client to an OpenIDProvider
	 * 3) a request "parser" (deciding which URLs need to be authenticated and how)
	 * 	 that is specific to the web app in which open id connect is integrated
	 */
	public OidcRequestHandler initialize( FilterConfig cfg_ );
	

	public static class OidcAuthError extends RuntimeException {
	    public OidcAuthError() { }
        public OidcAuthError(String msg) { super(msg); }

        private static final long serialVersionUID = -2473389450100453103L;
	}

	/**
	 * The concrete filter object (ServletFilter as per j2ee spec servlet-api  >2.5 ) that can be installed
	 * to preprocess HttpRequests 
	 */
	public interface OidcRequestHandler  {
		public void  processOidcStart(HttpServletRequest req, HttpServletResponse response, String chosenProvider );
		public boolean  processOidcCallback( HttpServletRequest req, HttpServletResponse response, String chosenProvider );
        public OidcClientConfiguration.WebAppComponentConfig webAppComponentConfig();
	}
	
	public static interface OpenIDClient  {

	    URI buildLoginURI();
	    boolean performProfileRetrieval(HttpServletRequest redirectCbReq);
	}

	public static interface OidcModuleConfiguration {
	    Map<String,OidcClientConfiguration> providersConfiguration();
	}

	/**
	 * This interface defines what configuration values are needed
	 * for an open id client component to inter-operate with an openid provider
	 * and to communicate with the web app
	 */
	public static interface OidcClientConfiguration {
	    
	    public ProviderConfig providerCfg();
        public WebAppComponentConfig webAppComponentConfig();

        public static interface ProviderConfig {
            public String oidProviderBaseURL();
            public String oidClientId();
            public String oidClientRedirectURL();
            
            public String tokenServerURL();
            public String userinfoURL();
            
            public String oidClientSecret();
            
            public Builder cloneBuilder();
            
            public static interface Builder {
                
                Builder oidProviderBaseURL(String val);
                Builder oidClientId(String val);
                Builder oidClientRedirectURL(String val);

                Builder tokenServerURL(String url);
                Builder userinfoURL( String url);
                
                Builder oidClientSecret(String val);

                ProviderConfig done();
            }
            
        }


        public static interface WebAppComponentConfig {
            
            boolean needsHttps();
            Builder cloneBuilder();

            public static interface Builder {
                Builder  needsHttps(boolean val_);
                WebAppComponentConfig done();
            }
        }
	    
	    public Builder cloneBuilder();

	    public static interface Builder {
	        Builder providerCfg(ProviderConfig val_);
	        Builder webAppComponentConfig(WebAppComponentConfig val_);
	        OidcClientConfiguration done();
	        
	    }




	}
	
	
	/**
	 * Utilities for declaring errors
	 * TODO: needs refactoring as currently a hack, it couples in error handling
	 *       with a particular wepapp context
	 *       this class does not belong in OIDC package
	 */
	public static class Err {
	    public static class ErrorData {
	        public final int majorCode;
	        public final int minorCode;
	        public final String userMessage;
	        public final Object extraData;
	        public final String nextUrl;

            public ErrorData( int majorCode_ ,
                    int minorCode_ ,
                    String userMessage_,
                    Object extraData_) 
            {
                this( majorCode_,minorCode_,userMessage_,extraData_,null);
            }
	        
	        public ErrorData( int majorCode_ ,
                              int minorCode_ ,
                              String userMessage_,
                              Object extraData_,
                              String nextUrl_ ) 
	        {
                this.majorCode=  majorCode_;
                this.minorCode= minorCode_;
                this.userMessage= userMessage_;
                this.extraData=  extraData_;
                this.nextUrl= nextUrl_;
            }
	 
	        /**
	         * TODO: move this utility outta here
	         */
	        @Deprecated
	        public static ErrorData fromRequest (HttpServletRequest req) {
	            return (ErrorData) req.getAttribute( TestMywikiMe.BootstrapFiler.OIDC_FILTER_ERROR );
	        }
	    }
	    


        public static ErrorData notAuthenticated(HttpServletRequest htReq, HttpServletResponse htResponse) {
           //TODO: extract constants for error codes
           return new ErrorData( -30101, -1, "Request to a restricted resource was not authenticated", null);
        }

        public static ErrorData errNoLoginOption() {
            return new ErrorData( -30101, -2, "Login form submitted does not have a loginOption", null);
        }

        public static ErrorData notImplementedYet(String function) {
            return new ErrorData( -30101, -3, "Function: " + function+ " is not implemnted yet.", null);
        }

        public static ErrorData invalidLoginOption(String loginOption) {
            return new ErrorData( -30101, -4, "Login form submit has an invalid loginOption value:" + loginOption, loginOption);
        }

        public static ErrorData callBackWithoutSession() {
            return new ErrorData( -30101, -5, "Received openID callback URL without a user session", null);
        }

        public static ErrorData callBackMissingProviderInSession() {
            return new ErrorData( -30101, -6, "Received openID callback URL without a user session", null);
        }
  
	}



}
