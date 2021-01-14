package com.mollin.yapi;

import com.mollin.yapi.command.YeelightCommand;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class YeelightMusicServer extends Yeelight {
    private static final int SERVER_PORT = 54345;

    private final InetAddress serverAdress;
    private final int portOffset;

    private final List<BufferedWriter> services = new LinkedList<>();

    public YeelightMusicServer() throws IOException {
        this(SERVER_PORT, YeelightEffect.SUDDEN, 100);
    }

    public YeelightMusicServer(int port) throws IOException {
        this(port, YeelightEffect.SUDDEN, 100);
    }

    public YeelightMusicServer(int portOffset, YeelightEffect effect, int duration) throws IOException {
        super(effect, duration);
        this.serverAdress = InetAddress.getLocalHost();
        this.portOffset = portOffset;
    }

    @Override String[] sendCommand(YeelightCommand command) throws YeelightSocketException, YeelightResultErrorException {
        String jsonCommand = command.toJson() + "\r\n";
        for (BufferedWriter service : this.services) {
            try {
                Logger.info("sending command {}", jsonCommand);
                service.write(jsonCommand);
                service.flush();
            } catch (IOException e) {
                throw new YeelightSocketException(e);
            }
        }

        return new String[]{};
    }

    public void register(YeelightDevice device) throws YeelightSocketException {
        try {
            int port = this.portOffset + services.size();
            YeelightCommand enableMusicMode = new YeelightCommand(
                    "set_music",
                    1,
                    serverAdress.getHostAddress(),
                    port
            );

            createServer(port);

            String[] result = device.sendCommand(enableMusicMode);
            if (!result[0].equals("ok")) {
                throw new Exception(String.format(
                        "Yeelight %s couldn't connect to server. Did you start the server?",
                        device.getIp().getHostAddress()
                ));
            }

        } catch (Exception e) {
            throw new YeelightSocketException(e);
        }
    }

    public void createServer(int port) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                Socket socket = serverSocket.accept();
                Logger.info("Yeelight connected to server at port " + port);
                services.add(
                        new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream())))
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void close() throws IOException {
        for (BufferedWriter bufferedWriter : this.services) {
            bufferedWriter.close();
            Logger.debug("Server closed");
        }
    }
}
