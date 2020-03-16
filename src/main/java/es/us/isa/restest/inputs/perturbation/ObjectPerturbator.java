package es.us.isa.restest.inputs.perturbation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.us.isa.jsonmutator.JsonMutator;
import es.us.isa.restest.inputs.ITestDataGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class leverages the {@link es.us.isa.jsonmutator.JsonMutator} to perturb
 * an original, valid JSON object (generally used as input for an API operation)
 * and transform it into another JSON object (possibly invalid, but not guaranteed).
 * For the moment, only single-order perturbations are applied, i.e., only one
 * mutation is applied at a time.
 *
 * @author Alberto Martin-Lopez
 */
public class ObjectPerturbator implements ITestDataGenerator {

    private List<JsonNode> originalObjects;
    private JsonMutator jsonMutator;
    private ObjectMapper objectMapper;
    private Boolean singleOrder = true; // True if single order mutation, false otherwise

    public ObjectPerturbator() {
        this.objectMapper = new ObjectMapper();
        this.jsonMutator = new JsonMutator();
        this.originalObjects = new ArrayList<>();
    }

    public ObjectPerturbator(JsonNode originalObject) {
        this();
        List<JsonNode> originalObjects = new ArrayList<>();
        originalObjects.add(originalObject);
        this.originalObjects = originalObjects;
    }

    public ObjectPerturbator(Object originalObject) {
        this();
        JsonNode jsonObject = objectMapper.valueToTree(originalObject);
        List<JsonNode> originalObjects = new ArrayList<>();
        originalObjects.add(jsonObject);
        this.originalObjects = originalObjects;
    }

    public ObjectPerturbator(String originalObject) {
        this();
        try {
            JsonNode jsonObject = objectMapper.readTree(originalObject);
            List<JsonNode> originalObjects = new ArrayList<>();
            originalObjects.add(jsonObject);
            this.originalObjects = originalObjects;
        } catch (IOException e) {
            System.err.println("An error occurred when deserializing JSON:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public ObjectPerturbator(List<String> stringObjects) {
        this();
        List<JsonNode> originalObjects = new ArrayList<>();
        try {
            for(String stringObject : stringObjects) {
                JsonNode jsonObject = objectMapper.readTree(stringObject);
                originalObjects.add(jsonObject);
            }
        } catch (IOException e) {
            System.err.println("An error occurred when deserializing JSON:");
            e.printStackTrace();
            System.exit(1);
        }
        this.originalObjects = originalObjects;
    }

    @Override
    public JsonNode nextValue() {
        Random random = new Random();
        int index = random.nextInt(originalObjects.size());
        JsonNode beforeMutating = originalObjects.get(index).deepCopy();
        JsonNode afterMutating = jsonMutator.mutateJson(originalObjects.get(index), singleOrder);
        originalObjects.set(index, beforeMutating);
        return afterMutating;
    }

    @Override
    public String nextValueAsString() {
        try {
            return objectMapper.writeValueAsString(nextValue());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<JsonNode> getOriginalObjects() {
        return originalObjects;
    }

    public List<String> getOriginalStringObjects() {
        List<String> stringObjects = new ArrayList<>();
        try {
            for(JsonNode originalObject : originalObjects) {
                stringObjects.add(objectMapper.writeValueAsString(originalObject));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return stringObjects;
    }

    public void addOriginalObject(JsonNode originalObject) {
        this.originalObjects.add(originalObject);
    }

    public void addOriginalObject(Object originalObject) {
        this.originalObjects.add(objectMapper.valueToTree(originalObject));
    }

    public void addOriginalObject(String originalObject) {
        try {
            this.originalObjects.add(objectMapper.readTree(originalObject));
        } catch (IOException e) {
            System.err.println("An error occurred when deserializing JSON:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Boolean getSingleOrder() {
        return singleOrder;
    }

    public void setSingleOrder(Boolean singleOrder) {
        this.singleOrder = singleOrder;
    }

    public void setOriginalObjects(List<String> stringObjects) {
        List<JsonNode> originalObjects = new ArrayList<>();
        try {
            for(String stringObject : stringObjects) {
                JsonNode jsonObject = objectMapper.readTree(stringObject);
                originalObjects.add(jsonObject);
            }
        } catch (IOException e) {
            System.err.println("An error occurred when deserializing JSON:");
            e.printStackTrace();
            System.exit(1);
        }
        this.originalObjects = originalObjects;
    }
}
