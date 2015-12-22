package me.mywiki.sample2.tests;

import static org.junit.Assert.*;

import java.io.StringReader;



import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This test will fail if we haven't gotten our JSON dependencies 
 * right in Maven
 */
public class TestJsonDependencies {

    @Test
    public void test() throws Exception { 
        ObjectMapper mapper= new ObjectMapper();
        JsonNode node= mapper.readValue("{ \"ala\":\"bala\" , \"protocala\":true, \"x\":[ 0,1,2,3] }" , JsonNode.class);
        System.err.println("Successfully parsed: "+ node);
    }

}
