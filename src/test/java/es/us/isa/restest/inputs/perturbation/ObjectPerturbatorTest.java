package es.us.isa.restest.inputs.perturbation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.util.Json;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectPerturbatorTest {

    @Test
    public void testConstructorWithoutArguments() {
        ObjectPerturbator objectPerturbator = new ObjectPerturbator();
        assertNull("The originalObjects list should be null", objectPerturbator.getOriginalObjects());
    }

    @Test
    public void testConstructorWithArgumentJsonNode() {
        JsonNode originalObject = new ObjectNode(null);
        ObjectPerturbator objectPerturbator = new ObjectPerturbator(originalObject);
        assertNotNull("The originalObjects list should not be null", objectPerturbator.getOriginalObjects());
        assertEquals("The original object should be '{}'", "{}", objectPerturbator.getOriginalStringObjects().get(0));
    }

    @Test
    public void testConstructorWithArgumentString() {
        String originalObject = "{}";
        ObjectPerturbator objectPerturbator = new ObjectPerturbator(originalObject);
        assertNotNull("The originalObjects list should not be null", objectPerturbator.getOriginalObjects());
        assertEquals("The original object should be '{}'", "{}", objectPerturbator.getOriginalStringObjects().get(0));
    }

    @Test
    public void testConstructorWithMultipleArguments() {
        String[] originalObjects = {"{}", "[]", "{\"prop1\": \"val1\", \"prop2\": [1, true, {}]}"};
        ObjectPerturbator objectPerturbator = new ObjectPerturbator(Arrays.asList(originalObjects));
        assertNotNull("The originalObjects list should not be null", objectPerturbator.getOriginalObjects());
        for(String object : objectPerturbator.getOriginalStringObjects()) {
            boolean b = object.equals("{}") || object.equals("[]") || object.equals("{\"prop1\":\"val1\",\"prop2\":[1,true,{}]}");
            assertTrue("The original objects should be '{}', '[]' and '{\"prop1\": \"val1\", \"prop2\": [1, true, {}]}'", b);
        }
    }

    @Test
    public void testNextValue() {
        String originalObject = "[]";
        ObjectPerturbator objectPerturbator = new ObjectPerturbator(originalObject);
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
        assertNotEquals(objectPerturbator.getOriginalObjects().get(0), objectPerturbator.nextValue());
    }

    @Test
    public void testNextValueMultipleValues() {
        String[] originalObjects = {"{\"prop1\": \"hello\"}", "{\"prop1\": \"val1\", \"prop2\": [1, true, {\"prop1_1\": 50}], \"prop3\": \"false\"}",
                "{\"prop1\": \"val1\", \"prop2\": [1, true, {}]}"};
        ObjectPerturbator objectPerturbator = new ObjectPerturbator(Arrays.asList(originalObjects));
        for(int i = 0; i < 10; i++) {
            JsonNode nextValue = objectPerturbator.nextValue();
            assertTrue(!objectPerturbator.getOriginalObjects().contains(nextValue));
        }

    }

    @Test
    public void testNextValueAsString() {
        String originalObject = "{\"prop1\": \"val1\", \"prop2\": [1, true, {}]}";
        ObjectPerturbator objectPerturbator = new ObjectPerturbator(originalObject);
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
        assertNotEquals(objectPerturbator.getOriginalStringObjects().get(0), objectPerturbator.nextValueAsString());
    }

    @Test
    public void testNextValueAsStringMultipleValues() {
        String[] originalObjects = {"{\"prop1\": \"hello\"}", "{\"prop1\": \"val1\", \"prop2\": [1, true, {\"prop1_1\": 50}], \"prop3\": \"false\"}",
                "{\"prop1\": \"val1\", \"prop2\": [1, true, {}]}"};
        ObjectPerturbator objectPerturbator = new ObjectPerturbator(Arrays.asList(originalObjects));
        for(int i = 0; i < 10; i++) {
            String nextValue = objectPerturbator.nextValueAsString();
            assertTrue(!objectPerturbator.getOriginalStringObjects().contains(nextValue));
        }

    }


}
