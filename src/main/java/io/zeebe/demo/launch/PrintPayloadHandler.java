package io.zeebe.demo.launch;

import io.zeebe.client.event.TaskEvent;
import io.zeebe.client.task.TaskController;
import io.zeebe.client.task.TaskHandler;

public class PrintPayloadHandler implements TaskHandler
{
    @Override
    public void handle(TaskController controller, TaskEvent task)
    {
        System.out.println(task.getPayload());
    }
}
