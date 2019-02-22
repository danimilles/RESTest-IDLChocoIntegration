package es.us.isa.rester.postmanparser;

import es.us.isa.rester.specification.OpenAPISpecification;
import es.us.isa.rester.util.JSONManager;

public class PostmanParser {

    private OpenAPISpecification spec;  // OpenAPI specification to deduce coverage levels from
    private String postmanPath;         // Path to the file (JSON) containing the Postman collection 
    private Object postmanCollection;   // Object containing the information parsed from the JSON file

    public PostmanParser(OpenAPISpecification spec, String postmanPath) {
        this.spec = spec;
        this.postmanPath = postmanPath;
        this.postmanCollection = JSONManager.readJson(postmanPath);
    }

    public OpenAPISpecification getSpec() {
        return this.spec;
    }

    public void setSpec(OpenAPISpecification spec) {
        this.spec = spec;
    }

    public String getPostmanPath() {
        return this.postmanPath;
    }

    public void setPostmanPath(String postmanPath) {
        this.postmanPath = postmanPath;
    }

    public Object getPostmanCollection() {
        return this.postmanCollection;
    }

    public void setPostmanCollection(Object postmanCollection) {
        this.postmanCollection = postmanCollection;
    }
}