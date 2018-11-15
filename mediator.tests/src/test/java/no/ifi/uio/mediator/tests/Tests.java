package no.ifi.uio.mediator.tests;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class Tests {

    private static Channel remoteChannel;
    private static Channel publicChannel;
    private static Channel privateChannel;

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException, InterruptedException {
        Thread.sleep(10000); // Docker Compose initialization delay

        ConnectionFactory remoteFactory = new ConnectionFactory();
        remoteFactory.setUri("amqp://lega:guest@localhost:5672/lega");
        remoteChannel = remoteFactory.newConnection().createChannel();

        ConnectionFactory publicFactory = new ConnectionFactory();
        publicFactory.setUri("amqp://guest:guest@localhost:5670");
        publicChannel = publicFactory.newConnection().createChannel();

        ConnectionFactory privateFactory = new ConnectionFactory();
        privateFactory.setUri("amqp://guest:guest@localhost:5671");
        privateChannel = privateFactory.newConnection().createChannel();
    }

    @Test
    public void fromPublicToPrivate() throws IOException, InterruptedException {
        byte[] sentBody = {1, 2, 3};
        publicChannel.basicPublish("lega", "archived", null, sentBody);
        Thread.sleep(60000); // for Travis
        GetResponse getResponse = privateChannel.basicGet("archived", true);
        Assert.assertNotNull(getResponse);
        byte[] receivedBody = getResponse.getBody();
        Assert.assertArrayEquals(sentBody, receivedBody);
    }

    @Test
    public void fromPrivateToRemote() throws IOException, InterruptedException {
        byte[] sentBody = {1, 2, 3};
        privateChannel.basicPublish("lega", "completed", null, sentBody);
        Thread.sleep(60000); // for Travis
        GetResponse getResponse = remoteChannel.basicGet("v1.completed", true);
        Assert.assertNotNull(getResponse);
        byte[] receivedBody = getResponse.getBody();
        Assert.assertArrayEquals(sentBody, receivedBody);
    }

}
