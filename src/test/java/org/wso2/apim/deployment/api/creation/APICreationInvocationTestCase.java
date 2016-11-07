/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.apim.deployment.api.creation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.apim.bean.APILifeCycleState;
import org.wso2.apim.bean.APILifeCycleStateRequest;
import org.wso2.apim.bean.APIRequest;
import org.wso2.apim.bean.APIThrottlingTier;
import org.wso2.apim.bean.SubscriptionRequest;
import org.wso2.apim.clients.APIPublisherRestClient;
import org.wso2.apim.clients.APIStoreRestClient;
import org.wso2.apim.deployment.base.APIMBaseTest;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import java.net.URL;

public class APICreationInvocationTestCase extends APIMBaseTest {
    private final Log log = LogFactory.getLog(APICreationInvocationTestCase.class);
    private APIPublisherRestClient apiPublisher;
    private APIStoreRestClient apiStore;
    private String apiName = "TestSampleApi1";
    private String apiContext = "testSampleApi1";
    private String appName = "sample-application2";

    @Factory(dataProvider = "userModeDataProvider")
    public APICreationInvocationTestCase(TestUserMode userMode) {
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment(ITestContext ctx) throws Exception {

        apiStore = new APIStoreRestClient(storeURL);
        apiPublisher = new APIPublisherRestClient(publisherURL);

        apiPublisher.login("admin", "admin");
        apiStore.login("admin", "admin");

    }

    @Test(groups = {"wso2.am"}, description = "Sample API creation")
    public void testAPICreation() throws Exception {
        String backendEndPoint = "http://wso2.com";
        APIRequest apiRequest = new APIRequest(apiName, apiContext,
                                               new URL(backendEndPoint));
        HttpResponse serviceResponse = apiPublisher.addAPI(apiRequest);
        verifyResponse(serviceResponse);
    }

    @Test(groups = {"wso2.am"}, description = "Sample API Publishing", dependsOnMethods = "testAPICreation")
    public void testAPIPublishing() throws Exception {

        APILifeCycleStateRequest updateRequest =
                new APILifeCycleStateRequest(apiName, "admin",
                                             APILifeCycleState.PUBLISHED);
        HttpResponse serviceResponse = apiPublisher.changeAPILifeCycleStatus(updateRequest);
        Thread.sleep(10000);
        verifyResponse(serviceResponse);

    }

    @Test(groups = {"wso2.am"}, description = "Sample Application Creation", dependsOnMethods = "testAPICreation")
    public void testApplicationCreation() throws Exception {
        HttpResponse serviceResponse = apiStore.addApplication(appName, APIThrottlingTier.UNLIMITED.getState(), "", "this-is-test");
        verifyResponse(serviceResponse);

    }

    @Test(groups = {"wso2.am"}, description = "API Subscription", dependsOnMethods = "testApplicationCreation")
    public void testAPISubscription() throws Exception {

        String provider = "admin";

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(apiName, provider);
        subscriptionRequest.setApplicationName(appName);
        subscriptionRequest.setTier("Gold");
        HttpResponse serviceResponse = apiStore.subscribe(subscriptionRequest);
        verifyResponse(serviceResponse);

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
//        apiStore.removeApplication(appName);
//        super.cleanUp();
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN},
                //new Object[]{TestUserMode.TENANT_ADMIN},
        };
    }
}

