package pardalis;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RequestUtils {
    private RequestUtils() {

    }

    public static CloseableHttpResponse sendPUTRequestWithJsonBody(String requestUrl, String payload) throws Exception {
        HttpPut httpPut = new HttpPut(requestUrl);
        StringEntity entity = new StringEntity(payload, "UTF-8");
        CloseableHttpClient client = HttpClients.createDefault();

        entity.setContentType("application/json");
        httpPut.setEntity(entity);

        return client.execute(httpPut);
    }

    public static String prepareRequestBodyJson(String sourceAccountId, String targetAccountId, String amount) {
        return "{ \"source-account\": \"" + sourceAccountId + "\",\n" +
                "\t\"target-account\": \"" + targetAccountId + "\",\n" +
                "\t\"amount\": \"" + amount + "\" }";
    }
}