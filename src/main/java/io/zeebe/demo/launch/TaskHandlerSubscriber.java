package io.zeebe.demo.launch;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import io.zeebe.client.ClientProperties;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.task.TaskHandler;
import io.zeebe.client.task.TaskSubscription;

public class TaskHandlerSubscriber
{

    protected ZeebeClient client;
    protected String taskType;
    protected String topic;
    protected String workerId;
    protected TaskHandler handler;

    public TaskHandlerSubscriber(String[] args, String defaultType, TaskHandler handler)
    {
        Properties properties = new Properties();
        this.topic = "default-topic";
        this.taskType = defaultType;
        if (args.length > 0)
        {
            properties.put(ClientProperties.BROKER_CONTACTPOINT, args[0]);
        }
        if (args.length > 1)
        {
            topic = args[1];
        }
        if (args.length > 2)
        {
            taskType = args[2];
        }

        this.client = ZeebeClient.create(properties);
        client.connect();

        this.workerId = UUID.randomUUID().toString();
        this.handler = handler;
    }

    public TaskSubscription openSubscription()
    {
        return client.tasks().newTaskSubscription(topic)
            .taskType(taskType)
            .lockOwner(workerId)
            .lockTime(Duration.ofSeconds(30))
            .handler(handler)
            .open();
    }

    public void close()
    {
        client.close();
    }
}
