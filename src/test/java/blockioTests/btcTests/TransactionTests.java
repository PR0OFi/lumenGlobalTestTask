package blockioTests.btcTests;

import com.github.tomakehurst.wiremock.WireMockServer;
import model.TestConstants;
import model.transaction.TransactionApi;
import model.wallet.WalletApi;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * The main idea of this class is to show that tests with additional actions like updating or sending a transaction
 * can be handled with a stub.
 * To check the result of updating entities {@link WalletApi} and {@link TransactionApi} without
 * fake updating take the breakpoint before the operation, mute or delete them, make the action manually,
 * and go ahead to the test flow
 */
public class TransactionTests extends BaseTest {

    private final BigDecimal customFee = BigDecimal.valueOf(0.0000208);
    private final BigDecimal amount = BigDecimal.valueOf(0.00001);
    private final String fromAddress = "2MsoGZPCmNMm3AWJC186FQXyoWGDZiUyuWW";
    private final String destinationAddress = "2N9zGCfYdRVaZW7LGwVeJVyuuhB5KoEfe7N";

    private WireMockServer wireMockServer;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        wireMockServer = new WireMockServer(9090);
        requestsUtil.prepareStubStatement(wireMockServer);

        wireMockServer.start();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void verifyBalanceAfterTransaction() {
        final WalletApi fromWalletApi = requestsUtil.getWalletByAddress(TestConstants.VALID_KEY, fromAddress);
        final WalletApi toWalletApi = requestsUtil.getWalletByAddress(TestConstants.VALID_KEY, destinationAddress);

        requestsUtil.sendBTC(TestConstants.VALID_KEY, customFee, amount, destinationAddress);

        final WalletApi updatedFromWalletApi = requestsUtil.getWalletByAddress(TestConstants.VALID_KEY, fromAddress);
        final WalletApi updatedToWalletApi = requestsUtil.getWalletByAddress(TestConstants.VALID_KEY, destinationAddress);
        requestsUtil.changeBalance(updatedFromWalletApi, updatedToWalletApi, customFee, amount);

        Assert.assertEquals(updatedFromWalletApi.getAvailableBalanceAsNumber(),
                fromWalletApi.getAvailableBalanceAsNumber().subtract(amount.add(customFee)));
        Assert.assertEquals(updatedToWalletApi.getAvailableBalanceAsNumber(), toWalletApi.getAvailableBalanceAsNumber().add(amount));
    }

    @Test
    public void verifyCountTransactions() {
        final List<TransactionApi> sentTransactions = requestsUtil.getSentTransactions();
        final List<TransactionApi> receivedTransactions = requestsUtil.getReceivedTransactions();

        requestsUtil.sendBTC(TestConstants.VALID_KEY, customFee, amount, destinationAddress);

        final List<TransactionApi> updatedSentTransactions = requestsUtil.getSentTransactions();
        final List<TransactionApi> updatedReceivedTransactions = requestsUtil.getReceivedTransactions();

        requestsUtil.updateTransactionList(updatedSentTransactions);
        requestsUtil.updateTransactionList(updatedReceivedTransactions);

        Assert.assertEquals(updatedSentTransactions.size(), sentTransactions.size() + 1);
        Assert.assertEquals(updatedReceivedTransactions.size(), receivedTransactions.size() + 1);
    }
}
