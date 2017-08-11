package io.zeebe.demo.launch;

import java.util.Properties;

import io.zeebe.client.WorkflowsClient;
import io.zeebe.client.ZeebeClient;

public class DemoProcessDeployer
{
    public static void main(String[] args)
    {
        ZeebeClient client = ZeebeClient.create(new Properties());
        client.connect();

        String inputMessage = "Hello, Zeebe!";
        WorkflowsClient workflowsClient = client.workflows();

        workflowsClient.deploy("default-topic")
            .resourceFromClasspath("test-process.bpmn")
            .execute();

        workflowsClient.create("default-topic")
            .bpmnProcessId("http-process")
            .latestVersion()
            .payload("{\"message\":\"" + inputMessage + "\"}")
            .execute();

        client.close();
    }

}
