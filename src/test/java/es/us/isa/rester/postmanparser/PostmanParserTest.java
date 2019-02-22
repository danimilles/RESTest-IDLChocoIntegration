package es.us.isa.rester.postmanparser;

import static org.junit.Assert.*;

import org.junit.Test;

import es.us.isa.rester.specification.OpenAPISpecification;

public class PostmanParserTest {

    @Test
    public void debugging() {
        String oasPath = "src/test/resources/specifications/petstore.json";
        OpenAPISpecification oas = new OpenAPISpecification(oasPath);
        String postmanPath = "src/test/resources/postman/RESTest.postman_collection.json";
        PostmanParser postmanParser = new PostmanParser(oas, postmanPath);

        System.out.println(postmanParser.getPostmanCollection());

    }
}