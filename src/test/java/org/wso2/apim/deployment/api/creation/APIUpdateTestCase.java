package org.wso2.apim.deployment.api.creation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.apim.bean.APIRequest;
import org.wso2.apim.clients.APIPublisherRestClient;
import org.wso2.apim.clients.APIStoreRestClient;
import org.wso2.apim.deployment.base.APIMBaseTest;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;

/*
* This class test updating API context of an available API
* */

public class APIUpdateTestCase extends APIMBaseTest {
    private final Log log = LogFactory.getLog(APIUpdateTestCase.class);
    private APIPublisherRestClient apiPublisher;
    private APIStoreRestClient apiStore;
    private String apiName = "TestApi";
    private String apiContext = "testApi";
    private String updatedApiContext = "testApiUpdated";


    @Factory(dataProvider = "userModeDataProvider")
    public APIUpdateTestCase(TestUserMode userMode) {
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment(ITestContext ctx) throws Exception {

        apiStore = new APIStoreRestClient(storeURL);
        apiPublisher = new APIPublisherRestClient(publisherURL);

        apiPublisher.login("admin", "admin");
        apiStore.login("admin", "admin");

    }

    @Test(groups = {"wso2.am"}, description = "Sample API updating")
    public void testAPIUpdating() throws Exception {

        //Adding New API
        String backendEndPoint = "http://wso2.com";
        APIRequest apiRequest = new APIRequest(apiName, apiContext, new URL(backendEndPoint));
        apiPublisher.addAPI(apiRequest);

        //Updating API context
        APIRequest apiRequestNew = new APIRequest(apiName, updatedApiContext,
                new URL(backendEndPoint));
        HttpResponse serviceResponseNew = apiPublisher.updateAPI(apiRequestNew);
        verifyResponse(serviceResponseNew);
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

}
