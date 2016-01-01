package me.mywiki.sample2.oidc;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import me.mywiki.sample2.oidc.OidcClientModule.Err.ErrorData;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration;

/**
 * This interface defines the mechanism to instantiate 
 * and use an open id client in a J2EE Servlet context (WebApp).
 */
public interface OidcClientModule {
    
    /**
     * 
     */
    public static class UserProfile {
        public final String id;
        public final String name;
        public final String email;
        public final String pictureUrl;
        public final String profileUrl;
        public UserProfile( @JsonProperty("sub") String id_,
                            @JsonProperty("name") String name_,
                            @JsonProperty("email") String email_,
                            @JsonProperty("picture") String pictureUrl_,
                            @JsonProperty("profile") String profileUrl_ ) 
        {
            this.id= id_;
            this.name= name_;
            this.email= email_;
            this.pictureUrl= pictureUrl_;
            this.profileUrl= profileUrl_;
        }
    }
	
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
	


	/**
	 * The concrete filter object (ServletFilter as per j2ee spec servlet-api  >2.5 ) that can be installed
	 * to preprocess HttpRequests 
	 */
	public interface OidcRequestHandler  {
		public void  processOidcStart(HttpServletRequest req, HttpServletResponse response, String chosenProvider );
		public boolean  processOidcCallback( HttpServletRequest req, HttpServletResponse response, String chosenProvider );
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


	    public ProviderConfig providerCfg();
	    
	    public Builder cloneBuilder();

	    public static interface Builder {
	        Builder providerCfg(ProviderConfig val_);
	        OidcClientConfiguration done();
	    }



	}
	
	
	/**
	 * Utilities for declaring errors
	 * @author Costin
	 *
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
