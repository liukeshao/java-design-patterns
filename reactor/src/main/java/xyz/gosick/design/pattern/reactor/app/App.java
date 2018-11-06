package xyz.gosick.design.pattern.reactor.app;

import lombok.extern.slf4j.Slf4j;
import xyz.gosick.design.pattern.reactor.framework.AbstractNioChannel;
import xyz.gosick.design.pattern.reactor.framework.ChannelHandler;
import xyz.gosick.design.pattern.reactor.framework.Dispatcher;
import xyz.gosick.design.pattern.reactor.framework.NioDatagramChannel;
import xyz.gosick.design.pattern.reactor.framework.NioReactor;
import xyz.gosick.design.pattern.reactor.framework.NioServerSocketChannel;
import xyz.gosick.design.pattern.reactor.framework.ThreadPoolDispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liukeshao
 * @date 2018/11/6 08:51
 */
@Slf4j
public class App {
    private NioReactor reactor;
    private List<AbstractNioChannel> channels = new ArrayList<>();
    private Dispatcher dispatcher;

    public App(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static void main(String[] args) throws IOException {
        new App(new ThreadPoolDispatcher(2)).start();
    }

    public void start() throws IOException {
        reactor = new NioReactor(dispatcher);
        var loggingHandler = new LoggingHandler();
        reactor.registerChannel(tcpChannel(6667, loggingHandler)).registerChannel(udpChannel(6668, loggingHandler)).start();
    }

    private AbstractNioChannel tcpChannel(int port, ChannelHandler handler) throws IOException {
        var channel = new NioServerSocketChannel(handler, port);
        channel.bind();
        channels.add(channel);
        return channel;
    }

    private AbstractNioChannel udpChannel(int port, ChannelHandler handler) throws IOException {
        var channel = new NioDatagramChannel(handler, port);
        channel.bind();
        channels.add(channel);
        return channel;
    }


}
