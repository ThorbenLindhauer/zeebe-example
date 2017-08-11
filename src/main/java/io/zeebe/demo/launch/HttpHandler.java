package io.zeebe.demo.launch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import io.zeebe.client.event.TaskEvent;
import io.zeebe.client.task.TaskController;
import io.zeebe.client.task.TaskHandler;

public class HttpHandler implements TaskHandler
{

    protected final HttpClient httpClient;

    public HttpHandler()
    {
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public void handle(TaskController controller, TaskEvent event)
    {
        Map<String, Object> headers = event.getCustomHeaders();
        String method = (String) headers.get("method");
        String url = (String) headers.get("url");
        String payload = event.getPayload();

        Objects.requireNonNull(method);
        Objects.requireNonNull(url);
        Objects.requireNonNull(payload);

        switch (method)
        {
            case "POST":
                System.out.println("Making request with payload: " + payload);
                String responseBody = makePostRequest(url, payload);
                System.out.println("Received response with payload " + responseBody);
                controller.completeTask(responseBody);
                break;
            default:
                throw new RuntimeException("Unsupported HTTP method " + method);
        }
    }

    protected String makePostRequest(String url, String jsonPayload)
    {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

        final HttpResponse response;
        try
        {
            response = httpClient.execute(post);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not perform http request", e);
        }

        HttpEntity responseBody = response.getEntity();
        Header encoding = responseBody.getContentEncoding();
        Charset responseCharset = encoding != null ?
                Charset.forName(encoding.getValue()) :
                StandardCharsets.UTF_8;

        try (InputStreamReader reader = new InputStreamReader(responseBody.getContent(), responseCharset);
            BufferedReader buf = new BufferedReader(reader))
        {
            return buf.lines().collect(Collectors.joining());
        } catch (Exception e)
        {
            throw new RuntimeException("Could not read http response", e);
        }
    }
}
