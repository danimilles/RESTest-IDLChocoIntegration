package es.us.isa.restest.testcases.restassured.filters;

//import io.restassured.filter.Filter;
import es.us.isa.restest.testcases.TestResult;
import es.us.isa.restest.util.PropertyManager;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import static es.us.isa.restest.coverage.CoverageMeter.exportCoverageOfTestResultToCSV;

public class CoverageFilter implements OrderedFilter {

    private String APIName;
    private String testResultId;

    public CoverageFilter() {
        super();
    }

    public CoverageFilter(String testResultId, String APIName) {
        super();
        this.testResultId = testResultId;
        this.APIName = APIName;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);

        // Export output coverage data after receiving API response
        String coverageDataFile = PropertyManager.readProperty("data.coverage.dir") + "/" + APIName + "/" + PropertyManager.readProperty("data.coverage.testresults.file");
        TestResult tr = new TestResult(testResultId, Integer.toString(response.statusCode()), response.asString(), response.contentType());
        exportCoverageOfTestResultToCSV(coverageDataFile, tr);

        return response;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // Lowest priority of all filters, so it runs last before sending the request and first after sending it
    }
}
