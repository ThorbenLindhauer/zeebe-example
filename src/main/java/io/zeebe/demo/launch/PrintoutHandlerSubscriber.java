package io.zeebe.demo.launch;

import java.util.Scanner;

import io.zeebe.client.task.TaskSubscription;

public class PrintoutHandlerSubscriber
{

    public static void main(String[] args)
    {
        TaskHandlerSubscriber subscriber = new TaskHandlerSubscriber(args, "print-payload", new PrintPayloadHandler());
        TaskSubscription subscription = subscriber.openSubscription();

        Scanner input = new Scanner(System.in);

        while (!"exit".equals(input.nextLine()))
        {
        }

        input.close();
        subscription.close();
        subscriber.close();
    }
}
