package me.mywiki.sample2.oidc.impl;




/**
 * This class encapsulates what we know about OpenID protocol
 */
public class OpenIDProtocol {
 


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
    public static final long OIDC_LIMIT_MAX_RESPONSE_LENGTH = 204800;

 


}
