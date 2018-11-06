package xyz.gosick.design.pattern.reactor.app;

import lombok.extern.slf4j.Slf4j;
import xyz.gosick.design.pattern.reactor.framework.AbstractNioChannel;
import xyz.gosick.design.pattern.reactor.framework.ChannelHandler;
import xyz.gosick.design.pattern.reactor.framework.NioDatagramChannel;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * @author liukeshao
 * @date 2018/11/6 08:59
 */
@Slf4j
public class LoggingHandler implements ChannelHandler {

    private static final byte[] ACK = "Data logged successfully".getBytes();

    @Override
    public void handleChannelRead(AbstractNioChannel channel, Object readObject, SelectionKey key) {
        if (readObject instanceof ByteBuffer) {
            doLogging((ByteBuffer) readObject);
            sendReply(channel, key);
        } else if (readObject instanceof NioDatagramChannel.DatagramPacket) {
            var datagram = (NioDatagramChannel.DatagramPacket) readObject;
            doLogging(datagram.getData());
            sendReply(channel, datagram, key);
        } else {
            throw new IllegalStateException("Unknown data received");
        }

    }

    private void sendReply(AbstractNioChannel channel, SelectionKey key) {
        var buffer = ByteBuffer.wrap(ACK);
        channel.write(buffer, key);
    }

    private static void sendReply(AbstractNioChannel channel, NioDatagramChannel.DatagramPacket incomingPacket, SelectionKey key) {


    }

    private static void doLogging(ByteBuffer data) {
        log.info(new String(data.array()), 0, data.limit());
    }
}
