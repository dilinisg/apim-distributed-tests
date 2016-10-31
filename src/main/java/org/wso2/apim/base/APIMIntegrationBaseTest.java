/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.apim.base;

import org.apache.axiom.om.OMElement;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.apim.bean.APIMURLBean;
import org.wso2.apim.clients.APIPublisherRestClient;
import org.wso2.apim.clients.APIStoreRestClient;
import org.wso2.apim.exception.APIManagerIntegrationTestException;
import org.wso2.carbon.automation.distributed.beans.InstanceUrls;
import org.wso2.carbon.automation.distributed.beans.Port;
import org.wso2.carbon.automation.distributed.commons.DeploymentConfigurationReader;
import org.wso2.carbon.automation.distributed.commons.DeploymentDataReader;
import org.wso2.carbon.automation.distributed.utills.ScriptExecutorUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.xpath.XPathExpressionException;


/**
 * Base class for all API Manager integration tests
 * Users need to extend this class to write integration tests.
 */
public class APIMIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(APIMIntegrationBaseTest.class);
    protected AutomationContext defaultContext, storeContext, publisherContext, keyManagerContext, gatewayContextMgt,
            gatewayContextWrk, backEndServer;
    protected OMElement synapseConfiguration;
    protected TestUserMode userMode;
    protected APIMURLBean defaultUrls, storeUrls, publisherUrls, gatewayUrlsMgt, gatewayUrlsWrk, keyMangerUrl, backEndServerUrl;
    protected User user;
    protected HashMap<String,String> instanceMap;

    protected String storeURL;
    protected String publisherURL;
    protected String keyManagerURL;
    protected String gateWayManagerURL;
    protected String gateWayWorkerURL;


    /**
     * This method will initialize test environment
     * based on user mode and configuration given at automation.xml
     *
     * @throws APIManagerIntegrationTestException - if test configuration init fails
     */
    protected void init(String pattern) throws APIManagerIntegrationTestException {
        userMode = TestUserMode.SUPER_TENANT_ADMIN;
        setURLs(pattern);
    }

    protected void setURLs(String pattern){

        HashMap<String,String> instanceMap2 = null;
        try {
            DeploymentConfigurationReader depconf = DeploymentConfigurationReader.readConfiguration();
            instanceMap2 = depconf.getDeploymentInstanceMap(pattern);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DeploymentDataReader dataJsonReader = new DeploymentDataReader();
        List<InstanceUrls> urlList = dataJsonReader.getInstanceUrlsList();
        for (InstanceUrls amt : urlList){
            if (instanceMap2 != null) {
                if (amt.getLable().equals(instanceMap2.get(APIMIntegrationConstants.AM_STORE_INSTANCE))) {
                    storeURL=getHTTPSUrl("servlet-http",amt.getHostIP(),amt.getPorts(),"/store");
                }
                if (amt.getLable().equals(instanceMap2.get(APIMIntegrationConstants.AM_PUBLISHER_INSTANCE))) {
                    publisherURL=getHTTPSUrl("servlet-http",amt.getHostIP(),amt.getPorts(),"/publisher");
                }
                if (amt.getLable().equals(instanceMap2.get(APIMIntegrationConstants.AM_KEY_MANAGER_INSTANCE))) {
                    keyManagerURL=getHTTPSUrl("servlet-http",amt.getHostIP(),amt.getPorts(),"");
                }
                if (amt.getLable().equals(instanceMap2.get(APIMIntegrationConstants.AM_GATEWAY_MGT_INSTANCE))) {
                    gateWayManagerURL=getHTTPSUrl("servlet-http",amt.getHostIP(),amt.getPorts(),"");
                }
                if (amt.getLable().equals(instanceMap2.get(APIMIntegrationConstants.AM_GATEWAY_WRK_INSTANCE))) {
                    gateWayWorkerURL=getHTTPSUrl("pass-through-http",amt.getHostIP(),amt.getPorts(),"");
                }
            }
        }
    }

    protected String getHTTPSUrl(String protocol, String hostIP, List<Port> ports, String context){

        String Url = "http://"+hostIP+":";
        for (Port port : ports) {
            if (port.getProtocol().equals(protocol)){
                Url = Url + port.getPort() + context;
                break;
            }
        }
        return Url;
    }

    protected void setTestSuite(String testSuite) throws IOException {
        ScriptExecutorUtil.deployScenario(testSuite);
    }

    protected void unSetTestSuite(String testSuite)
            throws APIManagerIntegrationTestException, IOException {
        ScriptExecutorUtil.unDeployScenario(testSuite);
        //gatewayWebAppUrl = gatewayUrls.getWebAppURLNhttp();
    }

    /**
     * init the object with user mode , create context objects and get session cookies
     *
     * @param userMode - user mode to run the tests
     * @throws APIManagerIntegrationTestException - if test configuration init fails
     */
    protected void init(TestUserMode userMode) throws APIManagerIntegrationTestException {

        try {
            DeploymentConfigurationReader depconf = DeploymentConfigurationReader.readConfiguration();
            instanceMap = depconf.getDeploymentInstanceMap("pattern1");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            storeContext =
                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                          instanceMap.get(APIMIntegrationConstants.AM_STORE_INSTANCE), userMode);

                storeUrls = new APIMURLBean(storeContext.getContextUrls());
            //create publisher server instance based on configuration given at automation.xml
            publisherContext =
                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                          instanceMap.get(APIMIntegrationConstants.AM_PUBLISHER_INSTANCE), userMode);
                publisherUrls = new APIMURLBean(publisherContext.getContextUrls());

            //create gateway server instance based on configuration given at automation.xml

            gatewayContextMgt =
                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                          instanceMap.get(APIMIntegrationConstants.AM_GATEWAY_MGT_INSTANCE), userMode);

                gatewayUrlsMgt = new APIMURLBean(gatewayContextMgt.getContextUrls());

            gatewayContextWrk =
                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                          instanceMap.get(APIMIntegrationConstants.AM_GATEWAY_WRK_INSTANCE), userMode);
                gatewayUrlsWrk = new APIMURLBean(gatewayContextWrk.getContextUrls());


            keyManagerContext = new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                                      instanceMap.get(APIMIntegrationConstants.AM_KEY_MANAGER_INSTANCE), userMode);
                keyMangerUrl = new APIMURLBean(keyManagerContext.getContextUrls());

            backEndServer = new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                                  APIMIntegrationConstants.BACKEND_SERVER_INSTANCE, userMode);
            try {
                backEndServerUrl = new APIMURLBean(backEndServer.getContextUrls());
            } catch(NoSuchElementException ex){
                // since backend instance not available in the deployment
                backEndServerUrl = defaultUrls;
            }

            user = storeContext.getContextTenant().getContextUser();

        } catch (XPathExpressionException e) {
            log.error("APIM test environment initialization failed", e);
            throw new APIManagerIntegrationTestException("APIM test environment initialization failed", e);
        }

    }

    /**
     * init the object with tenant domain, user key and instance of store,publisher and gateway
     * create context objects and construct URL bean
     *
     * @param domainKey - tenant domain key
     * @param userKey   - tenant user key
     * @throws APIManagerIntegrationTestException - if test configuration init fails
     */
    protected void init(String domainKey, String userKey)
            throws APIManagerIntegrationTestException {

        try {
            //create store server instance based configuration given at automation.xml
            storeContext =
                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                          APIMIntegrationConstants.AM_STORE_INSTANCE, domainKey, userKey);
            storeUrls = new APIMURLBean(storeContext.getContextUrls());

            //create publisher server instance
            publisherContext =
                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                          APIMIntegrationConstants.AM_PUBLISHER_INSTANCE, domainKey, userKey);
            publisherUrls = new APIMURLBean(publisherContext.getContextUrls());

            //create gateway server instance
            gatewayContextMgt =
                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                          APIMIntegrationConstants.AM_GATEWAY_MGT_INSTANCE, domainKey, userKey);
            gatewayUrlsMgt = new APIMURLBean(gatewayContextMgt.getContextUrls());

//            gatewayContextWrk =
//                    new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
//                                          APIMIntegrationConstants.AM_GATEWAY_WRK_INSTANCE, domainKey, userKey);
//            gatewayUrlsWrk = new APIMURLBean(gatewayContextWrk.getContextUrls());

            keyManagerContext = new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
                                                      APIMIntegrationConstants.AM_KEY_MANAGER_INSTANCE, domainKey, userKey);
            keyMangerUrl = new APIMURLBean(keyManagerContext.getContextUrls());

//            backEndServer = new AutomationContext(APIMIntegrationConstants.AM_PRODUCT_GROUP_NAME,
//                                                  APIMIntegrationConstants.BACKEND_SERVER_INSTANCE, domainKey, userKey);
//            backEndServerUrl = new APIMURLBean(backEndServer.getContextUrls());

            user = storeContext.getContextTenant().getContextUser();

        } catch (XPathExpressionException e) {
            log.error("Init failed", e);
            throw new APIManagerIntegrationTestException("APIM test environment initialization failed", e);
        }

    }

    /**
     * @param automationContext - automation context instance of given server
     * @return - created session cookie variable
     * @throws APIManagerIntegrationTestException - Throws if creating session cookie fails
     */
    protected String createSession(AutomationContext automationContext)
            throws APIManagerIntegrationTestException {
        LoginLogoutClient loginLogoutClient;
        try {
            loginLogoutClient = new LoginLogoutClient(automationContext);
            return loginLogoutClient.login();
        } catch (Exception e) {
            log.error("session creation error", e);
            throw new APIManagerIntegrationTestException("session creation error", e);
        }
    }

    /**
     * Get test artifact resources location
     *
     * @return - absolute patch of test artifact directory
     */
    protected String getAMResourceLocation() {
        return FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "AM";
    }

    protected String getStoreURLHttp() {
        return storeUrls.getWebAppURLHttp();
    }

    protected String getStoreURLHttps() {
        return storeUrls.getWebAppURLHttps();
    }

    protected String getPublisherURLHttp() {
        return publisherUrls.getWebAppURLHttp();
    }

    protected String getPublisherURLHttps() {
        return publisherUrls.getWebAppURLHttps();
    }

    protected String getGatewayMgtURLHttp() {
        return gatewayUrlsMgt.getWebAppURLHttp();
    }

    protected String getGatewayMgtBackendURLHttps() {
        return gatewayUrlsMgt.getWebAppURLHttp();
    }

    protected String getGatewayMgtURLHttps() {
        return gatewayUrlsMgt.getWebAppURLHttps();
    }

    protected String getGatewayURLHttp() {
        return gatewayUrlsMgt.getWebAppURLHttp();
    }

    protected String getGatewayURLNhttp() {
        return gatewayUrlsMgt.getWebAppURLNhttp();
    }

    protected String getKeyManagerURLHttp() {
        return keyMangerUrl.getWebAppURLHttp();
    }

    protected String getKeyManagerURLHttps() throws XPathExpressionException, IOException {
        return keyManagerContext.getContextUrls().getBackEndUrl().replace("/services", "");
    }

    protected String getAPIInvocationURLHttp(String apiContext)
            throws XPathExpressionException, IOException {
        return gatewayContextMgt.getContextUrls().getServiceUrl().replace("/services", "") + "/" + apiContext;
    }

    protected String getAPIInvocationURLHttp(String apiContext, String version)
            throws XPathExpressionException, IOException {
        return gatewayContextMgt.getContextUrls().getServiceUrl().replace("/services", "") + "/" + apiContext + "/" + version;
    }

    protected String getAPIInvocationURLHttps(String apiContext)
            throws XPathExpressionException, IOException {
        return gatewayContextMgt.getContextUrls().getSecureServiceUrl() + "/" + apiContext;
    }

    protected String getBackendEndServiceEndPointHttp(String serviceName) {
        return backEndServerUrl.getWebAppURLHttp() + serviceName;
    }

    protected String getBackendEndServiceEndPointHttps(String serviceName) {
        return backEndServerUrl.getWebAppURLHttps() + serviceName;
    }

    /**
     * Cleaning up the API manager by removing all APIs and applications other than default application
     *
     * @throws APIManagerIntegrationTestException - occurred when calling the apis
     * @throws org.json.JSONException             - occurred when reading the json
     */
    protected void cleanUp() throws Exception {

        APIStoreRestClient apiStore = new APIStoreRestClient(getStoreURLHttp());
        apiStore.login(user.getUserName(), user.getPassword());
        APIPublisherRestClient publisherRestClient = new APIPublisherRestClient(publisherURL);
        publisherRestClient.login(user.getUserName(), user.getPassword());
        HttpResponse subscriptionDataResponse = apiStore.getAllSubscriptions();
        verifyResponse(subscriptionDataResponse);
        JSONObject jsonSubscription = new JSONObject(subscriptionDataResponse.getData());

        if (!jsonSubscription.getBoolean(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_ERROR)) {
            JSONObject jsonSubscriptionsObject = jsonSubscription.getJSONObject(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_SUBSCRIPTION);
            JSONArray jsonApplicationsArray = jsonSubscriptionsObject.getJSONArray(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_APPLICATIONS);

            //Remove API Subscriptions
            for (int i = 0; i < jsonApplicationsArray.length(); i++) {
                JSONObject appObject = jsonApplicationsArray.getJSONObject(i);
                int id = appObject.getInt(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_ID);
                JSONArray subscribedAPIJSONArray = appObject.getJSONArray(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_SUBSCRIPTION);
                for (int j = 0; j < subscribedAPIJSONArray.length(); j++) {
                    JSONObject subscribedAPI = subscribedAPIJSONArray.getJSONObject(j);
                    verifyResponse(apiStore.removeAPISubscription(subscribedAPI.getString(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_API_NAME)
                            , subscribedAPI.getString(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_API_VERSION),
                                                                  subscribedAPI.getString(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_API_PROVIDER), String.valueOf(id)));
                }
            }
        }

        //delete all application other than default application
        String applicationData = apiStore.getAllApplications().getData();
        JSONObject jsonApplicationData = new JSONObject(applicationData);
        JSONArray applicationArray = jsonApplicationData.getJSONArray(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_APPLICATIONS);
        for (int i = 0; i < applicationArray.length(); i++) {
            JSONObject jsonApplication = applicationArray.getJSONObject(i);
            if (!jsonApplication.getString(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_API_NAME).equals(APIMIntegrationConstants.OAUTH_DEFAULT_APPLICATION_NAME)) {
                verifyResponse(apiStore.removeApplication(jsonApplication.getString(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_API_NAME)));
            }
        }

        String apiData = apiStore.getAPI().getData();
        JSONObject jsonAPIData = new JSONObject(apiData);
        JSONArray jsonAPIArray = jsonAPIData.getJSONArray(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_APIS);

        //delete all APIs
        for (int i = 0; i < jsonAPIArray.length(); i++) {
            JSONObject api = jsonAPIArray.getJSONObject(i);
//            verifyResponse(publisherRestClient.deleteAPI(api.getString("name"), api.getString("version"), user.getUserName()));
            publisherRestClient.deleteAPI(api.getString(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_API_NAME)
                    , api.getString(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_API_VERSION), user.getUserName());
        }
    }

    protected void verifyResponse(HttpResponse httpResponse) throws JSONException {
        Assert.assertNotNull(httpResponse, "Response object is null");
        log.info("Response Code : " + httpResponse.getResponseCode());
        log.info("Response Message : " + httpResponse.getData());
        Assert.assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK, "Response code is not as expected");
        JSONObject responseData = new JSONObject(httpResponse.getData());
        Assert.assertFalse(responseData.getBoolean(APIMIntegrationConstants.API_RESPONSE_ELEMENT_NAME_ERROR), "Error message received " + httpResponse.getData());

    }

}


