package org.briarproject.bramble.plugin.bluetooth;

import android.bluetooth.BluetoothSocket;

import org.briarproject.bramble.api.nullsafety.NotNullByDefault;
import org.briarproject.bramble.api.plugin.Plugin;
import org.briarproject.bramble.api.plugin.duplex.AbstractDuplexTransportConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@NotNullByDefault
class AndroidBluetoothTransportConnection
		extends AbstractDuplexTransportConnection {

	private final BluetoothSocket socket;

	AndroidBluetoothTransportConnection(Plugin plugin, BluetoothSocket socket) {
		super(plugin);
		this.socket = socket;
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	protected void closeConnection(boolean exception) throws IOException {
		socket.close();
	}
}
