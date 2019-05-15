package org.golde.plugableswitch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class Example {

	public static void main(String[] args) throws IOException, InterruptedException {
		PlugableSwitch ps = new PlugableSwitch("8C:DE:52:21:32:BA");
		ps.setDebuging(true);
		ps.connect();
		if(ps.isConnected()) {
			for(int i = 0; i < 10; i++) {
				ps.setState(i % 2 == 0);
				Thread.sleep(300);
			}
		}
		ps.disconnect();
	}

}
