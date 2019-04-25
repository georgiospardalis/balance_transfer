package pardalis.integration;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pardalis.entity.Account;

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

        Assert.assertNotNull(sourceAccountBefore);
        Assert.assertNotNull(targetAccountBefore);

        String requestBody = "{ \"source-account\": \"" + sourceAccountId.toString() + "\",\n" +
                "\t\"target-account\": \"" + targetAccountId.toString() + "\",\n" +
                "\t\"amount\": \"500.0\" }";

        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertTrue(responseBody.toLowerCase().contains("successful transaction"));

        Account sourceAccountAfter = entityManager.find(Account.class, sourceAccountId);
        Account targetAccountAfter = entityManager.find(Account.class, targetAccountId);

        Assert.assertNotNull(sourceAccountAfter);
        Assert.assertNotNull(targetAccountAfter);
        Assert.assertEquals(sourceAccountBefore.getBalance().subtract(sourceAccountAfter.getBalance()), new BigDecimal("500.0"));
    }

    @Test
    public void transferNoAccount_IT() throws Exception {
        BigInteger targetAccountId = new BigInteger("3");
        Account targetAccountBefore = entityManager.find(Account.class, targetAccountId);

        String requestBody = "{ \"source-account\": \"\",\n" +
                "\t\"target-account\": \"" + targetAccountId.toString() + "\",\n" +
                "\t\"amount\": \"500.0\" }";

        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Must provide both Accounts\"}");

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

        String requestBody = "{ \"source-account\": \"" + sourceAccountId.toString() + "\",\n" +
                "\t\"target-account\": \"" + targetAccountId.toString() + "\",\n" +
                "\t\"amount\": \"-500.0\" }";

        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Transfer Amount cannot be negative\"}");

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

        Assert.assertNotNull(sourceAccountBefore);
        Assert.assertNotNull(targetAccountBefore);

        String requestBody = "{ \"source-account\": \"" + sourceAccountId.toString() + "\",\n" +
                "\t\"target-account\": \"" + targetAccountId.toString() + "\",\n" +
                "\t\"amount\": \"100500.0\" }";

        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Insufficient Balance\"}");

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

        Assert.assertNotNull(targetAccountBefore);
        Assert.assertNull(imaginaryAccountBefore);

        String requestBody = "{ \"source-account\": \"" + imaginaryAccountId.toString() + "\",\n" +
                "\t\"target-account\": \"" + targetAccountId.toString() + "\",\n" +
                "\t\"amount\": \"500.0\" }";

        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Could not find Account(s)\"}");

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

        String requestBody = "{ \"source-account\": \"" + accountId.toString() +"\",\n" +
                "\t\"target-account\": \"3\",\n" +
                "\t\"amount\": \"500.0\" }";

        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Sender and Recipient is the same Account\"}");

        if (selfTargetedBefore != null) {
            Account selfTargetedAfter = entityManager.find(Account.class, accountId);

            Assert.assertNotNull(selfTargetedAfter);
            Assert.assertEquals(selfTargetedAfter, selfTargetedBefore);
        }
    }

    private static CloseableHttpResponse sendRequestWithBody(String payload) throws Exception {
        HttpPut httpPut = new HttpPut(requestUrl);
        StringEntity entity = new StringEntity(payload, "UTF-8");

        entity.setContentType("application/json");
        httpPut.setEntity(entity);

        CloseableHttpClient client = HttpClients.createDefault();
        return client.execute(httpPut);
    }

    private static Long getRowCountsForTransferOrderTable() {
        Query query = entityManager.createQuery("SELECT count(*) FROM transfer_order");

        return (Long)query.getSingleResult();
    }
}