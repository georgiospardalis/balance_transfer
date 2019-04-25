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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class EndToEndIT {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    private static String requestUrl;

    @BeforeClass
    public static void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("TestBalanceTransferManagement");
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
        String requestBody = "{ \"source-account\": \"2\",\n" +
                "\t\"target-account\": \"3\",\n" +
                "\t\"amount\": \"500.0\" }";
        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertTrue(responseBody.toLowerCase().contains("successful transaction"));
    }

    @Test
    public void transferNoAccount_IT() throws Exception {
        String requestBody = "{ \"source-account\": \"\",\n" +
                "\t\"target-account\": \"3\",\n" +
                "\t\"amount\": \"500.0\" }";
        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertTrue(responseBody.equals("{\"transfer-status\":\"Must provide both Accounts\"}"));
    }

    @Test
    public void transferAmountNegative_IT() throws Exception {
        String requestBody = "{ \"source-account\": \"1\",\n" +
                "\t\"target-account\": \"3\",\n" +
                "\t\"amount\": \"-500.0\" }";
        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Transfer Amount cannot be negative\"}");
    }


    @Test
    public void transferInsufficientBalance_IT() throws Exception {
        String requestBody = "{ \"source-account\": \"1\",\n" +
                "\t\"target-account\": \"3\",\n" +
                "\t\"amount\": \"100500.0\" }";
        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Insufficient Balance\"}");
    }

    @Test
    public void transferFromNonExistentAccount_IT() throws Exception {
        String requestBody = "{ \"source-account\": \"15555\",\n" +
                "\t\"target-account\": \"3\",\n" +
                "\t\"amount\": \"500.0\" }";
        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Could not find Account(s)\"}");
    }

    @Test
    public void transferSenderRecipientAreTheSame_IT() throws Exception {
        String requestBody = "{ \"source-account\": \"3\",\n" +
                "\t\"target-account\": \"3\",\n" +
                "\t\"amount\": \"500.0\" }";
        CloseableHttpResponse closeableHttpResponse = sendRequestWithBody(requestBody);

        Assert.assertEquals(closeableHttpResponse.getStatusLine().getStatusCode(), 200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
        String responseBody = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(responseBody, "{\"transfer-status\":\"Sender and Recipient is the same Account\"}");
    }

    private static CloseableHttpResponse sendRequestWithBody(String payload) throws Exception {
        String url = "http://localhost:28960/transaction/balance_transfer";
        HttpPut httpPut = new HttpPut(url);
        StringEntity entity = new StringEntity(payload, "UTF-8");

        entity.setContentType("application/json");
        httpPut.setEntity(entity);

        CloseableHttpClient client = HttpClients.createDefault();
        return client.execute(httpPut);
    }
}