package com.mollin.yapi;

import com.mollin.yapi.command.YeelightCommand;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.exception.YeelightSocketException;
import com.mollin.yapi.socket.YeelightSocketHolder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

public class YeelightMusicServer extends Yeelight {
    private static final int SERVER_PORT = 54321;

    private final List<YeelightSocketHolder> connectedDevices = new LinkedList<>();
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
    }

    @Override String[] sendCommand(YeelightCommand command) throws YeelightSocketException {
        String jsonCommand = command.toJson() + "\r\n";
        send(jsonCommand);
        return new String[]{"ok"};
    }

    private void send(String datas) {
        this.connectedDevices.forEach(device -> {
            try {
                device.send(datas);
            } catch (YeelightSocketException ignored) {  }
        });
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
            // there will be no responses from here on out
            connectedDevices.add(device.getSocketHolder());
        } catch (Exception e) {
            throw new YeelightSocketException(e);
        }
    }

    public void close() {
        connectedDevices.forEach(device -> {
            try {
                device.close();
            } catch (IOException ignored) { }
        });
    }
}
