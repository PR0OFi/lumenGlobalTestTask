package blockioTests.btcTests;

import model.TestConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.when;

/**
 * The main idea of this class is show that this request can be handled and verified with just RestAssured
 */
public class WalletActionsTests extends BaseTest {

    private static final String CUSTOM_LABEL = "UserLabel";

    @Test
    public void verifyThatWalletCreatesWithGivenLabel() {
        final String url = String.format("%sget_new_address/?api_key=%s&label=%s",
                TestConstants.BASE_URL, TestConstants.FAKE_KEY, CUSTOM_LABEL);
        final String responseLabel = when().get(url)
                .then()
                .statusCode(200)
                .log().body()
                .extract().path("data.label");

        Assert.assertEquals(responseLabel, CUSTOM_LABEL);
        Assert.assertTrue(
                requestsUtil.getWallets(TestConstants.FAKE_KEY).stream()
                        .anyMatch(walletApi -> walletApi.getLabel().equals(CUSTOM_LABEL)));
    }
}
