package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import exceptions.CustomRuntimeException;
import lombok.RequiredArgsConstructor;
import model.TestConstants;
import model.transaction.TransactionApi;
import model.transaction.TransactionResponseApi;
import model.wallet.ResponseApi;
import model.wallet.WalletApi;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import web.api.stubs.PrepareTransactionOKStub;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

/**
 * This class provides actions to interact with BlockIo through url
 */
@Component
@RequiredArgsConstructor
public class RequestsUtil {
    private final Log logger = LogFactory.getLog(this.getClass());

    private final RestTemplate restTemplate;

    private final PrepareTransactionOKStub prepareTransactionOKStub;

    private final ObjectMapper objectMapper;

    /**
     * Method to provide a couple of actions to make a transaction. Includes prepare and create a Transaction
     *
     * @param apiKey             - key of account
     * @param fee                - number custom of fee
     * @param amount             - number of amount to pay
     * @param destinationAddress - to address of wallet to where will be transaction
     * @return body of response
     */
    public String sendBTC(final String apiKey, final BigDecimal fee, final BigDecimal amount, final String destinationAddress) {
        logger.debug(format("Send BTC with next params: {apiKey=%s}, {fee=%s}, {amount=%s}, {destinationAddress=%s}",
                apiKey, fee.toString(), amount.toString(), destinationAddress));
        final String responseBody = prepareTransaction(apiKey, fee, amount, destinationAddress);
        createTransaction(apiKey, fee, amount, destinationAddress);
        return responseBody;
    }

    /**
     * Use it ONLY for testing with manual operations! It is a stub for fake operation
     *
     * @param fromWallet - address from where will be transaction
     * @param toWallet   - address to where will be transaction (destination)
     * @param fee        - number of custom fee
     * @param amount     - number of amount to pay
     */
    public void changeBalance(final WalletApi fromWallet, final WalletApi toWallet, final BigDecimal fee, final BigDecimal amount) {
        fromWallet.setAvailableBalance(fromWallet.getAvailableBalanceAsNumber().subtract(fee).subtract(amount).toString());
        toWallet.setAvailableBalance(toWallet.getAvailableBalanceAsNumber().add(amount).toString());
    }

    /**
     * Creates a transaction with required fields
     *
     * @param apiKey             - key of account
     * @param fee                - number custom of fee
     * @param amount             - number of amount to pay
     * @param destinationAddress - to address of wallet to where will be transaction
     */
    public void createTransaction(final String apiKey, final BigDecimal fee, final BigDecimal amount, final String destinationAddress) {
        logger.debug(format("The transaction was created for api=%s, customFee=%s, amount=%s, destinationAddress=%s",
                apiKey, fee, amount, destinationAddress));
    }

    /**
     * Global method to prepare a stub for "/api/v2/prepare_transaction/.*" path
     *
     * @param wireMockServer - instance of WireMock to initialize a stub
     */
    public void prepareStubStatement(final WireMockServer wireMockServer) {
        logger.debug("Creates a stub for WireMock with path \"/api/v2/prepare_transaction/.*\"");
        prepareTransactionOKStub.createStub(wireMockServer);
    }

    /**
     * Send a request for transaction prepared with parameters
     *
     * @param apiKey             - key of account
     * @param fee                - number custom of fee
     * @param amount             - number of amount to pay
     * @param destinationAddress - to address of wallet to where will be transaction
     * @return body of response
     */
    public String prepareTransaction(final String apiKey, final BigDecimal fee, final BigDecimal amount, final String destinationAddress) {
        final String url = format("%sprepare_transaction/?api_key=%s&priority=custom&custom_network_fee=%s&amounts=%s&to_addresses=%s",
                TestConstants.BASE_URL, apiKey, fee, amount, destinationAddress);
        logger.debug(format("Prepare a transaction with next params: {apiKey=%s}, {fee=%s}, {amount=%s}, {destinationAddress=%s} by url=%s",
                apiKey, fee.toString(), amount.toString(), destinationAddress, url));
        return sendFakeRequestAndGetResponse(url);
    }

    /**
     * Send a request to create a new wallet/address
     *
     * @param apiKey    - key of account
     * @param labelName - custom label name to create
     */
    public void createWalletWithGivenLabel(final String apiKey, final String labelName) {
        final String url = format("%sget_new_address/?api_key=%s&label=%s",
                TestConstants.BASE_URL, TestConstants.FAKE_KEY, TestConstants.LABEL);
        logger.debug(format("Send a request to create a wallet={%s}", url));

        sendGetRequestAndGetResponse(url);
    }

    /**
     * Find a wallet with specific address
     *
     * @param apiKey   - key of account
     * @param walletId - address of wallet
     * @return {@link WalletApi} if exists or Threw an {@link java.util.NoSuchElementException} exception
     */
    public WalletApi getWalletByAddress(final String apiKey, final String walletId) {
        logger.debug(format("Get wallet by address=%s", walletId));
        return getWallets(apiKey).stream()
                .filter(wallet -> wallet.getAddress().equals(walletId))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Gets all non-archived wallets
     *
     * @param apiKey - key of account
     * @return list of wallets
     */
    public List<WalletApi> getWallets(final String apiKey) {
        final String url = format("%sget_my_addresses/?api_key=%s&page=1",
                TestConstants.BASE_URL, Objects.nonNull(apiKey) ? apiKey : TestConstants.VALID_KEY);
        logger.debug(format("Get all non-archived wallets by url=%s", url));

        return getListWallets(sendGetRequestAndGetResponse(url));
    }

    /**
     * Gets all archived wallets
     *
     * @param apiKey - key of account
     * @return list of archived wallets
     */
    public List<WalletApi> getArchivedWallets(final String apiKey) {
        final String url = format("%sget_my_archived_addresses/?api_key=%s&page=1",
                TestConstants.BASE_URL, TestConstants.VALID_KEY);
        logger.debug(format("Get archived wallets for {%s} by url=%s", apiKey, url));

        return getListWallets(sendGetRequestAndGetResponse(url));
    }

    /**
     * Gets all transactions with type {@link model.transaction.TransactionType} SENT
     *
     * @return list of transactions
     */
    public List<TransactionApi> getSentTransactions() {
        final String url = String.format("%sget_transactions/?api_key=%s&type=sent",
                TestConstants.BASE_URL, TestConstants.VALID_KEY);
        logger.debug(format("Get SENT transactions by url=%s", url));

        final String response = sendGetRequestAndGetResponse(url);
        return getTransactionApis(response);
    }

    /**
     * Use it ONLY for testing with manual operations! It is a stub for fake operation
     *
     * @param transactions - list of operations to change
     */
    public void updateTransactionList(final List<TransactionApi> transactions) {
        final TransactionApi transactionApi = transactions.get(0);
        final TransactionApi clone = transactionApi.clone().toBuilder()
                .txid("t".repeat(46))
                .build();
        transactions.add(clone);
    }

    /**
     * Gets all transactions with type {@link model.transaction.TransactionType} RECEIVED
     *
     * @return list of transactions
     */
    public List<TransactionApi> getReceivedTransactions() {
        final String url = String.format("%sget_transactions/?api_key=%s&type=received",
                TestConstants.BASE_URL, TestConstants.VALID_KEY);
        logger.debug(format("Get RECEIVED transactions by url=%s", url));

        final String response = sendGetRequestAndGetResponse(url);

        return getTransactionApis(response);
    }

    private List<TransactionApi> getTransactionApis(String response) {
        final TransactionResponseApi transaction = mapJsonToObject(response, TransactionResponseApi.class);
        return Objects.nonNull(transaction.getData().getTransactions())
                ? transaction.getData().getTransactions()
                : Collections.emptyList();
    }

    private List<WalletApi> getListWallets(final String response) {
        final ResponseApi responseApi = mapJsonToObject(response, ResponseApi.class);
        return Objects.nonNull(responseApi)
                && Objects.nonNull(responseApi.getData())
                && Objects.nonNull(responseApi.getData().getAddresses())
                ? responseApi.getData().getAddresses() : Collections.emptyList();
    }

    private <T> T mapJsonToObject(final String json, final Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (final Exception e) {
            throw new CustomRuntimeException(format("Could not parse json=%s", json), e);
        }
    }


    private String sendFakeRequestAndGetResponse(final String url) {
        final String convertedUrl = url.replace("https://block.io", "http://localhost:9090");
        return sendGetRequestAndGetResponse(convertedUrl);
    }

    private String sendGetRequestAndGetResponse(final String url) {
        logger.debug(format("Sending request for url=%s", url));
        final ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();

        logger.info(body);

        return body;
    }
}
