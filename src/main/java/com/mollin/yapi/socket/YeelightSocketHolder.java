package com.mollin.yapi.socket;

import com.mollin.yapi.exception.YeelightSocketException;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket holder for manage exception, stream, ...
 */
public class YeelightSocketHolder {
    private static final int SOCKET_TIMEOUT = 1000;

    private final InetAddress ip;

    public void setPort(int port) {
        this.port = port;
    }

    private int port;
    private final Socket socket;
    private final BufferedReader socketReader;
    private final BufferedWriter socketWriter;

    /**
     * Constructor for socket holder
     * Create socket and associated streams (reader + writer)
     * @param ip Socket holder IP
     * @param port Socket holder port
     * @throws YeelightSocketException when socket error occurs
     */
    public YeelightSocketHolder(String ip, int port) throws YeelightSocketException {
        try {
            this.ip = InetAddress.getByName(ip);
            this.port = port;

            InetSocketAddress inetSocketAddress = new InetSocketAddress(this.ip, port);
            socket = new Socket();
            socket.connect(inetSocketAddress, YeelightSocketHolder.SOCKET_TIMEOUT);
            socket.setKeepAlive(true);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            this.socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (Exception e) {
            throw new YeelightSocketException(e);
        }
    }

    public InetAddress getIp() {
        return this.ip;
    }

    /**
     * Send datas on the socket
     * @param datas Datas to send
     * @throws YeelightSocketException when socket error occurs
     */
    public void send(String datas) throws YeelightSocketException {
        try {
            Logger.debug("{} sent to {}:{}", datas, this.ip, this.port);
            this.socketWriter.write(datas);
            this.socketWriter.flush();
        } catch (Exception e) {
            throw new YeelightSocketException(e);
        }
    }

    /**
     * Read line on the socket (terminated with \r, \n or \r\n)
     * @return The line read
     * @throws YeelightSocketException when socket error occurs
     */
    public String readLine() throws YeelightSocketException {
        try {
            String datas = this.socketReader.readLine();
            Logger.debug("{} received from {}:{}", datas, this.ip, this.port);
            return datas;
        } catch (Exception e) {
            throw new YeelightSocketException(e);
        }
    }

    public void close() throws IOException {
        this.socketWriter.close();
        this.socketReader.close();
        this.socket.close();
    }
}
