/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.apim.deployment.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.wso2.apim.base.APIMIntegrationBaseTest;
import org.wso2.apim.exception.APIManagerIntegrationTestException;

import java.io.IOException;

/**
 * Base test class for all API Manager test cases.
 */
public class APIMBaseTest extends APIMIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(APIMBaseTest.class);

    @BeforeSuite(alwaysRun = true) public void createEnvironment(ITestContext ctx)
            throws APIManagerIntegrationTestException, IOException {
        super.setTestSuite(ctx.getCurrentXmlTest().getSuite().getName());
        super.init(ctx.getCurrentXmlTest().getSuite().getName());
    }

    @BeforeClass(alwaysRun = true) public void init(ITestContext ctx) throws APIManagerIntegrationTestException {

    }

    @AfterSuite(alwaysRun = true) public void deleteEnvironment(ITestContext ctx)
            throws APIManagerIntegrationTestException, IOException {
        super.unSetTestSuite(ctx.getCurrentXmlTest().getSuite().getName());
    }
}
