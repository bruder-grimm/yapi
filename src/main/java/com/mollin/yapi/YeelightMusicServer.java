package com.mollin.yapi;

import com.mollin.yapi.command.YeelightCommand;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class YeelightMusicServer extends Yeelight {
    private static final int SOCKET_TIMEOUT = 10000;
    private static final int SERVER_PORT = 54321;

    private final Socket serverSocket;
    private final BufferedWriter socketWriter;
    private final InetAddress serverAdress;
    private final int port;

    public YeelightMusicServer() throws IOException {
        this(SERVER_PORT, YeelightEffect.SUDDEN, 100);
    }

    public YeelightMusicServer(int port) throws IOException {
        this(port, YeelightEffect.SUDDEN, 100);
    }

    public YeelightMusicServer(int port, YeelightEffect effect, int duration) throws IOException {
        super(effect, duration);
        this.serverAdress = InetAddress.getLocalHost();
        this.port = port;
        this.serverSocket = new ServerSocket(this.port).accept();

        this.socketWriter = new BufferedWriter(new OutputStreamWriter(this.serverSocket.getOutputStream()));
    }

    @Override String[] sendCommand(YeelightCommand command) throws YeelightSocketException {
        String jsonCommand = command.toJson() + "\r\n";
        send(jsonCommand);
        return new String[]{"ok"};
    }

    private void send(String datas) throws YeelightSocketException {
        try {
            this.socketWriter.write(datas);
            this.socketWriter.flush();
        } catch (Exception e) {
            throw new YeelightSocketException(e);
        }
    }

    public void register(YeelightDevice device) throws YeelightSocketException {
        try {
            YeelightCommand enableMusicMode = new YeelightCommand(
                    "set_music",
                    1,
                    serverAdress.getHostAddress(),
                    this.port
            );

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

    public void close() throws YeelightSocketException {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new YeelightSocketException(e);
        }
    }
}
