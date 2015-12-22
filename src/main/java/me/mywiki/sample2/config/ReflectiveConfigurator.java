package me.mywiki.sample2.config;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import java.lang.UnsupportedOperationException;

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
                
                assertTrue("builder should be an interface", 
                            builderClass_.isInterface());
                
                Set<String> builderPropNames= new HashSet<>();
                
                for (Method m: builderClass_.getDeclaredMethods()) {
                    String mName= m.getName();
                    if (mName.equals("done")) {
                        assertTrue("done is a method with 0 paramters",  0 == m.getParameterCount());
                        assertEquals("done retrusn the reade",readerClass_,m.getReturnType());
                        continue;
                    }
                    // all other methods are setter of form Builder propertyName(PropType val);
                    assertTrue("setter method: "+mName, 1 == m.getParameterCount());
                    assertTrue("returning a builder for"+mName, builderClass_.equals(m.getReturnType()));
                    builderPropNames.add(mName);
                }

                assertTrue( "builder should be an interface", 
                            builderClass_.isInterface());
                
                Set <String> readerPropNames=  new HashSet<String>();
                for (Method m: readerClass_.getMethods()) {
                    String mName= m.getName();
                    if (mName.equals("cloneBuilder")) {
                        assertTrue("done is a method with 0 paramters",  0 == m.getParameterCount());
                        assertEquals("done returns the reades",builderClass_,m.getReturnType());
                        continue;
                    }
                    // all other methods are setter of form Builder propertyName(PropType val);
                    assertEquals("getter method has 0 params "+mName, 0, m.getParameterCount());
                    readerPropNames.add(mName);
                }
                
                assertEquals("Rwader properties match builder properties",readerPropNames, builderPropNames);
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
