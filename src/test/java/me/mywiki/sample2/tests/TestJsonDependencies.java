package me.mywiki.sample2.tests;

import static org.junit.Assert.*;

import java.io.StringReader;

import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.mywiki.sample2.oidc.UserProfile;

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

    @Test
    public void canReadUserProfile() throws Exception {
        String userProfile= IOUtils.toString(TestJsonDependencies.class.getResource("TestProfile.json"));
        ObjectMapper mapper= new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        UserProfile user= mapper.readerFor(UserProfile.class).readValue(userProfile);
    }
}
