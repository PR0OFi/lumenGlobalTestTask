package blockioTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.CustomRuntimeException;
import lib.blockIo.BlockIo;
import model.transaction.TransactionApi;
import model.transaction.TransactionDataApi;
import model.transaction.TransactionType;
import model.wallet.BalanceApi;
import model.wallet.ResponseApi;
import model.wallet.WalletApi;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * The main idea of this class is to show that tests can be covered with the help of the official SDK of {@link BlockIo}
 * This SDK proposes all popular actions like creating and accepting a transaction, getting wallets, etc
 * Those tests work with real API and don't require any stub or mock. So they work with real API and
 * simulate a real behavior with BlockIo service
 */
public class SDKTest {
    private final BlockIo sdk = new BlockIo("2701-0827-6937-ec93", "mnGz53En7oqVKeT4");

    private final String toWalletAddress = "2N9zGCfYdRVaZW7LGwVeJVyuuhB5KoEfe7N";
    private final BigDecimal amount = new BigDecimal("0.00001");

    private final ObjectMapper mapper = new ObjectMapper();

    private JSONObject preparedTransaction;

    public SDKTest() throws Exception {
    }

    @BeforeClass
    public void setup() throws Exception {
        preparedTransaction = sdk.PrepareTransaction(new JSONObject(Map.of(
                "api_key", "2701-0827-6937-ec93",
                "to_address", toWalletAddress,
                "amount", amount.toString(),
                "priority", "custom",
                "custom_network_fee", "0.0000208",
                "from_user_ids", "1"
        )));
    }

    @Test
    public void verifyThatWalletCreatesWithGivenLabel() throws Exception {
        final String testLabel = "testLabelFromSdk";
        sdk.GetNewAddress(new JSONObject(Map.of("label", testLabel)));

        final JSONObject jsonObject = sdk.GetMyAddresses(null);
        Assert.assertTrue(
                mapJsonToObject(jsonObject, ResponseApi.class).getData().getAddresses()
                        .stream()
                        .anyMatch(walletApi -> walletApi.getLabel().equals(testLabel)));
    }

    @Test
    public void verifyCorrectBalanceAfterTransaction() throws Exception {
        final WalletApi toWalletApi = getWalletByAddress(toWalletAddress);

        final JSONObject transaction = sdk.CreateAndSignTransaction(preparedTransaction);
        sdk.SubmitTransaction(new JSONObject(Map.of("transaction_data", transaction)));

        final JSONObject updatedBalance = (JSONObject) sdk.GetAddressBalance(new JSONObject(Map.of("label", toWalletApi.getLabel()))).get("data");
        final BalanceApi updatedToWalletApi = mapJsonToObject(updatedBalance, BalanceApi.class);

        Assert.assertEquals(new BigDecimal(updatedToWalletApi.getAvailableBalance()),
                toWalletApi.getAvailableBalanceAsNumber().add(amount));
    }

    @Test
    public void verifyThatTransactionsUpdatesCorrectly() throws Exception {
        final List<TransactionApi> sentTransactionApis = getTransactions(TransactionType.SENT.getRepresentation());
        final List<TransactionApi> receivedTransactionApis = getTransactions(TransactionType.RECEIVED.getRepresentation());

        final JSONObject transaction = sdk.CreateAndSignTransaction(preparedTransaction);
        sdk.SubmitTransaction(new JSONObject(Map.of("transaction_data", transaction)));

        final List<TransactionApi> updatedSentTransactionApis = getTransactions(TransactionType.SENT.getRepresentation());
        final List<TransactionApi> updatedReceivedTransactionApis = getTransactions(TransactionType.RECEIVED.getRepresentation());

        Assert.assertNotEquals(updatedSentTransactionApis, sentTransactionApis);
        Assert.assertNotEquals(updatedReceivedTransactionApis, receivedTransactionApis);
    }

    private List<TransactionApi> getTransactions(final String type) throws Exception {
        final JSONObject sentTransactions = (JSONObject) sdk.GetTransactions(new JSONObject(Map.of("type", type))).get("data");
        return mapJsonToObject(sentTransactions, TransactionDataApi.class).getTransactions();
    }

    private WalletApi getWalletByAddress(final String toWalletAddress) throws Exception {
        final List<WalletApi> wallets = mapJsonToObject(sdk.GetMyAddresses(null), ResponseApi.class).getData().getAddresses();
        return wallets.stream()
                .filter(walletApi -> walletApi.getAddress().equals(toWalletAddress))
                .findFirst().orElseThrow();
    }

    private <T> T mapJsonToObject(final JSONObject json, final Class<T> clazz) {
        try {
            return mapper.readValue(json.toString(), clazz);
        } catch (final Exception e) {
            throw new CustomRuntimeException(format("Could not parse json=%s", json), e);
        }
    }
}