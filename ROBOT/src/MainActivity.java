import java.util.Set;

import eecs.design.robot.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
public class MainActivity extends Activity {

	public static final int GET_BT_CONFIG = 1;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		// IF (BLUETOOTH CONFIG NOT ALREADY SAVED)
		Intent goToBluetoothConfig = new Intent( getApplicationContext(), BluetoothActivity.class );
		startActivityForResult( goToBluetoothConfig, GET_BT_CONFIG );
	 }
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if( requestCode == GET_BT_CONFIG )
		{
			if(resultCode == RESULT_OK)
			{
				// Save BT config
			}
		}
	}
}
