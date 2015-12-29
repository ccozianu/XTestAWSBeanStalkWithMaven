package me.mywiki.sample2.oidc.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;

import me.mywiki.sample2.oidc.OidcClientModule;
import me.mywiki.sample2.oidc.OidcClientModule.Err;
import me.mywiki.sample2.oidc.OidcClientModule.Err.ErrorData;
import me.mywiki.sample2.oidc.OidcClientModule.OidcRequestHandler;

public class OidcSimpleBootstrapFilter implements Filter {

    public static final String OIDC_FILTER_ERROR = "me.mywiki.sample2.oidc.ErrorData";
     
    private OidcClientModule oidcClientModule;
    private OidcRequestHandler oidcHandler;
    private String contextPath;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            Class<? extends OidcClientModule> clazz= readFromJ2eeConfig( filterConfig);
            this.oidcClientModule= clazz.newInstance();
            this.oidcHandler= oidcClientModule.initialize(filterConfig);
        }
        catch (Exception ex) {
            if (ex instanceof RuntimeException) { throw (RuntimeException) ex; }
            else                                { throw new RuntimeException(ex); }
        }
    }
    
    @Override
    public void destroy() {
        if (oidcClientModule != null) {
            // TODO: implement closing protocol
        }
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends OidcClientModule> readFromJ2eeConfig(FilterConfig filterConfig) 
    {
        try {
            
            String className= System.getProperty("me.mywiki.oidcModuleClass");
            if (className == null ) {
                className = System.getenv("me_mywiki_oidcmoduleclass");
            }
            if (className == null ) {
                className= filterConfig.getInitParameter("me.mywiki.oidcModuleClass");
            }
            Verify.verifyNotNull(className, "OIDC Client implementation class name not found in the environment or config for webapp");
                                             
            return (Class<? extends OidcClientModule>) Class.forName(className);
                         
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
        HttpServletRequest htReq= (HttpServletRequest) request;
        HttpServletResponse htResponse= (HttpServletResponse) response;
        if ( needsLogin(htReq)
             && ! isAlreadyLoggedIn(htReq)) 
        {
            //TODO: the Login form (both rendering and handling)
            // should be abstracted in its own module, for now we make simplifying assumptions
            
            if ( handleLoginFormSubmit(htReq, htResponse)) {
                return;
            }
            if (handleOidcCallbackRedirect(htReq, htResponse)) {
                return;
            }
            
            sendToAuthError( htReq, htResponse, OidcClientModule.Err.notAuthenticated(htReq,htResponse));
        
            return;
        }
        
        // if either the resource does not require auth
        // or the user is already authenticated pass the request down the chain
        chain.doFilter(request, response);
    }

    private boolean handleOidcCallbackRedirect( HttpServletRequest htReq, 
                                                HttpServletResponse htResponse)
                                                throws ServletException, IOException
    {

        if (! htReq.getServletPath().startsWith("/test2-openid-redirect")) {
            return false;
        }

        HttpSession session= htReq.getSession(false);
        if (session == null) {
                sendToAuthError(htReq, htResponse, Err.callBackWithoutSession());
                return true;
        }
            
        String oidcProviderName = (String) session.getAttribute("OIDC_PROVIDER");
        if ( oidcProviderName == null ) {
            sendToAuthError(htReq, htResponse, Err.callBackMissingProviderInSession());
            return true;
        }
        oidcHandler.processOidcCallback(htReq, htResponse, oidcProviderName);
        sendToDefaultPage(htReq, htResponse);
        return true;
       
    }

    private void sendToDefaultPage( HttpServletRequest htReq, 
                                    HttpServletResponse htResponse )
                                    throws IOException,ServletException
    {
        //TODO: configurize
        htReq.getRequestDispatcher("/index.jsp")
             .forward(htReq, htResponse);;
    }

    private boolean handleLoginFormSubmit( HttpServletRequest htRequest, 
                                           HttpServletResponse htResponse )
                        throws IOException, ServletException
    {
        if ( htRequest.getMethod().equals("POST") 
             && htRequest.getRequestURI().endsWith("action/login") )
         {
            String loginOption= htRequest.getParameter("loginOption");
            
            if (loginOption == null) {
                sendToAuthError(htRequest, htResponse, Err.errNoLoginOption());
            }
            else if ( loginOption.equals("Local") ) 
            {
                performLocalLogin(htRequest, htResponse);
            }
            else if (loginOption.startsWith("OpenIDConnect_")) 
            {
                oidcHandler.processOidcStart(htRequest, htResponse, loginOption.substring("openIDConnect_".length()));
                return true;
            }
            else {
                sendToAuthError( htRequest, htResponse, Err.invalidLoginOption( loginOption));
            }

         }
        return false;
    }



    private void performLocalLogin( HttpServletRequest htRequest, 
                                    HttpServletResponse htResponse) 
                                    throws ServletException, IOException
    {
        sendToAuthError( htRequest, htResponse, Err.notImplementedYet("local login"));
    }


    private boolean isAlreadyLoggedIn(HttpServletRequest htReq) {
        HttpSession session= htReq.getSession(false);
        if (session != null) {
            return session.getAttribute(OidcClientModule.DEFAULT_USERDATA_SESSION_NAME) != null ;
        }
        return false;
    }

    private boolean needsLogin(HttpServletRequest request) {
        //TODO: later refactor hard-coded paths to config driven
        String sPath=request.getServletPath();
        return ! ( sPath.startsWith("/open")
                    || sPath.startsWith("/error")
                    || sPath.startsWith("/login") );
    }
    
    private void sendToAuthError( HttpServletRequest htRequest,
                                  HttpServletResponse htResponse,
                                  ErrorData error) 
                                         throws IOException, ServletException
    {
        htRequest.setAttribute(OIDC_FILTER_ERROR, error);
        htRequest.getRequestDispatcher("/error.jsp")
                 .forward(htRequest, htResponse);
    }
}
