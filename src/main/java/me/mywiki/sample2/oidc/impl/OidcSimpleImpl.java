package me.mywiki.sample2.oidc.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;

import me.mywiki.sample2.ccozianu_dev.CcozianuDevModuleImpl;
import me.mywiki.sample2.oidc.OidcClientModule;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration;
import me.mywiki.sample2.oidc.OidcClientModule.OidcRequestHandler;
import me.mywiki.sample2.oidc.UserProfile;
import me.mywiki.sample2.oidc.OidcClientModule.OidcClientConfiguration.WebAppComponentConfig;

/**
 * The actual meat and bones of the implementation
 */
public class OidcSimpleImpl implements OidcRequestHandler {
	
   @Override
   public WebAppComponentConfig webAppComponentConfig() {
       return oidcClientCfg.webAppComponentConfig();
   }
    
    @Override
    public boolean processOidcCallback(HttpServletRequest redirectRequest, HttpServletResponse htResponse, String chosenProvider) {
        String codeVal= redirectRequest.getParameter(OidcNames.OIDC_PNAME_AUTHCODE);
        if (StringUtils.isEmpty(codeVal)) {
            logger.severe("Authorization redirect missing code");
            return false;
        }
        
        logger.info("Trying to obtain auth token for code:"+codeVal);
    
        try (CloseableHttpClient httpClient= HttpClients.createDefault()) {
            
            HttpPost httpPost = new HttpPost(this.oidcClientCfg.providerCfg().tokenServerURL());
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("grant_type", "authorization_code"));
            formparams.add(new BasicNameValuePair("code", codeVal ));
            //TODO: verify this is not needed here
            formparams.add(new BasicNameValuePair("redirect_uri", buildCallbackURI(redirectRequest).toString()));
            UrlEncodedFormEntity 
                submitForm = new UrlEncodedFormEntity( formparams, 
                                                       StandardCharsets.UTF_8);
            httpPost.setEntity(submitForm);
            
            httpPost.addHeader( "Authorization", 
                                String.format( "Basic %s", 
                                               Base64.getUrlEncoder()
                                                     .encodeToString(
                                                             String.format( "%s:%s", 
                                                                             this.oidcClientCfg.providerCfg().oidClientId(), 
                                                                             this.oidcClientCfg.providerCfg().oidClientSecret())
                                                                   .getBytes(StandardCharsets.UTF_8)
                                                                )));
            logger.info("Client Authorization:" +httpPost.getFirstHeader("Authorization"));
            //TODO: replace wrap this in a proper retry
                logger.info("Executing request: " + httpPost.getRequestLine() 
                             + " to target " + oidcClientCfg.providerCfg().tokenServerURL());
                
                try (CloseableHttpResponse authResponse = httpClient.execute(httpPost)) {
                    logger.info("Response status: "+authResponse.getStatusLine());
                    logger.info("Response headers: "+ Arrays.asList( authResponse.getAllHeaders()));
                    if (authResponse.getStatusLine().getStatusCode() != 200 ) {
                        throw new RuntimeException("Invalid (maybe stale/replay) redirect request");
                    }
                    HttpEntity entity= authResponse.getEntity();
                    long contentLength = entity != null 
                                            ? entity.getContentLength()
                                            : 0;
                    logger.info("Content length is: "+contentLength);                        
                    if ( contentLength < OIDC_LIMIT_MAX_RESPONSE_LENGTH) 
                    {
                       String strResponse= EntityUtils.toString(entity);
                       ObjectMapper oMapper= new ObjectMapper();
                       Map<?,?> jsonResponse = oMapper.readValue( strResponse
                                                                 , Map.class);
                       String authTokenRaw= null; 
                       String idTokenRaw= null; 
                       ReadOnlyJWTClaimsSet idClaims= null;
                       String refreshTokenRaw= null;
                       
                       logger.info("JSON respomnse from token endpoint: "+jsonResponse);
                       
                       Object userProfile=null;
                       if (jsonResponse.containsKey("id_token") ) {
                           idTokenRaw= (String) jsonResponse.get("id_token");
                           idClaims= validateIdToken(idTokenRaw);
                       }
                       if (jsonResponse.containsKey("access_token")) {
                           authTokenRaw= (String) jsonResponse.get("access_token");
                           userProfile= retrieveUserProfile(authTokenRaw);
                       }
                       if (jsonResponse.containsKey("refresh_token") ) {
                           refreshTokenRaw= (String) jsonResponse.get("refresh_token");
                       }
                       
                       HttpSession session= redirectRequest.getSession(true);
                       session.setAttribute( OidcClientModule.DEFAULT_IDCLAIMS_SESION_NAME, idClaims);
                       session.setAttribute( OidcClientModule.DEFAULT_USERDATA_SESSION_NAME, userProfile);
                       
                       logger.info("Token endpoint response:\n"+ jsonResponse);
                       logger.info("Extracted access_token:"+authTokenRaw);
                       logger.info("Extracted id_token: " +idTokenRaw);
                       return true;
                    }
                }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex, () -> "Exception thrown trying to get the tokens");
            if (ex instanceof RuntimeException) { throw (RuntimeException)ex; }
            else                                { throw  new RuntimeException(ex); }
        }
        
        return false;
    }
	
	
	private static ReadOnlyJWTClaimsSet validateIdToken(String idTokenRaw) {
	        try {
	            JWT idToken= JWTParser.parse(idTokenRaw);
	            logger.info(() -> "got jwt token: " +  Objects.toString(idToken) );
	            ReadOnlyJWTClaimsSet idClaims = idToken.getJWTClaimsSet();
	            logger.info(() -> "got id claims: " +  Objects.toString(idClaims) );
	            return idClaims;
	        }
	        catch ( ParseException ex) {
	            throw new RuntimeException(ex);
	        }
   }        


    private final ObjectMapper mapper= new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Object retrieveUserProfile(String authToken) throws IOException {
	        HttpGet httpGet = new HttpGet(oidcClientCfg.providerCfg().userinfoURL());
	        httpGet.addHeader("Authorization", "Bearer " + authToken);

	        try ( CloseableHttpClient httpClient= HttpClients.createDefault();
	              CloseableHttpResponse response= httpClient.execute(httpGet) ) 
	        {
	            int responseStatus= response.getStatusLine().getStatusCode();
	            logger.info("UserInfo response status code: " + responseStatus );
	            if (responseStatus == 200)  {
	                HttpEntity httpEntity= response.getEntity();
	                logger.info("UserInfo response content type: " + ContentType.getOrDefault(httpEntity));
	                //TODO: this is dubious
	                return mapper.readerFor(UserProfile.class).readValue(httpEntity.getContent());
	            }
	            else {
	                throw new OidcClientModule.OidcAuthError("Waiting for HTTP 200 from the profile provider, got: " + responseStatus);
	            }
	        }
	        
    }

    @Override
	public void processOidcStart(HttpServletRequest htRequest, HttpServletResponse htResponse, String chosenProvider) {
	    
	    htRequest.getSession(true).setAttribute("OIDC_PROVIDER", chosenProvider);
        
        URI authURI= buildProviderLoginURI( htRequest );
        logger.info("Redirecting to: "+authURI);
        try {
            htResponse.sendRedirect(authURI.toString());
        }
        catch (IOException ioEx) {
            throw new RuntimeException(ioEx);
        }
	}

    private FilterConfig cfg;
    private OidcClientConfiguration oidcClientCfg;
    private static final SecureRandom sRandom= new SecureRandom();
    
    private static Logger logger= Logger.getLogger(CcozianuDevModuleImpl.class.getName());

    public OidcSimpleImpl( FilterConfig cfg_,
                     OidcClientConfiguration oidcClientCfg_ ) 
    {
        this.cfg= cfg_;
        this.oidcClientCfg= oidcClientCfg_;
    }

    private URI buildCallbackURI( HttpServletRequest req) throws URISyntaxException {
        return new URIBuilder()
                .setScheme(req.getScheme())
                .setHost(req.getServerName())
                .setPort(req.getServerPort())
                .setPath(req.getContextPath() +  oidcClientCfg.providerCfg().oidClientRedirectURL())
                .build();        
    }
    
    private URI buildProviderLoginURI(HttpServletRequest req) {
        try {
            
            return new URIBuilder( oidcClientCfg.providerCfg().oidProviderBaseURL())
                                .addParameter( OidcNames.OIDC_PNAME_RESPONSE_TYPE, 
                                               OidcNames.OIDC_VALUE_CODE)
                                .addParameter( OidcNames.OIDC_PNAME_CLIENT_ID ,
                                               oidcClientCfg.providerCfg().oidClientId() )
                                .addParameter( OidcNames.OIDC_PNAME_CALLBACK_URL, 
                                               buildCallbackURI(req).toString())
                                .addParameter( OidcNames.OIDC_PNAME_STATE, 
                                               stateValue())
                                .addParameter( OidcNames.OIDC_PNAME_NONCE ,
                                               nonceValue())
                                .addParameter(OidcNames.OIDC_PNAME_SCOPE, "openid profile email")
                            .build();
            }
            catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
    }


    private String nonceValue() { return "NONCE-" + Long.toHexString(sRandom.nextLong()); }
    


    private String stateValue() { return "STATE-" + Long.toHexString(sRandom.nextLong()); }


    public static final long OIDC_LIMIT_MAX_RESPONSE_LENGTH = 204800;


    /**
     * This class encapsulates what we know about OpenID protocol
     */
    public static class OidcNames {
     


        /**
         * redirect to authorization endpoints
         * MUST have response_type=code,
         * the client asks that the answe should contain a code, to be used for furhter
         * authentication/authorization interactions
         */
        public static final String OIDC_PNAME_RESPONSE_TYPE = "response_type";
        /**
         * @see OIDC_PNAME_RESONSE_TYPE
         */
        public static final String OIDC_VALUE_CODE = "code";
        
        public static final String OIDC_PNAME_CLIENT_ID = "client_id";
        public static final String OIDC_PNAME_CALLBACK_URL = "redirect_uri";
        public static final String OIDC_PNAME_STATE = "state";
        public static final String OIDC_PNAME_NONCE = "nonce";
        
        /**
         * URL parameter name where authorization name will be passed, typically in the callback URL
         * The client can take this code and use it to get more details, for example from the token server
         * and user info server
         */
        public static final String OIDC_PNAME_AUTHCODE="code";
        
        public static final String OIDC_PNAME_SCOPE = "scope";

        //TODO: maybe make it configurable
        /**
         * Our oidc client will abort if an http response from oidc provider
         * is more than 200K, to prevent DOS whether intentional or from buggy software
         */
      


    }

}