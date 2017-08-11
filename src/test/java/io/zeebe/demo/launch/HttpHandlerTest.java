package io.zeebe.demo.launch;

import static io.zeebe.test.util.TestUtil.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.broker.Broker;
import io.zeebe.client.TasksClient;
import io.zeebe.client.TopicsClient;
import io.zeebe.client.WorkflowsClient;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.event.WorkflowInstanceEvent;

public class HttpHandlerTest
{

    protected Broker broker;
    protected ZeebeClient client;

    @Before
    public void setUp()
    {
        broker = new Broker((String) null);
        client = ZeebeClient.create(new Properties());
        client.connect();
    }

    @After
    public void tearDown()
    {
        client.close();
        broker.close();
    }

    @Test
    public void shouldInvokeHttpService() throws InterruptedException, IOException
    {
        // given
        WorkflowsClient workflowsClient = client.workflows();
        TasksClient tasksClient = client.tasks();

        workflowsClient.deploy("default-topic")
            .resourceFromClasspath("test-process.bpmn")
            .execute();

        String workerId = UUID.randomUUID().toString();
        tasksClient.newTaskSubscription("default-topic")
            .lockOwner(workerId)
            .lockTime(Duration.ofSeconds(30))
            .taskType("http-request")
            .handler(new HttpHandler())
            .open();

        tasksClient.newTaskSubscription("default-topic")
            .lockOwner(workerId)
            .lockTime(Duration.ofSeconds(30))
            .taskType("print-payload")
            .handler(new PrintPayloadHandler())
            .open();

        // when
        String inputMessage = "Hello, Zeebe!";
        workflowsClient.create("default-topic")
            .bpmnProcessId("http-process")
            .latestVersion()
            .payload("{\"message\":\"" + inputMessage + "\"}")
            .execute();

        // then
        TopicsClient topicsClient = client.topics();
        AtomicReference<WorkflowInstanceEvent> endEventRef = new AtomicReference<>();
        topicsClient.newSubscription("default-topic")
            .name("test")
            .workflowInstanceEventHandler(e ->
            {
                if ("WORKFLOW_INSTANCE_COMPLETED".equals(e.getState()))
                {
                    endEventRef.set(e);
                }
            })
            .open();

        waitUntil(() -> endEventRef.get() != null);

        WorkflowInstanceEvent endEvent = endEventRef.get();
        assertThat(endEvent.getPayload());
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonTree = objectMapper.readTree(endEvent.getPayload());
        JsonNode shoutOutMessage = jsonTree.get("shoutout-message");
        assertThat(shoutOutMessage).isNotNull();
        assertThat(shoutOutMessage.isTextual()).isTrue();
        assertThat(shoutOutMessage.asText()).isEqualTo(inputMessage.toUpperCase());
    }
}
