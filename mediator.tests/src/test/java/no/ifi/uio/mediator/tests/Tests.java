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

/**
 * Integration tests: <br>- forwarding message from Public Infrastructure to Private Infrastructure; <br>-
 * forwarding message from Private Infrastructure to CEGA.
 */
public class Tests {

    private static Channel remoteChannel;
    private static Channel publicChannel;
    private static Channel privateChannel;
    private static final byte[] SENT_BODY = {1, 2, 3};

    /**
     * Initializing connection to 3 queues: Remote (CEGA), Public (OpenStack) and Private (TSD).
     *
     * @throws IOException In case of error.
     * @throws TimeoutException In case of error.
     * @throws NoSuchAlgorithmException In case of error.
     * @throws KeyManagementException In case of error.
     * @throws URISyntaxException In case of error.
     */
    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, NoSuchAlgorithmException,
            KeyManagementException, URISyntaxException {
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

    /**
     * Forwarding message from Public Infrastructure to Private Infrastructure with routing key archived.
     *
     * @throws IOException In case of error.
     * @throws InterruptedException In case of error.
     */
    @Test
    public void fromPublicToPrivateRoutingKeyArchived() throws IOException, InterruptedException {

        publicChannel.basicPublish("lega", "archived", null, SENT_BODY);
        sleep(10000);
        GetResponse getResponse = privateChannel.basicGet("archived", true);
        Assert.assertNotNull(getResponse);
        byte[] receivedBody = getResponse.getBody();
        Assert.assertArrayEquals(SENT_BODY, receivedBody);
    }


    /**
     * Forwarding message from Public Infrastructure to Private Infrastructure with routing key archived.
     *
     * @throws IOException In case of error.
     * @throws InterruptedException In case of error.
     */
    @Test
    public void fromPublicToPrivateRoutingKeyUnarchived() throws IOException, InterruptedException {
        
        publicChannel.basicPublish("lega", "unarchived", null, SENT_BODY);
        sleep(10000);
        GetResponse getResponse = privateChannel.basicGet("unarchived", true);
        Assert.assertNotNull(getResponse);
        byte[] receivedBody = getResponse.getBody();
        Assert.assertArrayEquals(SENT_BODY, receivedBody);
    }
    
    /**
     * Forwarding message from Private Infrastructure to CEGA with routing key completed
     *
     * @throws IOException In case of error.
     * @throws InterruptedException In case of error.
     */
    @Test
    public void fromPrivateToRemoteRoutingKeyCompleted() throws IOException, InterruptedException {
        privateChannel.basicPublish("lega", "completed", null, SENT_BODY);
        sleep(20000);
        GetResponse getResponse = remoteChannel.basicGet("v1.completed", true);
        Assert.assertNotNull(getResponse);
        byte[] receivedBody = getResponse.getBody();
        Assert.assertArrayEquals(SENT_BODY, receivedBody);
    }

    /**
     * Forwarding message from Private Infrastructure to CEGA with routing key incompleted
     *
     * @throws IOException In case of error.
     * @throws InterruptedException In case of error.
     */
    @Test
    public void fromPrivateToRemoteRoutingKeyIncompleted() throws IOException, InterruptedException {
        privateChannel.basicPublish("lega", "incompleted", null, SENT_BODY);
        sleep(20000);
        GetResponse getResponse = remoteChannel.basicGet("v1.incompleted", true);
        Assert.assertNotNull(getResponse);
        byte[] receivedBody = getResponse.getBody();
        Assert.assertArrayEquals(SENT_BODY, receivedBody);
    }

    private void sleep(int milliSeconds) throws InterruptedException {
        Thread.sleep(milliSeconds);
    }

}
