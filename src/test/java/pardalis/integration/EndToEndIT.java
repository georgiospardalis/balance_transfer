package pardalis.integration;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONObject;
import org.junit.*;
import pardalis.RequestUtils;
import pardalis.entity.Account;
import pardalis.entity.TransferOrder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;
import java.util.stream.Collectors;

public class EndToEndIT {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    private static String requestUrl = "http://localhost:";

    @BeforeClass
    public static void init() throws IOException {
        InputStream inputStream = EndToEndIT.class.getResourceAsStream("/app.properties");
        Properties properties = new Properties();

        properties.load(inputStream);

        String persistenceUnit = properties.getProperty("persistenceUnit") + "Tester";

        requestUrl = requestUrl + properties.getProperty("jetty.server.port") + "/transaction/balance_transfer";
        entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterClass
    public static void tearDown() {
        entityManager.clear();
        entityManager.close();
        entityManagerFactory.close();
    }

    @Test
    public void transferOK_IT() throws Exception {
        BigInteger sourceAccountId = new BigInteger("2");
        BigInteger targetAccountId = new BigInteger("3");
        Account sourceAccountBefore = entityManager.find(Account.class, sourceAccountId);
        Account targetAccountBefore = entityManager.find(Account.class, targetAccountId);
        Long transactionsBefore = getRowCountsForTransferOrderTable();
        Long transactionsAfterExpected = transactionsBefore + 1;

        Assert.assertNotNull(sourceAccountBefore);
        Assert.assertNotNull(targetAccountBefore);

        String requestBody = RequestUtils.prepareRequestBodyJson(sourceAccountId.toString(), targetAccountId.toString(), "500.00");
        CloseableHttpResponse closeableHttpResponse = RequestUtils.sendPUTRequestWithJsonBody(requestUrl, requestBody);

        Assert.assertEquals(200, closeableHttpResponse.getStatusLine().getStatusCode());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertTrue(responseBody.toLowerCase().contains("successful transaction"));

        entityManager.clear();

        Account sourceAccountAfter = entityManager.find(Account.class, sourceAccountId);
        Account targetAccountAfter = entityManager.find(Account.class, targetAccountId);
        Long transactionsAfter = getRowCountsForTransferOrderTable();

        Assert.assertEquals(transactionsAfterExpected, transactionsAfter);
        Assert.assertNotNull(sourceAccountAfter);
        Assert.assertNotNull(targetAccountAfter);
        Assert.assertEquals(new BigDecimal("500.00"), sourceAccountBefore.getBalance().subtract(sourceAccountAfter.getBalance()));
        Assert.assertEquals(targetAccountBefore.getBalance().add(new BigDecimal("500.00")), targetAccountAfter.getBalance());

        JSONObject jsonObject = new JSONObject(responseBody);
        TransferOrder transferOrder = entityManager.find(TransferOrder.class, jsonObject.get("transferId"));

        Assert.assertNotNull(transferOrder);
        Assert.assertEquals(sourceAccountId, transferOrder.getSourceAccountId());
        Assert.assertEquals(targetAccountId, transferOrder.getTargetAccountId());
        Assert.assertEquals(new BigDecimal("500.00"), transferOrder.getTransferAmount());
        Assert.assertEquals(transferOrder.getTimestamp(), jsonObject.get("timestamp"));
    }

    @Test
    public void transferNoAccount_IT() throws Exception {
        BigInteger targetAccountId = new BigInteger("3");
        Account targetAccountBefore = entityManager.find(Account.class, targetAccountId);
        Long transactionsBefore = getRowCountsForTransferOrderTable();
        String requestBody = RequestUtils.prepareRequestBodyJson("", targetAccountId.toString(), "500.00");
        CloseableHttpResponse closeableHttpResponse = RequestUtils.sendPUTRequestWithJsonBody(requestUrl, requestBody);

        Assert.assertEquals(200, closeableHttpResponse.getStatusLine().getStatusCode());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        entityManager.clear();

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Must provide both Accounts\"}");
        Assert.assertEquals(transactionsBefore, getRowCountsForTransferOrderTable());

        if (targetAccountBefore != null) {
            Account targetAccountAfter = entityManager.find(Account.class, targetAccountId);

            Assert.assertNotNull(targetAccountAfter);
            Assert.assertEquals(targetAccountAfter, targetAccountBefore);
        }
    }

    @Test
    public void transferAmountNegative_IT() throws Exception {
        BigInteger sourceAccountId = new BigInteger("1");
        BigInteger targetAccountId = new BigInteger("3");
        Account sourceAccountBefore = entityManager.find(Account.class, sourceAccountId);
        Account targetAccountBefore = entityManager.find(Account.class, targetAccountId);
        Long transactionsBefore = getRowCountsForTransferOrderTable();
        String requestBody = RequestUtils.prepareRequestBodyJson(sourceAccountId.toString(), targetAccountId.toString(), "-500.00");
        CloseableHttpResponse closeableHttpResponse = RequestUtils.sendPUTRequestWithJsonBody(requestUrl, requestBody);

        Assert.assertEquals(200, closeableHttpResponse.getStatusLine().getStatusCode());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        entityManager.clear();

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Transfer Amount cannot be negative\"}");
        Assert.assertEquals(transactionsBefore, getRowCountsForTransferOrderTable());

        if (sourceAccountBefore != null) {
            Account sourceAccountAfter = entityManager.find(Account.class, sourceAccountId);

            Assert.assertNotNull(sourceAccountAfter);
            Assert.assertEquals(sourceAccountAfter, sourceAccountBefore);
        }

        if (targetAccountBefore != null) {
            Account targetAccountAfter = entityManager.find(Account.class, targetAccountId);

            Assert.assertNotNull(targetAccountAfter);
            Assert.assertEquals(targetAccountAfter, targetAccountBefore);
        }
    }

    @Test
    public void transferInsufficientBalance_IT() throws Exception {
        BigInteger sourceAccountId = new BigInteger("1");
        BigInteger targetAccountId = new BigInteger("3");
        Account sourceAccountBefore = entityManager.find(Account.class, sourceAccountId);
        Account targetAccountBefore = entityManager.find(Account.class, targetAccountId);
        Long transactionsBefore = getRowCountsForTransferOrderTable();

        Assert.assertNotNull(sourceAccountBefore);
        Assert.assertNotNull(targetAccountBefore);

        String requestBody = RequestUtils.prepareRequestBodyJson(sourceAccountId.toString(), targetAccountId.toString(), "104000.00");
        CloseableHttpResponse closeableHttpResponse = RequestUtils.sendPUTRequestWithJsonBody(requestUrl, requestBody);

        Assert.assertEquals(200, closeableHttpResponse.getStatusLine().getStatusCode());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        entityManager.clear();

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Insufficient Balance\"}");
        Assert.assertEquals(transactionsBefore, getRowCountsForTransferOrderTable());

        Account sourceAccountAfter = entityManager.find(Account.class, sourceAccountId);
        Account targetAccountAfter = entityManager.find(Account.class, targetAccountId);

        Assert.assertNotNull(sourceAccountAfter);
        Assert.assertNotNull(targetAccountAfter);
        Assert.assertEquals(sourceAccountAfter, sourceAccountBefore);
        Assert.assertEquals(targetAccountBefore, targetAccountAfter);
        Assert.assertEquals(sourceAccountBefore.getBalance().subtract(new BigDecimal("100500.0")).compareTo(BigDecimal.ZERO), -1);
    }

    @Test
    public void transferFromNonExistentAccount_IT() throws Exception {
        BigInteger imaginaryAccountId = new BigInteger("15555");
        BigInteger targetAccountId = new BigInteger("3");
        Account imaginaryAccountBefore = entityManager.find(Account.class, imaginaryAccountId);
        Account targetAccountBefore = entityManager.find(Account.class, targetAccountId);
        Long transactionsBefore = getRowCountsForTransferOrderTable();

        Assert.assertNotNull(targetAccountBefore);
        Assert.assertNull(imaginaryAccountBefore);

        String requestBody = RequestUtils.prepareRequestBodyJson(imaginaryAccountId.toString(), targetAccountId.toString(), "500.00");
        CloseableHttpResponse closeableHttpResponse = RequestUtils.sendPUTRequestWithJsonBody(requestUrl, requestBody);

        Assert.assertEquals(200, closeableHttpResponse.getStatusLine().getStatusCode());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        entityManager.clear();

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Could not find Account(s)\"}");
        Assert.assertEquals(transactionsBefore, getRowCountsForTransferOrderTable());

        Account imaginaryAccountAfter = entityManager.find(Account.class, imaginaryAccountId);
        Account targetAccountAfter = entityManager.find(Account.class, targetAccountId);

        Assert.assertNull(imaginaryAccountAfter);
        Assert.assertNotNull(targetAccountAfter);
        Assert.assertEquals(targetAccountAfter, targetAccountBefore);
    }

    @Test
    public void transferSenderRecipientAreTheSame_IT() throws Exception {
        BigInteger accountId = new BigInteger("3");
        Account selfTargetedBefore = entityManager.find(Account.class, accountId);
        Long transactionsBefore = getRowCountsForTransferOrderTable();
        String requestBody = RequestUtils.prepareRequestBodyJson(accountId.toString(), accountId.toString(), "500.00");
        CloseableHttpResponse closeableHttpResponse = RequestUtils.sendPUTRequestWithJsonBody(requestUrl, requestBody);

        Assert.assertEquals(200, closeableHttpResponse.getStatusLine().getStatusCode());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        entityManager.clear();

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Sender and Recipient is the same Account\"}");
        Assert.assertEquals(transactionsBefore, getRowCountsForTransferOrderTable());

        if (selfTargetedBefore != null) {
            Account selfTargetedAfter = entityManager.find(Account.class, accountId);

            Assert.assertNotNull(selfTargetedAfter);
            Assert.assertEquals(selfTargetedAfter, selfTargetedBefore);
        }
    }

    private static Long getRowCountsForTransferOrderTable() {
        Query query = entityManager.createQuery("SELECT count(*) FROM TransferOrder");

        return (Long)query.getSingleResult();
    }
}