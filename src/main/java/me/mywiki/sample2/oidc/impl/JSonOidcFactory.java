package me.mywiki.sample2.oidc.impl;

import java.io.StringReader;

import javax.servlet.FilterConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.mywiki.sample2.oidc.OidcClientModule;

/**
 * The role of this is to instantiate an OidcClientMdule from json configuration
 */
public class  JSonOidcFactory {
    
    public OidcClientModule instantiateOidcModule( String json, FilterConfig containerConfig ) {
        return  new OidcClientImpl();
    }
}