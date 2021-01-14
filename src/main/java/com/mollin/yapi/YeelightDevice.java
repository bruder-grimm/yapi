package com.mollin.yapi;

import com.mollin.yapi.command.YeelightCommand;
import com.mollin.yapi.enumeration.YeelightAdjustAction;
import com.mollin.yapi.enumeration.YeelightAdjustProperty;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.enumeration.YeelightProperty;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;
import com.mollin.yapi.result.YeelightResultError;
import com.mollin.yapi.result.YeelightResultOk;
import com.mollin.yapi.socket.YeelightSocketHolder;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class controlling a Yeelight device (command sending).
 */
public class YeelightDevice extends Yeelight {
    /** Socket holder for sending commands (and receive results) */
    private final YeelightSocketHolder socketHolder;
    private final InetAddress ip;

    InetAddress getIp() {
        return ip;
    }

    /** Constructor for Yeelight device
     *  @param ip Yeelight device IP
     *  @param port Yeelight device port
     *  @param effect Device effect setting for commands
     *  @param duration Device effect duration setting for commands
     *  @throws YeelightSocketException when a socket error occurs */
    public YeelightDevice(String ip, int port, YeelightEffect effect, int duration) throws YeelightSocketException {
        super(effect, duration);
        this.socketHolder = new YeelightSocketHolder(ip, port);
        this.ip = this.socketHolder.getIp();
    }

    /**
     * Constructor for Yeelight device. Initial effect set to 'sudden'
     * @param ip Yeelight device IP
     * @param port Yeelight device port
     * @throws YeelightSocketException when a socket error occurs
     */
    public YeelightDevice(String ip, int port) throws YeelightSocketException {
        this(ip, port, YeelightEffect.SUDDEN, 0);
    }

    /**
     * Constructor for Yeelight device. Port set to 55443 and initial effect set to 'sudden'
     * @param ip Yeelight device IP
     * @throws YeelightSocketException when a socket error occurs
     */
    public YeelightDevice(String ip) throws YeelightSocketException {
        this(ip, 55443);
    }

    /**
     * Read socket until result with corresponding ID is found
     * @param id ID of result to find
     * @return Raw result array
     * @throws YeelightSocketException when socket error occurs
     * @throws YeelightResultErrorException when result founds is an error
     */
    private String[] readUntilResult(int id) throws YeelightSocketException, YeelightResultErrorException {
        do {
            String datas = this.socketHolder.readLine();
            // parsing datas
            Optional<YeelightResultOk> okResult = YeelightResultOk.from(datas);
            Optional<YeelightResultError> errorResult = YeelightResultError.from(datas);
            // check results
            if (okResult.isPresent() && okResult.get().getId() == id) {
                return okResult.get().getResult();
            } else if (errorResult.isPresent() && errorResult.get().getId() == id) {
                throw errorResult.get().getException();
            }
        } while (true);
    }

    /**
     * Send command on socket
     * @param command Command to send
     * @return Raw result array
     * @throws YeelightSocketException when socket error occurs
     * @throws YeelightResultErrorException when command result is an error
     */
    @Override String[] sendCommand(YeelightCommand command) throws YeelightSocketException, YeelightResultErrorException {
        String jsonCommand = command.toJson() + "\r\n";
        this.socketHolder.send(jsonCommand);
        return this.readUntilResult(command.getId());
    }

    /**
     * Retrieve properties of device
     * @param properties Required properties (if no property is specified, all properties are retrieve)
     * @return Required properties map (Property → string value)
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public Map<YeelightProperty, String> getProperties(YeelightProperty... properties) throws YeelightResultErrorException, YeelightSocketException {
        YeelightProperty[] expectedProperties = properties.length == 0 ? YeelightProperty.values() : properties;
        Object[] expectedPropertiesValues = Stream.of(expectedProperties).map(YeelightProperty::getValue).toArray();
        YeelightCommand command = new YeelightCommand("get_prop", expectedPropertiesValues);
        String[] result = this.sendCommand(command);
        Map<YeelightProperty, String> propertyToValueMap = new HashMap<>();
        for (int i = 0; i < expectedProperties.length; i++) {
            propertyToValueMap.put(expectedProperties[i], result[i]);
        }
        return propertyToValueMap;
    }

    /**
     * Save current state of the device as 'default'.
     * If device is power off and then power on (hard power reset), the device will show 'default' saved state
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setDefault() throws YeelightResultErrorException, YeelightSocketException {
        YeelightCommand command = new YeelightCommand("set_default");
        this.sendCommand(command);
    }

    /**
     * Start a power off device timer
     * @param delay Timer delay in minutes (&gt;= 0)
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void addCron(int delay) throws YeelightResultErrorException, YeelightSocketException {
        delay = Math.max(0, delay);
        YeelightCommand command = new YeelightCommand("cron_add", 0, delay);
        this.sendCommand(command);
    }

    /**
     * Retrieve delay in minutes of current power off timer
     * @return Delay in minutes of current power off timer (0 if there is no timer)
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public int getCronDelay() throws YeelightResultErrorException, YeelightSocketException {
        Map<YeelightProperty, String> propertyToString = this.getProperties(YeelightProperty.DELAY_OFF);
        try {
            return Integer.parseInt(propertyToString.get(YeelightProperty.DELAY_OFF));
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Remove/stop current power off timer
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void deleteCron() throws YeelightResultErrorException, YeelightSocketException {
        YeelightCommand command = new YeelightCommand("cron_del", 0);
        this.sendCommand(command);
    }

    /**
     * Adjust some parameters. (Main used by controllers)
     * @param property Property to adjust (if null, Circle chosen)
     * @param action Direction of adjustment (if null, Color chose). For property {@link YeelightAdjustProperty#COLOR}, action can only be {@link YeelightAdjustAction#CIRCLE}
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setAdjust(YeelightAdjustProperty property, YeelightAdjustAction action) throws YeelightResultErrorException, YeelightSocketException {
        String actionValue = action == null ? YeelightAdjustAction.CIRCLE.getValue() : action.getValue();
        String propertyValue = property == null ? YeelightAdjustProperty.COLOR.getValue() : property.getValue();
        YeelightCommand command = new YeelightCommand("set_adjust", actionValue, propertyValue);
        this.sendCommand(command);
    }

    /**
     * Name the device. The name will be stored in the device and can be accessed in device properties
     * @param name Device name value (if null, empty name will sent)
     * @throws YeelightResultErrorException when command result is an error
     * @throws YeelightSocketException when socket error occurs
     */
    public void setName(String name) throws YeelightResultErrorException, YeelightSocketException {
        if (name == null) {
            name = "";
        }
        YeelightCommand command = new YeelightCommand("set_name", name);
        this.sendCommand(command);
    }
}
