package me.mywiki.sample2.oidc.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;

/**
 * This class encapsulates what we know about OpenID protocol
 */
public class OpenIDProtocol {
 
    /**
     * defines the parameters to be used as a clien
     * of an open id provider endpoint.
     * This can either be known and configured ahead of time,
     * or discovered dynamically at an know URL such as 
     * {endpoint_url}/.wellknown/configuration
     */
    public static interface OpenIDEndpointConfig {
        String tokenBaseURL();
    }

    static final Logger logger = Logger.getLogger(OpenIDProtocol.class.getName());

    /**
     * redirect to authorization endpoints
     * MUST have response_type=code
     */
    public static final String AUTH_PARAM_RESPONSE_TYPE = "response_type";
    public static final String RESPONSE_TYPE_CODE = "code";
    
    public static final String AUTH_PARAM_CLIENT_ID = "client_id";
    public static final String AUTH_PARAM_CALLBACK_URL = "redirect_uri";
    public static final String AUTH_PARAM_STATE = "state";
    public static final String AUTH_PARAM_NONCE = "nonce";
    
    public static final String AUTHCB_PARAM_CODE="code";
    
    public static final String AUTH_PARAM_SCOPE = "scope";

    public static final long MAX_RESPONSE_LENGTH = 204800;

 


    static ReadOnlyJWTClaimsSet 
        validateIdToken( String idTokenRaw, OpenIDEndpointConfig endPointCfg) 
                         throws ParseException
    {
            JWT idToken= JWTParser.parse(idTokenRaw);
            logger.info( () ->  "got jwt token: " +  Objects.toString(idToken) );
            ReadOnlyJWTClaimsSet idClaims = idToken.getJWTClaimsSet();
            //TODO: validate claims
            return idClaims;
    }

    
    /**
     * 
     * @param redirectRequest
     * @param endPointCfg
     * @param authObjectSessionName
     */
    public static boolean extractOidcAndAuthenticate( HttpServletRequest redirectRequest,
                                                      OpenIDEndpointConfig endPointCfg,
                                                      String localRedirectCallbackURI,
                                                      String authObjectSessionName ) 
                                                     throws ClientProtocolException, IOException
    {
        String codeVal= redirectRequest.getParameter(AUTHCB_PARAM_CODE);
        if (StringUtils.isEmpty(codeVal)) {
            logger.info("Authorizationr redirect missing code");
            return false;
        }
        
        logger.info("Trying to obtain auth token for code:"+codeVal);
        // 
    
        try (CloseableHttpClient httpClient= HttpClients.createDefault()) 
        {
             HttpPost httpPost = new HttpPost(endPointCfg.tokenBaseURL());
            
             List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("grant_type", "authorization_code"));
                formparams.add(new BasicNameValuePair("code", codeVal ));
                formparams.add(new BasicNameValuePair("redirect_uri", localRedirectCallbackURI));
                UrlEncodedFormEntity 
                    submitForm = new UrlEncodedFormEntity( formparams, 
                                                           StandardCharsets.UTF_8);
                httpPost.setEntity(submitForm);
                
                /*httpPost.addHeader( "Authorization", 
                                    String.format( "Basic %s", 
                                                   Base64.getUrlEncoder()
                                                         .encodeToString(
                                                                 String.format( "%s:%s", 
                                                                                 SampleOpenIDAuthFilter.AUTH_CLIENT_ID, 
                                                                                 SampleOpenIDAuthFilter.AUTH_CLIENT_SECRET)
                                                                       .getBytes(StandardCharsets.UTF_8)
                                                                    )));*/
                logger.info("Client Authorization:" +httpPost.getFirstHeader("Authorization"));
                //TODO: replace wrap this in a proper retry
                logger.info("Executing request: " + httpPost.getRequestLine() + " to target " + endPointCfg.tokenBaseURL());
                
                try (CloseableHttpResponse response = httpClient.execute(httpPost) ) {
                    logger.info("Response status: "+response.getStatusLine());
                    logger.info("Response headers: "+ Arrays.asList( response.getAllHeaders()));
                    HttpEntity entity= response.getEntity();
                    long contentLength = entity != null 
                                            ? entity.getContentLength()
                                            : 0;
                    logger.info("Content length is: "+contentLength);                        
                    if ( contentLength < MAX_RESPONSE_LENGTH) 
                    {
                       String strResponse= EntityUtils.toString(entity);
                       ObjectMapper oMapper= new ObjectMapper();
                       Map<?,?> jsonResponse = oMapper.readValue( strResponse
                                                                 , Map.class);
                       String authTokenRaw= null; 
                       String idTokenRaw= null; 
                       ReadOnlyJWTClaimsSet idClaims= null;
                       String refreshTokenRaw= null;
                       
                       if (jsonResponse.containsKey("access_token")) {
                           authTokenRaw= (String) jsonResponse.get("access_token");
                       }
                       if (jsonResponse.containsKey("id_token") ) {
                           idTokenRaw= (String) jsonResponse.get("id_token");
                           idClaims= validateIdToken(idTokenRaw, null);
                       }
                       if (jsonResponse.containsKey("refresh_token") ) {
                           refreshTokenRaw= (String) jsonResponse.get("refresh_token");
                       }
                       
                       redirectRequest.getSession(true).setAttribute( authObjectSessionName, 
                                                                      idClaims);
                       
                       logger.info("Token endpoint response:\n"+ jsonResponse);
                       logger.info("Extracted access_token:"+authTokenRaw);
                       logger.info("Extracted id_token: " +idTokenRaw);
                       return true;
                    }
                }
 
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex, () -> "Exception thrown trying to get the tokens");
        }
        
        return false;
    }
}
