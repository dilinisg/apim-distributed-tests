package org.wso2.apim.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.BeforeSuite;
import org.testng.collections.Lists;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.distributed.TestLinkConstants;
import org.wso2.carbon.automation.distributed.beans.Deployment;
import org.wso2.carbon.automation.distributed.beans.TestLink;
import org.wso2.carbon.automation.distributed.commons.BaseManager;
import org.wso2.carbon.automation.distributed.commons.DeploymentConfigurationReader;
import org.wso2.carbon.automation.distributed.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.distributed.utills.GitRepositoryUtil;
import org.wso2.carbon.automation.distributed.utills.TestLinkBuilder;
import org.wso2.carbon.automation.distributed.utills.TestLinkSiteUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Running the TestNG programatically and Injecting the suites
 */
public class TestNgExecuter {
    private static final Log log = LogFactory.getLog(TestNgExecuter.class);

    @BeforeSuite
    public void executeEnvironment() throws IOException
    {

        TestLink testLinkBean = new DeploymentConfigurationReader().getTestLinkConfigurations();
        List<Deployment> deploymentList;

        if (false){

            TestLinkBuilder tlbuilder = new TestLinkBuilder();
            log.info("Connecting to TestLink : " +testLinkBean.getUrl());
            TestLinkSiteUtil tlsite = null;

            HashMap<String, Deployment> deploymentHashMap = new DeploymentConfigurationReader().getDeploymentHashMap();
            deploymentList = new ArrayList<>(deploymentHashMap.values());
            ArrayList mylist = null;

            for (Deployment deployment : deploymentList) {
                // The Test Link platform should e same as the deployment pattern name
                tlsite = tlbuilder.getTestLinkSite(testLinkBean.getUrl(),testLinkBean.getDevkey(),testLinkBean.getProjectName(),testLinkBean.getTestPlan(),deployment.getName(),testLinkBean.getBuild(),null);
                log.info("Retrieving Automation Test from TestLink. Project : " + testLinkBean.getProjectName() + " Test Plan : " +testLinkBean.getTestPlan() + " Platform : " +deployment.getName());
                mylist = tlsite.getTestCaseClassList(new String[]{testLinkBean.getTestLinkCustomField()});

                XmlSuite suite = new XmlSuite();
                suite.setName(deployment.getName());
                XmlTest test = new XmlTest(suite);
                test.setName("AutomationTests");
                List<XmlClass> classes = new ArrayList<>();

                for (Object className : mylist){
                    try {
                        classes.add(new XmlClass((String) className));
                    } catch (TestNGException e){
                        log.error("Error occurred while adding the class : " +e.toString());
                    }
                }

                test.setXmlClasses(classes) ;
                List<XmlSuite> suites = new ArrayList<>();
                suites.add(suite);
                TestNG tng = new TestNG();
                tng.setXmlSuites(suites);
                log.info("Running Test Suite " +deployment.getName());
                tng.run();
            }
        } else {
            TestNG testng = new TestNG();
            List<String> suites = new ArrayList<>();
            suites.add("../src/test/resources/testng_pattern1.xml"); // TODO The path shouldn't be hardcoded.
            testng.setTestSuites(suites);
            testng.run();

        }



//        TestListenerAdapter tla = new TestListenerAdapter();
//        TestNG testng = new TestNG();
//        //Class[] stockArr = new Class[mylist.size()];
//        //stockArr = ;
//        Class[] array = mylist.toArray(new Class[mylist.size()]);
//        testng.setTestClasses(org.wso2.apim.integration.tests.api.lifecycle.ChangeApplicationTierAndTestInvokingTestCase.C);
//        testng.addListener(tla);
//        testng.run();

//        XmlSuite suite = new XmlSuite();
//        suite.setName("pattern1");
//
//        XmlTest test = new XmlTest(suite);
//        test.setName("TmpTest");
//        List<XmlClass> classes = new ArrayList<XmlClass>();
//        classes.add(new XmlClass("org.wso2.apim.deployment.lifecycle.APIAccessibilityOfPublishedOldAPIAndPublishedCopyAPITestCase"));
//        test.setXmlClasses(classes) ;
//
//        List<XmlSuite> suites = new ArrayList<XmlSuite>();
//        suites.add(suite);
//        TestNG tng = new TestNG();
//        tng.setXmlSuites(suites);
//        tng.run();

    }
}
