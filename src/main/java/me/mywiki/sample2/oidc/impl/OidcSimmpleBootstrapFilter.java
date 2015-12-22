package me.mywiki.sample2.oidc.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;

import me.mywiki.sample2.oidc.OidcClientModule;
import me.mywiki.sample2.oidc.OidcClientModule.OidcContext;

public class OidcSimmpleBootstrapFilter implements Filter {

    public static final String OIDC_CONFIG_KEY="OidcSimpleClientConfig";
    private OidcClientModule oidcClientModule;
    
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            Class<? extends OidcClientModule> clazz= readFromJ2eeConfig( filterConfig);
            this.oidcClientModule= clazz.newInstance();
        }
        catch (Exception ex) {
            if (ex instanceof RuntimeException) { throw (RuntimeException) ex; }
            else                                { throw new RuntimeException(ex); }
        }
    }
    
    @Override
    public void destroy() {
        if (oidcClientModule != null) {
            // TODO: implement closing protocole
        }
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends OidcClientModule> readFromJ2eeConfig(FilterConfig filterConfig) 
    {
        try {
            String jsonText= filterConfig.getInitParameter(OIDC_CONFIG_KEY);
            if (jsonText == null ) { 
                throw new RuntimeException("Please provide OIDC configuration under: "+ OIDC_CONFIG_KEY);
            }
            ObjectMapper mapper= new ObjectMapper();
            JsonNode json= mapper.readValue(jsonText,JsonNode.class);
            switch ( json.getNodeType() ) {
            case OBJECT:
                         String className= json.get("oidcModuleClass").asText();
                         Verify.verifyNotNull(className, "json should contain the classname under: oidcModuleClass, it doesnt %1s", jsonText);
                         return (Class<? extends OidcClientModule>) Class.forName(className);
                         
                default:
                    throw new IllegalArgumentException("Invalid json configuration: "+jsonText);
            }
            
        }
        catch (Exception ex) {
            if (ex instanceof RuntimeException) { throw (RuntimeException)ex; }
                                           else { throw new RuntimeException(ex); }       
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException 
    {
        
        chain.doFilter(request, response);
    }
}
