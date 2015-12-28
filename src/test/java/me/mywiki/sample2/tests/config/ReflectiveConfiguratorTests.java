package me.mywiki.sample2.tests.config;

import static org.junit.Assert.*;

import static org.hamcrest.core.Is.*;
import org.junit.Test;

import me.mywiki.sample2.config.ReflectiveConfigurator;

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

}
