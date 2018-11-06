package xyz.gosick.design.pattern.reactor.framework;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author liukeshao
 * @date 2018/11/5 14:29
 */
public abstract class AbstractNioChannel {
    private final SelectableChannel channel;
    private final ChannelHandler handler;
    private final Map<SelectableChannel, Queue<Object>> channelToPendingWrites = new ConcurrentHashMap<>();
    private NioReactor reactor;

    protected AbstractNioChannel(SelectableChannel channel, ChannelHandler handler) {
        this.channel = channel;
        this.handler = handler;
    }

    void setReactor(NioReactor reactor) {
        this.reactor = reactor;
    }

    public SelectableChannel getJavaChannel() {
        return channel;
    }

    public abstract int getInterestedOps();

    public abstract void bind() throws IOException;

    public abstract Object read(SelectionKey key) throws IOException;

    public ChannelHandler getHandler() {
        return handler;
    }

    void flush(SelectionKey key) throws IOException {
        var pendingWrites = channelToPendingWrites.get(key.channel());
        while (true) {
            var pendingWrite = pendingWrites.poll();
            if (pendingWrite == null) {
                reactor.changeOps(key, SelectionKey.OP_READ);
                break;
            }
            doWrite(pendingWrite, key);
        }
    }

    protected abstract void doWrite(Object pendingWrite, SelectionKey key) throws IOException;

    public void write(Object data, SelectionKey key) {
        var pendingWrites = this.channelToPendingWrites.get(key.channel());
        if (pendingWrites == null) {
            synchronized (this.channelToPendingWrites) {
                pendingWrites = this.channelToPendingWrites.get(key.channel());
                if (pendingWrites == null) {
                    pendingWrites = new ConcurrentLinkedDeque<>();
                    this.channelToPendingWrites.put(key.channel(), pendingWrites);
                }
            }
        }
        pendingWrites.add(data);
        reactor.changeOps(key, SelectionKey.OP_WRITE);
    }


}
