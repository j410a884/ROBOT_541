import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import eecs.design.robot.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class BluetoothActivity extends Activity {
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	private BluetoothAdapter mBluetoothAdapter;
	ArrayAdapter<String> mArrayAdapter;
	public static final int REQUEST_ENABLE_BT = 2;
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	        }
	    }
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_act_layout);
		mArrayAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1 );
		// Create the bluetooth adapter.
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Check if bluetooth is enabled. If not, then enable it.
		if ( !mBluetoothAdapter.isEnabled() ) {
		    Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
		    startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT );
		}
				
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == REQUEST_ENABLE_BT)
		{
			if(resultCode == RESULT_OK)
			{
				mBluetoothAdapter.startDiscovery();
			}
		}
	}
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;

	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }

	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();

	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }

	        // Do work to manage the connection (in a separate thread)
	        //manageConnectedSocket(mmSocket);
	    }

	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;

	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;

	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }

	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }

	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()

	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }

	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }

	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
}
