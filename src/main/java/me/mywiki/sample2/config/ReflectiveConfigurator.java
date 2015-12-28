package me.mywiki.sample2.config;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;

//import static org.junit.Assert.*;

/**
 * A configuration is a map from name to values,
 * as such the contract for configuration is to map
 * what names are associated with each values
 * and provide a convenient mechanism for instantiating full maps
 */
public class ReflectiveConfigurator {


        
     
        
        public static <Reader, Builder> 
            Builder configBuilderFor( Class<Reader>  readerClass, 
                                      Class<Builder> builderClass ) 
        {
             return new  ReflectiveBuilderImpl <Reader, Builder>( readerClass, builderClass)
                                        .makeBuilder();
        }
        
        public static class MissingPropertyException extends RuntimeException 
        {
            private static final long serialVersionUID = 1L;

            public MissingPropertyException() {
                super();
            }

            public MissingPropertyException(String message, Throwable cause) {
                super(message, cause);
            }

            public MissingPropertyException(String message) {
                super(message);
             }

            public MissingPropertyException(Throwable cause) {
                super(cause);
             }
            
        }


        private static class ReflectiveBuilderImpl<Reader, Builder>
        {
     
            final Class<Builder> builderClass;
            final Class<Reader>  readerClass;
            
            //final Map<String,Object> valueMap= new HashMap<>();
            final Set<String> propNames;

            ReflectiveBuilderImpl( Class<Reader> readerClass_, 
                                          Class<Builder> builderClass_) 
            {
                this.builderClass= builderClass_;
                this.readerClass= readerClass_;
                this.propNames= checkAgainstSpec(readerClass, builderClass);
            }
            
            private static 
                Set<String> checkAgainstSpec( Class<?> readerClass_,
                                              Class<?> builderClass_) 
            {
                Preconditions.checkArgument(builderClass_.isInterface(), "builder should be an interface"); 
                
                Set<String> builderPropNames= new HashSet<>();
                
                for (Method m: builderClass_.getDeclaredMethods()) {
                    String mName= m.getName();
                    if (mName.equals("done")) {
                        Preconditions.checkArgument(  0 == m.getParameterCount(),"done is a method with 0 paramters");
                        Preconditions.checkArgument(m.getReturnType().equals(readerClass_), "done returns the reader object");
                        continue;
                    }
                    // all other methods are setter of form Builder propertyName(PropType val);
                    Preconditions.checkArgument(1 == m.getParameterCount(), "setter method: "+mName );
                    Preconditions.checkArgument(builderClass_.equals(m.getReturnType()), "returning a builder for"+mName );
                    builderPropNames.add(mName);
                }

                
                Set <String> readerPropNames=  new HashSet<String>();
                for (Method m: readerClass_.getMethods()) {
                    String mName= m.getName();
                    if (mName.equals("cloneBuilder")) {
                        Preconditions.checkArgument(  0 == m.getParameterCount(), "cloneBuilder is a method with 0 paramters" );
                        Preconditions.checkArgument( m.getReturnType().equals(builderClass_), "cloneBuilder returns the builder");
                        continue;
                    }
                    // all other methods are setter of form Builder propertyName(PropType val);
                    Preconditions.checkArgument( 0== m.getParameterCount() ,"getter method has 0 params "+mName );
                    readerPropNames.add(mName);
                }
                
                Preconditions.checkArgument( readerPropNames.equals(builderPropNames), "Reader properties match builder properties");
                return readerPropNames;
            }

            @SuppressWarnings("unchecked")
            public  Builder makeBuilder() 
            {
                return (Builder) 
                        Proxy.newProxyInstance(  this.getClass().getClassLoader(), 
                                                 new Class<?> [] {builderClass}, 
                                                 new ConfigBuilderHandler());
            }

            @SuppressWarnings("unchecked")
            public  Builder makeBuilder(Map<String,Object> initialValues) 
            {
                return (Builder) 
                        Proxy.newProxyInstance(  this.getClass().getClassLoader(), 
                                                 new Class<?> [] {builderClass}, 
                                                 new ConfigBuilderHandler(initialValues));
            }
     
            @SuppressWarnings("unchecked")
            public Reader buildTheReader(Map<String, Object> valueMap) 
            {
                //check that all properties are assigned
                if (valueMap.keySet().equals(propNames))
                    return (Reader)
                            Proxy.newProxyInstance(  this.getClass().getClassLoader(), 
                                                     new Class<?> [] { readerClass }, 
                                                     new ConfigReaderHandler( valueMap) );
                else {
                    //TODO: supply a list of what is missing
                    throw new MissingPropertyException();
                }
                    
            }
            
            private class ConfigBuilderHandler implements InvocationHandler {
                final Map<String,Object> valueMap;
                
                public ConfigBuilderHandler() {
                    this.valueMap= new HashMap<>();
                }
                
                public ConfigBuilderHandler(Map<String, Object> initialValues) {
                    this.valueMap= new HashMap<>(initialValues);
                }

                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    String mName= method.getName();
                    if (mName.equals("done")) {
                        return buildTheReader(this.valueMap);
                    }
                    // here we assume that all the other methods have the shape
                    // XXXBuilder propertyName( PropertyType val_)
                    // because the builder constructor enforces this condition
                    if (args.length != 1 ) {
                        throw new IllegalStateException("Expecting propety setter, of type XXXBuilder propertyName( PropertyType val_)");
                    }
                    valueMap.put(mName,args[0]);
                    return proxy;
                }
            }
            
            public class ConfigReaderHandler implements InvocationHandler {
                
                final Map<String,Object>myValueMap;

                public ConfigReaderHandler(Map<String, Object> valueMap) {
                    // copy the input to avoid side effects
                    this.myValueMap= new HashMap<String, Object>(valueMap);
                }

                @Override
                public Object invoke ( Object proxy, 
                                       Method m, 
                                       Object[] args)
                        throws Throwable {
                    String mName= m.getName();
                    if (mName.equals("cloneBuilder")) {
                        return makeBuilder(this.myValueMap);
                    }
                    else if(mName.equals("toString") && args == null) {
                        return myValueMap.toString();
                    }
                    // accessor method
                    if (myValueMap.containsKey(mName)) {
                        return myValueMap.get(mName);
                    }
                    else {
                        throw new IllegalStateException("Value not supplied for property: "+mName);
                    }
                        
                }

            }
        }

}
