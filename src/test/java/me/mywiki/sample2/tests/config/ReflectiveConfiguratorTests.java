package me.mywiki.sample2.tests.config;

import static org.junit.Assert.*;

import java.util.function.Function;

import static org.hamcrest.core.Is.*;
import org.junit.Test;

import me.mywiki.sample2.config.ReflectiveConfigurator;
import me.mywiki.sample2.config.ReflectiveConfigurator.DecodedBy;

public class ReflectiveConfiguratorTests {

    public static interface Configuration1 {
        public String property1();
        
        public int property2();
        
        public Builder cloneBuilder();
        
        public static interface Builder {
            public Builder property1(String v);
            public Builder property2(int v);
            public Configuration1 done();
        }
    }
    
    @Test
    public void testCase1() {
        Configuration1 config11= ReflectiveConfigurator.configBuilderFor( Configuration1.class, Configuration1.Builder.class)
                                .property1("value1")
                                .property2(5)
                                .done();
        assertThat(config11.property1(), is("value1"));
        assertThat(config11.property2(), is(5));
        
        Configuration1 config12= config11.cloneBuilder()
                                         .property2(3)
                                         .done();
        
        assertThat(config12.property1(), is("value1"));
        assertThat(config12.property2(), is(3));
    }

    public static interface Configuration2 {
        int intVal1();
        String strVal2();
        
        @DecodedBy(_fun = TestDecryptor.class)
        String encryptedVal2();
        
        static class TestDecryptor implements Function<Object, Object> {
            @Override
            public Object apply(Object t) {
                String blah= (String) t;
                return t+"." + t;
            }
        }
        
        public static interface Builder {
            Builder intVal1(int val_);
            Builder strVal2(String val_);
            
            Builder encryptedVal2( String val_);
            Configuration2 done();
        }
    }
    
    @Test
    public void testCase2() {
        Configuration2 config2= ReflectiveConfigurator.configBuilderFor( Configuration2.class, Configuration2.Builder.class)
                                .intVal1(1)
                                .strVal2("val2")
                                .encryptedVal2("val2")
                                .done();
        assertThat(config2.intVal1(), is(1));
        assertThat(config2.strVal2(), is("val2"));
        assertThat(config2.encryptedVal2(), is("val2.val2"));
        
    }

}
