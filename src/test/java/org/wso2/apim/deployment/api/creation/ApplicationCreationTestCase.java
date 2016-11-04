package org.wso2.apim.deployment.api.creation;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.apim.bean.APIThrottlingTier;
import org.wso2.apim.clients.APIPublisherRestClient;
import org.wso2.apim.clients.APIStoreRestClient;
import org.wso2.apim.deployment.base.APIMBaseTest;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

/**
 * Created by yasassri on 11/2/16.
 */
public class ApplicationCreationTestCase extends APIMBaseTest {
    private APIPublisherRestClient apiPublisher;
    private APIStoreRestClient apiStore;
    private String appName = "sample-application1";

    @Factory(dataProvider = "userModeDataProvider")
    public ApplicationCreationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment(ITestContext ctx) throws Exception {
        super.init(userMode);
        //String publisherURLHttp = getPublisherURLHttp();
        //String storeURLHttp = getStoreURLHttp();

        apiStore = new APIStoreRestClient(storeURL);
        apiPublisher = new APIPublisherRestClient(publisherURL);

        apiPublisher.login("admin", "admin");
        apiStore.login("admin", "admin");

    }


    @Test(groups = {"wso2.am"}, description = "Sample Application Creation")
    public void testApplicationCreation() throws Exception {
        HttpResponse serviceResponse = apiStore.addApplication(appName, APIThrottlingTier.UNLIMITED.getState(), "", "this-is-test");
        verifyResponse(serviceResponse);
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN},
                //new Object[]{TestUserMode.TENANT_ADMIN},
        };
    }
}
