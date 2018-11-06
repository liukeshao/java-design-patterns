package xyz.gosick.design.pattern.reactor.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author liukeshao
 * @date 2018/11/5 17:20
 */
@Slf4j
public class NioServerSocketChannel extends AbstractNioChannel {

    private final int port;

    public NioServerSocketChannel(ChannelHandler handler, int port) throws IOException {
        super(ServerSocketChannel.open(), handler);
        this.port = port;
    }

    @Override
    public int getInterestedOps() {
        return SelectionKey.OP_ACCEPT;
    }

    @Override
    public ServerSocketChannel getJavaChannel() {
        return (ServerSocketChannel) super.getJavaChannel();
    }

    @Override
    public void bind() throws IOException {
        getJavaChannel().socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
        getJavaChannel().configureBlocking(false);
        log.info("Bound TCP socket at port: {}", port);
    }

    @Override
    public ByteBuffer read(SelectionKey key) throws IOException {
        var socketChannel = (SocketChannel) key.channel();
        var buffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(buffer);
        buffer.flip();
        if (read == -1) {
            throw new IOException("Socket closed");
        }
        return buffer;
    }

    @Override
    protected void doWrite(Object pendingWrite, SelectionKey key) throws IOException {
        var pendingBuffer = (ByteBuffer) pendingWrite;
        ((SocketChannel) key.channel()).write(pendingBuffer);
    }
}
