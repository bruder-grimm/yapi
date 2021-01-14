package com.mollin.yapi;

import com.mollin.yapi.command.YeelightCommand;
import com.mollin.yapi.enumeration.YeelightAdjustAction;
import com.mollin.yapi.enumeration.YeelightAdjustProperty;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.enumeration.YeelightProperty;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;
import com.mollin.yapi.flow.YeelightFlow;
import com.mollin.yapi.result.YeelightResultError;
import com.mollin.yapi.result.YeelightResultOk;
import com.mollin.yapi.socket.YeelightSocketHolder;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.mollin.yapi.utils.YeelightUtils.clamp;
import static com.mollin.yapi.utils.YeelightUtils.clampAndComputeRGBValue;

/**
 * Either a Server (controls like a virtual device) or a simple, single light
 */
public abstract class Yeelight {
    /** Device effect setting for commands */
    private YeelightEffect effect;
    /** Device effect duration setting for commands */
    private int duration;

    public Yeelight(YeelightEffect effect, int duration) {
        this.effect = effect;
        this.duration = duration;
    }

    /** Send command to device
     *  @param command Command to send
     *  @return Raw result array
     *  @throws YeelightSocketException when socket error occurs
     *  @throws YeelightResultErrorException when command result is an error */
    abstract String[] sendCommand(YeelightCommand command) throws YeelightSocketException, YeelightResultErrorException;

    /**
     * Setter for Yeelight device effect
     * @param effect Effect to set (if null, 'sudden' is chosen)
     */
    public void setEffect(YeelightEffect effect) {
        this.effect = effect == null ? YeelightEffect.SUDDEN : effect;
    }

    /**
     * Setter for Yeelight device effect duration
     * @param duration Duration to set (&gt;= 0)
     */
    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }


    /**
     * Change the device color temperature
     * @param colorTemp Color temperature value [1700;6500]
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setColorTemperature(int colorTemp) throws YeelightResultErrorException, YeelightSocketException {
        colorTemp = clamp(colorTemp, 1700, 6500);
        YeelightCommand command = new YeelightCommand("set_ct_abx", colorTemp, this.effect.getValue(), this.duration);
        this.sendCommand(command);
    }

    /**
     * Change the device color
     * @param r Red value [0;255]
     * @param g Green value [0;255]
     * @param b Blue value [0;255]
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setRGB(int r, int g, int b) throws YeelightResultErrorException, YeelightSocketException {
        int rgbValue = clampAndComputeRGBValue(r, g, b);
        YeelightCommand command = new YeelightCommand("set_rgb", rgbValue, this.effect.getValue(), this.duration);
        this.sendCommand(command);
    }

    /**
     * Change hue and sat of the device
     * @param hue Hue value [0;359]
     * @param sat Sat value [0;100]
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setHSV(int hue, int sat) throws YeelightResultErrorException, YeelightSocketException {
        hue = clamp(hue, 0, 359);
        sat = clamp(sat, 0, 100);
        YeelightCommand command = new YeelightCommand("set_hsv", hue, sat, this.effect.getValue(), this.duration);
        this.sendCommand(command);
    }

    /**
     * Change the device brightness
     * @param brightness Brightness value [1;100]
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setBrightness(int brightness) throws YeelightResultErrorException, YeelightSocketException {
        brightness = clamp(brightness, 1, 100);
        YeelightCommand command = new YeelightCommand("set_bright", brightness, this.effect.getValue(), this.duration);
        this.sendCommand(command);
    }

    /**
     * Switch on or off the device power
     * @param power Power value (true = on, false = off)
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setPower(boolean power) throws YeelightResultErrorException, YeelightSocketException {
        String powerStr = power ? "on" : "off";
        YeelightCommand command = new YeelightCommand("set_power", powerStr, this.effect.getValue(), this.duration);
        this.sendCommand(command);
    }

    /**
     * Toggle the device power
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void toggle() throws YeelightResultErrorException, YeelightSocketException {
        YeelightCommand command = new YeelightCommand("toggle");
        this.sendCommand(command);
    }

    /**
     * Start a flow
     * @param flow Flow to start
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void startFlow(YeelightFlow flow) throws YeelightResultErrorException, YeelightSocketException {
        YeelightCommand command = new YeelightCommand("start_cf", flow.createCommandParams());
        this.sendCommand(command);
    }

    /**
     * Stop a flow
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void stopFlow() throws YeelightResultErrorException, YeelightSocketException {
        YeelightCommand command = new YeelightCommand("stop_cf");
        this.sendCommand(command);
    }
}
