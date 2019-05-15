package org.golde.plugableswitch;

import java.io.IOException;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class PlugableSwitch {

	private boolean connected = false;
	private StreamConnection streamConnection;
	private OutputStream outStream;
	private final PlugableRemoteDevice remoteDevice;
	private boolean debug = false;

	public PlugableSwitch(String mac) {
		remoteDevice = new PlugableRemoteDevice(mac);
	}

	//btspp://8CDE522132BA:6;authenticate=false;encrypt=false;master=false
	//	public final PlugableRemoteDevice getRemoteDevice() {
	//		return remoteDevice;
	//	}

	public final void setDebuging(boolean enabled) {
		this.debug = enabled;
	}

	public final void connect() {
		if(connected) {
			debug("Already connected to: %mac%!");
		}
		debug("Connecting to device: %mac%...");
		try {
			remoteDevice.connect();
			while(remoteDevice.isStillScanning()) {
				Thread.sleep(500);
			}

			streamConnection = (StreamConnection) Connector.open(remoteDevice.getUrl());
			outStream = streamConnection.openOutputStream();
			connected = true;
		}
		catch(Exception e) {
			debug("Failed to connect to: %mac%!");
			e.printStackTrace();
			connected = false;
		}

		debug("Connected to device: %mac%!");
	}

	public final void disconnect() throws IOException {
		if(!connected) {
			debug("Already disconnected to device %mac%!");
			return;
		}
		try {
			if(outStream != null) {
				outStream.close();
			}
			if(streamConnection != null) {
				streamConnection.close();
			}
			connected = false;
			debug("Disconnected from device: %mac%!");
		}
		catch(IOException e) {
			debug("Failed to disconnect from device: %mac%!");
			e.printStackTrace();
			connected = true;
		}

	}

	public final boolean isConnected() {
		return connected;
	}

	public final void setState(boolean on) throws IOException{
		//ON Payload = 0xCCAA03010101
	   //OFF Payload = 0xCCAA03010100
		if(on) {
			sendMessageToDevice(new byte[] {(byte) 0xCC, (byte) 0xAA, 0x03, 0x01, 0x01, 0x01});
		}
		else {
			sendMessageToDevice(new byte[] {(byte) 0xCC, (byte) 0xAA, 0x03, 0x01, 0x01, 0x00});
		}
	}

	//Utils

	private void sendMessageToDevice(byte[] data) throws IOException {
		if(!isConnected()) {
			return;
		}
		outStream.write(data);
		outStream.flush();
	}

	private void debug(String msg) {
		if(debug) {
			System.out.println(msg.replace("%mac%", remoteDevice.getBluetoothAddress()));
		}
	}

	private class PlugableRemoteDevice extends RemoteDevice {

		private String urlToReturn;
		private boolean scanFinished;

		protected PlugableRemoteDevice(String mac) {
			super(mac.replace(":", ""));
		}

		private void connect() throws BluetoothStateException {
			//search for services:
			UUID[] uuidSet = new UUID[1];
			uuidSet[0] = new UUID("0000110100001000800000805F9B34FB", false); //RFCOMM id

			LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(null, uuidSet, this, new DiscoveryListener() {
				@Override
				public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {

				}

				@Override
				public void inquiryCompleted(int discType) {
				}

				@Override
				public void serviceSearchCompleted(int transID, int respCode) {
					scanFinished = true;
				}

				@Override
				public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
					for (int i = 0; i < servRecord.length; i++) {
						urlToReturn = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
						if (urlToReturn != null) {
							break; //take the first one
						}
					}
				}
			});
		}

		public final boolean isStillScanning() {
			return !scanFinished;
		}

		public final String getUrl() {
			return urlToReturn;
		}

	}

}
