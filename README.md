[![Build Status](https://travis-ci.org/florian-mollin/yapi.svg?branch=master)](https://travis-ci.org/florian-mollin/yapi)

# THIS IS THE MUSIC MODE YOU'RE LOOKING FOR 
If you're getting quota exceeded when controlling you device, this is the fork of the api that established a direkt connection between yeelight and java


# Yapi : Yeelight Java API
Yapi is an API that lets you control different Yeelight devices via Wi-Fi.

## Get Yapi
To use Yapi, you can download the JAR [here](jar/) (*Java 8 or above is required*).


## Usage / Example
Here is a simple example of how to use the API:
```java
// Instantiate your device (with its IP)
YeelightDevice device = new YeelightDevice("192.168.1.47");
// Switch on the device
device.setPower(true);
// Change device color
device.setRGB(255, 126, 0);
// Change device brightness
device.setBrightness(100);
```
For a complete overview of available commands, see the [API documentation](https://github.com/florian-mollin/yapi/wiki).
