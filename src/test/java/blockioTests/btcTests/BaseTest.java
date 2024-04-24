package blockioTests.btcTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import util.RequestsUtil;
import web.api.stubs.PrepareTransactionOKStub;

import java.lang.reflect.Method;

import static java.lang.String.format;

/**
 * This class uses {@link AbstractTestNGSpringContextTests} to initialize
 * spring context and is also used as configuration and to include external dependencies.
 * The main idea in creating this class is to make a global parent class
 * for flexibility settings and configuration for a special scope of the tests
 */
@SpringBootTest(classes = {RestTemplateConfig.class,
        RequestsUtil.class,
        PrepareTransactionOKStub.class,
        ObjectMapper.class})
public class BaseTest extends AbstractTestNGSpringContextTests {

    @Autowired
    protected RequestsUtil requestsUtil;

    @BeforeMethod(alwaysRun = true)
    protected final void beforeMethod(final Method method) {
        logger.info(format("!Start %s", method.getName()));
    }

    @AfterMethod(alwaysRun = true)
    protected final void afterMethod(final Method method) {
        logger.info(format("!End %s", method.getName()));
    }
}