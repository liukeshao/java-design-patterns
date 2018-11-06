package xyz.gosick.design.pattern.reactor.app;

import lombok.extern.slf4j.Slf4j;
import xyz.gosick.design.pattern.reactor.framework.NioDatagramChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liukeshao
 * @date 2018/11/6 09:40
 */
@Slf4j
public class AppClient {
    private final ExecutorService service = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws IOException {
        new AppClient().start();
    }

    public void start() throws IOException {
        log.info("Starting logging clients");
        service.execute(new TcpLoggingClient("Client 1", 6666));
        service.execute(new TcpLoggingClient("Client 2", 6667));
        service.execute(new UdpLoggingClient("Client 3", 6668));
        service.execute(new UdpLoggingClient("Client 4", 6668));
    }

    private static void artificialDelayOf(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("sleep interrupted", e);
        }
    }


    static class TcpLoggingClient implements Runnable {
        private final int serverPort;
        private final String clientName;

        public TcpLoggingClient(String clientName, int serverPort) {
            this.serverPort = serverPort;
            this.clientName = clientName;
        }

        @Override
        public void run() {
            try (var socket = new Socket(InetAddress.getLocalHost(), serverPort)) {
                OutputStream outputStream = socket.getOutputStream();
                var writer = new PrintWriter(outputStream);
                sendLogRequests(writer, socket.getInputStream());
            } catch (IOException e) {
                log.error("error sending requests", e);
                throw new RuntimeException(e);
            }
        }

        private void sendLogRequests(PrintWriter writer, InputStream inputStream) throws IOException {
            for (int i = 0; i < 4; i++) {
                writer.println(clientName + " - Log request: " + i);
                writer.flush();

                var data = new byte[1024];
                var read = inputStream.read(data, 0, data.length);
                if (read == 0) {
                    log.info("Read zero bytes");
                } else {
                    log.info(new String(data, 0, read));
                }
            }
            artificialDelayOf(100);
        }
    }

    static class UdpLoggingClient implements Runnable {

        private final String clientName;
        private final InetSocketAddress remoteAddress;

        public UdpLoggingClient(String clientName, int port) throws UnknownHostException {
            this.clientName = clientName;
            this.remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(), port);
        }

        @Override
        public void run() {
            try (var socket = new DatagramSocket()) {
                for (int i = 0; i < 4; i++) {
                    String message = clientName + " - Log request: " + i;
                    var request = new DatagramPacket(message.getBytes(), message.getBytes().length, remoteAddress);
                    socket.send(request);
                    var data = new byte[1024];
                    var reply = new DatagramPacket(data, data.length);
                    socket.receive(reply);
                    if (reply.getLength() == 0) {
                        log.info("Read zero bytes");
                    } else {
                        log.info(new String(reply.getData(), 0, reply.getLength()));
                    }
                    artificialDelayOf(100);
                }
            } catch (IOException e) {
                log.error("error sending packets", e);
            }
        }
    }

}
