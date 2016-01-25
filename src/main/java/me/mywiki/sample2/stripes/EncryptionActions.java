package me.mywiki.sample2.stripes;

import java.util.function.Function;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * A very simple calculator action.
 * @author Tim Fennell
 */
@UrlBinding("/open/actions/testEncryption")
public class EncryptionActions implements ActionBean {
    
    private ActionBeanContext context;

    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { 
        this.context = context; 
    }

    @Validate(required=true) private String textToEncrypt;
    public String getNumberOne() { return textToEncrypt; }
    public void setNumberOne(String textToEncrypt_) { this.textToEncrypt = textToEncrypt_; }

    private Function<String, String> encryptionFunction;

    private String result;
    public String getResult() { return result; }

    /** An event handler method that adds number one to number two. */
    @DefaultHandler
    public Resolution doEncrypt() {
        
        result = this.encryptionFunction.apply(textToEncrypt);
        return new ForwardResolution("/open/stripes/testEncryption.jsp");
        
    }


}
