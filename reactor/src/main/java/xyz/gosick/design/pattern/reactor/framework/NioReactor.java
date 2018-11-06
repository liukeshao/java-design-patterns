package xyz.gosick.design.pattern.reactor.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author liukeshao
 * @date 2018/11/5 14:16
 */
@Slf4j
public class NioReactor {

    private final Selector selector;
    private final Dispatcher dispatcher;

    private final Queue<Runnable> pendingCommands = new ConcurrentLinkedDeque<>();
    private final ExecutorService reactorMain = Executors.newSingleThreadExecutor();

    public NioReactor(Dispatcher dispatcher) throws IOException {
        this.selector = Selector.open();
        this.dispatcher = dispatcher;
    }

    public void start() {
        reactorMain.execute(() -> {
            try {
                log.info("Reactor started, waiting for events...");
                eventLoop();
            } catch (IOException e) {
                log.error("exception in event loop", e);
            }
        });
    }

    public void stop() throws InterruptedException, IOException {
        reactorMain.shutdownNow();
        selector.wakeup();
        reactorMain.awaitTermination(4, TimeUnit.SECONDS);
        selector.close();
        log.info("Reactor stopped");
    }

    public NioReactor registerChannel(AbstractNioChannel channel) throws IOException {
        var key = channel.getJavaChannel().register(selector, channel.getInterestedOps());
        key.attach(channel);
        channel.setReactor(this);
        return this;
    }

    private void eventLoop() throws IOException {
        while (true) {
            if (Thread.interrupted()) {
                break;
            }
            processPendingCommands();
            selector.select();
            var keys = selector.selectedKeys();
            var iterator = keys.iterator();
            while (iterator.hasNext()) {
                var key = iterator.next();
                if (!key.isValid()) {
                    iterator.remove();
                    continue;
                }
                processKey(key);
            }
            keys.clear();
        }
    }

    private void processPendingCommands() {
        var iterator = pendingCommands.iterator();
        while (iterator.hasNext()) {
            var command = iterator.next();
            command.run();
            iterator.remove();
        }
    }

    private void processKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            onChannelAcceptable(key);
        } else if (key.isReadable()) {
            onChannelReadable(key);
        } else if (key.isWritable()) {
            onChannelWritable(key);
        }
    }


    private static void onChannelWritable(SelectionKey key) throws IOException {
        var channel = (AbstractNioChannel) key.attachment();
        channel.flush(key);
    }

    private void onChannelReadable(SelectionKey key) {
        try {
            var readObject = ((AbstractNioChannel) key.attachment()).read(key);
            dispatchEvent(key, readObject);
        } catch (IOException e) {
            try {
                key.channel().close();
            } catch (IOException e1) {
                log.error("error closing channel", e1);
            }
        }
    }

    private void dispatchEvent(SelectionKey key, Object readObject) {
        dispatcher.onChannelReadEvent((AbstractNioChannel) key.attachment(), readObject, key);
    }

    private void onChannelAcceptable(SelectionKey key) throws IOException {
        var serverSocketChannel = (ServerSocketChannel) key.channel();
        var socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        var readKey = socketChannel.register(selector, SelectionKey.OP_READ);
        readKey.attach(key.attachment());
    }

    public void changeOps(SelectionKey key, int interestedOps) {
        pendingCommands.add(new ChangeKeyOpsCommand(key, interestedOps));
        selector.wakeup();
    }

    class ChangeKeyOpsCommand implements Runnable {

        private SelectionKey key;
        private int interestedOps;

        public ChangeKeyOpsCommand(SelectionKey key, int interestedOps) {
            this.key = key;
            this.interestedOps = interestedOps;
        }

        @Override
        public void run() {
            key.interestOps(interestedOps);
        }

        @Override
        public String toString() {
            return "Change of ops to: " + interestedOps;
        }
    }
}
