package it;

import com.mollin.yapi.YeelightDevice;
import com.mollin.yapi.YeelightMusicServer;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;

import java.io.IOException;
import java.util.Random;

public class MusicServerTest {

    public static void main(String[] args) throws IOException, YeelightSocketException, YeelightResultErrorException, InterruptedException {
        YeelightDevice device1 = new YeelightDevice("192.168.178.10");
        YeelightDevice device2 = new YeelightDevice("192.168.178.11");
        YeelightMusicServer musicServer = new YeelightMusicServer(54321);

        musicServer.register(device1);

        Random random = new Random();


            musicServer.setPower(true);
            for (int i = 0; i < 100; i++) {
                musicServer.setColorTemperature(1700);
                Thread.sleep(250);
                musicServer.setColorTemperature(6500);
                Thread.sleep(250);
            }
    }
}
