package xyz.gosick.design.pattern.reactor.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

/**
 * @author liukeshao
 * @date 2018/11/5 17:00
 */
@Slf4j
public class NioDatagramChannel extends AbstractNioChannel {
    private final int port;

    public NioDatagramChannel(ChannelHandler handler, int port) throws IOException {
        super(DatagramChannel.open(), handler);
        this.port = port;
    }

    @Override
    public int getInterestedOps() {
        return SelectionKey.OP_READ;
    }

    @Override
    public void bind() throws IOException {
        getJavaChannel().socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
        getJavaChannel().configureBlocking(false);
        log.info("Bound UDP socket at port: {}", port);
    }

    @Override
    public DatagramPacket read(SelectionKey key) throws IOException {
        var buffer = ByteBuffer.allocate(1024);
        var sender = ((DatagramChannel) key.channel()).receive(buffer);
        buffer.flip();
        var packet = new DatagramPacket(buffer);
        packet.setSender(sender);
        return packet;
    }

    @Override
    public DatagramChannel getJavaChannel() {
        return (DatagramChannel) super.getJavaChannel();
    }

    @Override
    protected void doWrite(Object data, SelectionKey key) {
        super.write(data, key);
    }

    public static class DatagramPacket {
        private SocketAddress sender;
        private ByteBuffer data;
        private SocketAddress receiver;

        public DatagramPacket(ByteBuffer data) {
            this.data = data;
        }

        public SocketAddress getSender() {
            return sender;
        }

        public void setSender(SocketAddress sender) {
            this.sender = sender;
        }

        public SocketAddress getReceiver() {
            return receiver;
        }

        public void setReceiver(SocketAddress receiver) {
            this.receiver = receiver;
        }

        public ByteBuffer getData() {
            return data;
        }
    }
}
