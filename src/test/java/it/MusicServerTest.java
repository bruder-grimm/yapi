package it;

import com.mollin.yapi.YeelightDevice;
import com.mollin.yapi.YeelightMusicServer;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;
import com.mollin.yapi.flow.YeelightFlow;
import com.mollin.yapi.flow.transition.YeelightColorTransition;
import com.mollin.yapi.flow.transition.YeelightSleepTransition;

import java.io.IOException;
import java.util.Random;

import static com.mollin.yapi.enumeration.YeelightFlowAction.RECOVER;
import static com.mollin.yapi.flow.YeelightFlow.INFINITE_COUNT;

public class MusicServerTest {

    public static void main(String[] args) throws IOException, YeelightSocketException, YeelightResultErrorException, InterruptedException {
        YeelightDevice device1 = new YeelightDevice("192.168.178.10");
        YeelightDevice device2 = new YeelightDevice("192.168.178.11");
        YeelightMusicServer musicServer = new YeelightMusicServer(54321);

        musicServer.register(device1);

        int bpm = 137;
        int interval = 60000 / bpm; // there are 60k ms in a minute

        System.out.println(interval / 16);

        musicServer.setPower(true);
        musicServer.startFlow(
                new YeelightFlow(10, RECOVER,
                        new YeelightColorTransition(255, 255, 255, 5 , 0),
                        new YeelightColorTransition(255, 255, 255, 5, 100)
                )
        );
    }
}
