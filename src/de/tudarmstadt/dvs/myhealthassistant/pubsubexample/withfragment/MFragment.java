/* 
 * Copyright (C) 2014 TU Darmstadt, Hessen, Germany.
 * Department of Computer Science Databases and Distributed Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tudarmstadt.dvs.myhealthassistant.pubsubexample.withfragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.IMyHealthHubRemoteService;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.AbstractChannel;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.Event;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Advertisement;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Announcement;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.StartProducer;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.StopProducer;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Subscription;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Unadvertisement;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Unsubscription;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.notifications.NotificationEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.SensorReadingEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.cardiovascular.BloodPressureEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.cardiovascular.HRFidelityEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.cardiovascular.HRVariabilityEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.cardiovascular.HeartRateEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.AccDeviceSensorEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.AccSensorEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.AccSensorEventAnkle;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.AccSensorEventInG;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.AccSensorEventKnee;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.BodyTemperatureEventInCelsius;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.BodyTemperatureEventInFahrenheit;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.WeightEventInKg;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.WeightEventInLbs;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.activity.ActivityEventReha;
import de.tudarmstadt.dvs.myhealthassistant.pubsubexample.R;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is an examples applications that shows the interaction with
 * myHealhAssistant's myHealthHub. For event consumers, it explains: - how to
 * connect to the myHealthHub remote service - how to (un-)subscribe to event
 * types - how to receive events - how to receive information about event
 * producer's connectivity
 * 
 * For event producers, it explains: - how to advertise event types - how to
 * receive start/stop events telling whether the produced events are needed -
 * how to inform the middleware/applications about sensor connectivity
 * 
 * @author HieuHa
 * 
 */
public class MFragment extends ListFragment {

	// for debugging
	private static String TAG = MFragment.class.getSimpleName();

	// shared preferences including sensor auto-connect information
	// private SharedPreferences preferences;

	private MListAdapter mAdapter;
	// private MAbstractSensorListAdapter adapter;

	// for enabling Bluetooth
	// private BluetoothAdapter mBluetoothAdapter;

	// used for an increasing event ID
	private int eventCounter;

	// for connecting to the remote service of myHealthHub
	private Intent myHealthHubIntent;
	private boolean connectedToHMM;

	// for receiving reading events:
	private ReadingEventReceiver myReadingReceiver = new ReadingEventReceiver();

	// for receiving management events:
	private ManagementEventReceiver myManagementReceiver = new ManagementEventReceiver();

	private View rootView;

	private ArrayList<MEvent> mList;

	// public enum EVT{
	// BP_EVT, HR_EVT, ACC_EVT, CEL_EVT, FAH_EVT, HRF_EVT, KG_EVT, LBS_EVT,
	// HRA_EVT;
	// }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		mList = new ArrayList<MEvent>();
		mAdapter = new MListAdapter(getActivity().getApplicationContext(), 0);
		setListAdapter(mAdapter);
		// setListShown(false);

		mList.add(new MEvent("Blood Pressure",
				SensorReadingEvent.BLOOD_PRESSURE));
		mList.add(new MEvent("Heart Rate", SensorReadingEvent.HEART_RATE));
		mList.add(new MEvent("Heart Rate Variability",
				SensorReadingEvent.HR_VARIABILITY));
		// mList.add(new MEvent("Accelerometer",
		// SensorReadingEvent.ACCELEROMETER));
		mList.add(new MEvent("Accelerometer in m/s2",
				SensorReadingEvent.ACCELEROMETER_ON_DEVICE));
		mList.add(new MEvent("Accelerometer in G",
				SensorReadingEvent.ACCELEROMETER_IN_G));
		mList.add(new MEvent("Activity Recognition",
				SensorReadingEvent.ACTIVITY_REHA));
		// mList.add(new MEvent("Scale", "?"));
		// mList.add(new MEvent("ECG To Heart Rate",
		// SensorReadingEvent.ECG_STREAM));
		mList.add(new MEvent("BodyTemp in Celsius",
				SensorReadingEvent.BODY_TEMPERATURE_IN_CELSIUS));
		mList.add(new MEvent("BodyTemp in Fahrenheit",
				SensorReadingEvent.BODY_TEMPERATURE_IN_FAHRENHEIT));
		mList.add(new MEvent("HR + Fidelity", SensorReadingEvent.HR_FIDELITY));
		mList.add(new MEvent("Weight in Kg", SensorReadingEvent.WEIGHT_IN_KG));
		mList.add(new MEvent("Weight in Lbs", SensorReadingEvent.WEIGHT_IN_LBS));
		mList.add(new MEvent("Hr With Alarm", NotificationEvent.EVENT_TYPE));

		mAdapter.setData(mList);

		/* Preferences */
		// preferences = PreferenceManager.getDefaultSharedPreferences(this
		// .getActivity());

		registerForContextMenu(getListView());
		setHasOptionsMenu(true);

		/** Connection to myHealthHub Service */
		myHealthHubIntent = new Intent(
				IMyHealthHubRemoteService.class.getName());
		getActivity().bindService(myHealthHubIntent,
				myHealthAssistantRemoteConnection, Context.BIND_AUTO_CREATE);

		// initialize event counter
		eventCounter = 0;
		// register management receiver for receiving start/stop events and
		// sensor connectivity information
		getActivity().registerReceiver(myManagementReceiver,
				new IntentFilter(AbstractChannel.MANAGEMENT));

		/*
		 * register reading receiver for the desired event types. You can also
		 * register individual receivers for specific event types by having
		 * multiple "myReadingReceivers".
		 */
		IntentFilter inFil = new IntentFilter();
		inFil.addAction(SensorReadingEvent.BLOOD_PRESSURE);
		inFil.addAction(SensorReadingEvent.HEART_RATE);
		inFil.addAction(SensorReadingEvent.WEIGHT_IN_KG);
		inFil.addAction(SensorReadingEvent.WEIGHT_IN_LBS);
		inFil.addAction(SensorReadingEvent.BODY_TEMPERATURE_IN_CELSIUS);
		inFil.addAction(SensorReadingEvent.BODY_TEMPERATURE_IN_FAHRENHEIT);
		inFil.addAction(SensorReadingEvent.HR_FIDELITY);
		inFil.addAction(SensorReadingEvent.HR_VARIABILITY);
		inFil.addAction(SensorReadingEvent.ACCELEROMETER);
		inFil.addAction(SensorReadingEvent.ACCELEROMETER_ON_DEVICE);
		inFil.addAction(SensorReadingEvent.ACCELEROMETER_IN_G);
		inFil.addAction(NotificationEvent.EVENT_TYPE);
		// inFil.addAction(ActivityEventReha.EVENT_TYPE);
		inFil.addAction(SensorReadingEvent.ACTIVITY_REHA);

		getActivity().registerReceiver(myReadingReceiver, inFil);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(
				R.layout.fragment_list_with_empty_container_gen_evt, container,
				false);

		return rootView;
	}

	/**
	 * Service connection to myHealthHub remote service. This connection is
	 * needed in order to start myHealthHub. Furthermore, it is used inform the
	 * application about the connection status.
	 */
	private ServiceConnection myHealthAssistantRemoteConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(getActivity().getApplicationContext(),
					"Connected to myHealthAssistant", Toast.LENGTH_LONG).show();
			connectedToHMM = true;
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "I am disconnected.");
			connectedToHMM = false;
		}
	};

	@Override
	public void onDestroy() {
		Log.d(TAG, "onStop");

		for (MEvent e : mList) {
			if (e.isOnGen())
				e.stopGenEvent();

			e.onUnsubEvent();
			e.onUnadvEvent();
		}
		
		if (connectedToHMM) {
			getActivity().unbindService(myHealthAssistantRemoteConnection);
			connectedToHMM = false;
		}

		getActivity().stopService(myHealthHubIntent);
		getActivity().unregisterReceiver(myReadingReceiver);
		getActivity().unregisterReceiver(myManagementReceiver);

		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		Log.d(TAG, "onPauseView");
		super.onPause();
	}

	public class MEvent {
		public static final String ID_TAG = "mEvent";
		private String id;
		private String sensorEvent;
		private boolean onAdv, onSub, onGen;

		private int m_interval = 1000; // 1 seconds by default, can be changed
										// later
		private Handler m_handler;
		private Runnable m_Generator;

		public MEvent(String id, String sensorEvent) {
			this.id = id;
			this.sensorEvent = sensorEvent;
			m_handler = new Handler();
		}

		public void onAdvEvent() {
			// Generate advertisement
			Advertisement adverisement = new Advertisement("PubSubExampleEvent"
					+ eventCounter++, getTimestamp(), "PubSubExample"
					+ sensorEvent, getActivity().getApplicationContext()
					.getPackageName(), sensorEvent, "-");

			// publish advertisement
			publishManagemntEvent(adverisement);
			onAdv = true;
			mAdapter.notifyDataSetChanged();

		}

		public void onUnadvEvent() {
			// generate unadvertisement
			Unadvertisement unadverisement = new Unadvertisement(
					"PubSubExampleEvent" + eventCounter++, getTimestamp(),
					"PubSubExample" + sensorEvent, getActivity()
							.getApplicationContext().getPackageName(),
					sensorEvent);

			// publish unadvertisement
			publishManagemntEvent(unadverisement);
			onAdv = false;
			mAdapter.notifyDataSetChanged();
		}

		public void onSubEvent() {
			// generate subscription
			Subscription sub = new Subscription("PubSubExampleEvent"
					+ eventCounter++, getTimestamp(), "PubSubExample",
					getActivity().getPackageName(), sensorEvent);

			// publish subscription
			publishManagemntEvent(sub);
			onSub = true;
			mAdapter.notifyDataSetChanged();
		}

		public void onUnsubEvent() {
			// generate subscription
			Unsubscription unsub = new Unsubscription("PubSubExampleEvent"
					+ eventCounter++, getTimestamp(), "PubSubExample",
					getActivity().getPackageName(), sensorEvent);

			// publish un-subscription
			publishManagemntEvent(unsub);
			onSub = false;
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public String toString() {
			return id;
		}

		public void onGenEvent() {
			m_Generator = new Runnable() {
				@Override
				public void run() {
					// Log.e(TAG, "generator running!");
					if (sensorEvent.equals(SensorReadingEvent.BLOOD_PRESSURE)) {
						publishReadingEvent(new BloodPressureEvent(
								"PubSubExampleEvent" + eventCounter++,
								getTimestamp(), "PubSubExampleBPSensor",
								"PubSubExampleBPSensor", getTimestamp(),
								getRandomNumber(70, 150), getRandomNumber(50,
										110), getRandomNumber(55, 180), "mmHG"));
					} else if (sensorEvent
							.equals(SensorReadingEvent.HEART_RATE)) {
						publishReadingEvent(new HeartRateEvent(
								"PubSubExampleEvent" + eventCounter++,
								getTimestamp(), "PubSubExampleBPSensor",
								"PubSubExampleBPSensor", getTimestamp(),
								getRandomNumber(79, 90)));
					} else if (sensorEvent
							.equals(SensorReadingEvent.WEIGHT_IN_KG)) {
						publishReadingEvent(new WeightEventInKg(TAG
								+ eventCounter++, getTimestamp(), TAG,
								"PubSubExampleASensor", getTimestamp(),
								getRandomNumber(50, 190)));
					} else if (sensorEvent
							.equals(SensorReadingEvent.WEIGHT_IN_LBS)) {
						publishReadingEvent(new WeightEventInLbs(TAG
								+ eventCounter++, getTimestamp(), TAG,
								"PubSubExampleASensor", getTimestamp(),
								getRandomNumber(50, 190)));
					} else if (sensorEvent
							.equals(SensorReadingEvent.BODY_TEMPERATURE_IN_CELSIUS)) {
						publishReadingEvent(new BodyTemperatureEventInCelsius(
								TAG + eventCounter++, getTimestamp(), TAG,
								"PubSubExampleASensor", getTimestamp(),
								getRandomNumber(29, 35)));
					} else if (sensorEvent
							.equals(SensorReadingEvent.BODY_TEMPERATURE_IN_FAHRENHEIT)) {
						publishReadingEvent(new BodyTemperatureEventInFahrenheit(
								TAG + eventCounter++, getTimestamp(), TAG,
								"PubSubExampleASensor", getTimestamp(),
								getRandomNumber(100, 175)));
					} else if (sensorEvent
							.equals(SensorReadingEvent.ACCELEROMETER_ANKLE)) {
						// { 156.7037f, 123.5630f, 118.4889f, 61.4074f,
						// 82.3407f, 38.0741f }, // jogging
						// { 152.0734f, 125.1835f, 119.2844f, 11.7431f,
						// 26.0550f, 11.0275f }, // walking
						// { 130.0000f, 99.0000f, 124.0000f, 1.2222f, 0.9444f,
						// 0.9444f }, // sitting
						// { 152.0000f, 128.5263f, 121.2105f, 0.0526f, 0.9474f,
						// 1.4737f }, // standing
						ArrayList<int[]> dataCloud = new ArrayList<int[]>();
						dataCloud.add(new int[] { 156, 123, 118, 61, 82, 38 }); // jogging
						dataCloud.add(new int[] { 152, 125, 119, 11, 26, 11 }); // walking
						dataCloud.add(new int[] { 130, 99, 124, 1, 0, 0 }); // sitting
						dataCloud.add(new int[] { 152, 128, 121, 0, 0, 1 }); // standing

						int index = getRandomNumber(0, 4);
						int[] entry = dataCloud.get(index);
						publishReadingEvent(new AccSensorEventAnkle(TAG
								+ eventCounter++, getTimestamp(), TAG,
								"PubSubExampleASensor", getTimestamp(),
								entry[0], entry[1], entry[2], entry[3],
								entry[4], entry[5], true, true, false));
					} else
						return;

					m_handler.postDelayed(m_Generator, m_interval);
				}
			};
			m_Generator.run();
			onGen = true;
		}

		public void stopGenEvent() {
			if (m_handler != null && m_Generator != null) {
				m_handler.removeCallbacks(m_Generator);
			}
			onGen = false;
		}

		public boolean isOnAdv() {
			return onAdv;
		}

		public boolean isOnGen() {
			return onGen;
		}

		public boolean isOnSub() {
			return onSub;
		}

	}

	/**
	 * Publishes a reading event
	 * 
	 * @param evt
	 *            that shall be published.
	 */
	private void publishReadingEvent(Event evt) {
		String eventType = evt.getEventType();
		String text = evt.getTimestamp() + "--";
		text += evt.getShortEventType() + "--";
		if (eventType.equals(SensorReadingEvent.WEIGHT_IN_KG)) {
			int i = ((WeightEventInKg) evt).getWeight();
			text += i;
		}
		if (eventType.equals(SensorReadingEvent.WEIGHT_IN_LBS)) {
			int i = ((WeightEventInLbs) evt).getWeight();
			text += i;
		} else if (eventType.equals(SensorReadingEvent.BLOOD_PRESSURE)) {
			String i = ((BloodPressureEvent) evt).getSystolic() + "/"
					+ ((BloodPressureEvent) evt).getDiastolic();
			text += i;
		} else if (eventType.equals(SensorReadingEvent.HEART_RATE)) {
			int i = ((HeartRateEvent) evt).getValue();
			text += i;
		} else if (eventType
				.equals(SensorReadingEvent.BODY_TEMPERATURE_IN_CELSIUS)) {
			int i = ((BodyTemperatureEventInCelsius) evt).getTemperature();
			text += i;
		} else if (eventType
				.equals(SensorReadingEvent.BODY_TEMPERATURE_IN_FAHRENHEIT)) {
			int i = ((BodyTemperatureEventInFahrenheit) evt).getTemperature();
			text += i;
		} else if (evt.getEventType().equals(
				SensorReadingEvent.ACCELEROMETER_ANKLE)) {
			AccSensorEvent acc = (AccSensorEvent) evt;
			text += (acc.x_mean + ", " + acc.y_mean + ", " + acc.z_mean);
		} else if (evt.getEventType().equals(
				SensorReadingEvent.ACCELEROMETER_KNEE)) {
			AccSensorEvent acc = (AccSensorEvent) evt;
			text += (acc.x_mean + ", " + acc.y_mean + ", " + acc.z_mean);
		}
		setTextInGen(text);
		publishEvent(evt, AbstractChannel.RECEIVER);
	}

	/**
	 * Publishes a management event.
	 * 
	 * @param managementEvent
	 */
	private void publishManagemntEvent(Event managementEvent) {
		publishEvent(managementEvent, AbstractChannel.MANAGEMENT);
	}

	/**
	 * Publishes an event on a specific myHealthHub channel.
	 * 
	 * @param event
	 *            that shall be published.
	 * @param channel
	 *            on which the event shall be published.
	 */
	private void publishEvent(Event event, String channel) {
		Intent i = new Intent();
		// add event
		i.putExtra(Event.PARCELABLE_EXTRA_EVENT_TYPE, event.getEventType());
		i.putExtra(Event.PARCELABLE_EXTRA_EVENT, event);

		// set channel
		i.setAction(channel);

		// set receiver package
		// i.setPackage("de.tudarmstadt.dvs.myhealthassistant.pubsubexample");
		i.setPackage("de.tudarmstadt.dvs.myhealthassistant.myhealthhub");

		// sent intent
		if (getActivity() != null)
			getActivity().sendBroadcast(i);
		// mAdapter.notifyDataSetChanged();
	}

	/*
	 * === Event receivers
	 * ===========================================================
	 */
	/**
	 * Management receiver implemented as a Android BroadcastReceiver for
	 * receiving management events.
	 * 
	 * For event consumers, it is important in order to receive information
	 * about the sensor connectivity.
	 * 
	 * For event producers, the management channel is used in order to inform
	 * the event producers about start/stop events for specific event types.
	 * This helps to save energy if a event type is not needed (e.g., stop the
	 * sensor).
	 */
	private class ManagementEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			// Get the event from the parcelable intent
			Event evt = intent.getParcelableExtra(Event.PARCELABLE_EXTRA_EVENT);

			// for debugging:
			Log.e(TAG, "ManagementEventReceiver: " + evt.getEventType());
			String text = evt.getShortEventType();
			boolean colorIsGreen = false;
			// consume the individual event types
			// if event is of type announcement in order to inform about sensor
			// connectivity:
			if (evt.getEventType().equals(Announcement.EVENT_TYPE)) {
				int announcement = ((Announcement) evt).getAnnouncement();

				/* Applies for CONSUMER side ================ */

				// sensor is connected:
				if (announcement == Announcement.SENSOR_CONNECTED) {
					text += " SENSOR_CONNECTED";
					colorIsGreen = true;

					// sensor is disconnected:
				} else if (announcement == Announcement.SENSOR_DISCONNECTED) {
					text += " SENSOR_DISCONNECTED";
					colorIsGreen = false;
				}

				/* Applies for PRODUCER side ================ */

				// incoming start producer event telling that a specific event
				// type is desired.
			} else if (evt.getEventType().equals(
					SensorReadingEvent.ACCELEROMETER_KNEE)) {
				AccSensorEvent acc = (AccSensorEvent) evt;
				text += (acc.x_mean + ", " + acc.y_mean + ", " + acc.z_mean
						+ " (" + acc.getTimestamp() + ")\n");

			} else if (evt.getEventType().equals(
					SensorReadingEvent.ACCELEROMETER_ANKLE)) {
				AccSensorEvent acc = (AccSensorEvent) evt;
				text += (acc.x_mean + ", " + acc.y_mean + ", " + acc.z_mean
						+ " (" + acc.getTimestamp() + ")\n");
			} else if (evt.getEventType().equals(StartProducer.EVENT_TYPE)) {
				text += " start";
				colorIsGreen = true;

				// incoming stop producer event telling the the specific event
				// type is not needed anymore.
			} else if (evt.getEventType().equals(StopProducer.EVENT_TYPE)) {
				text += " stop";
				colorIsGreen = false;
			}
			setTextInAdv(text, (colorIsGreen) ? Color.GREEN : Color.RED);
		}
	};

	/*
	 * ==========================================================================
	 * ===== The following code is interesting for EVENT CONSUMERS only.
	 * ========
	 * =======================================================================
	 */
	/**
	 * Event receiver implemented as a Android BroadcastReceiver for receiving
	 * myHealthAssistant sensor reading events.
	 */
	private class ReadingEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			/* Get event type and the event itself */
			Event evt = intent.getParcelableExtra(Event.PARCELABLE_EXTRA_EVENT);
			String eventType = evt.getEventType();

			// for debugging:
			Log.e(TAG, "ReadingEventReceiver of type: " + eventType);
			// Log.e(TAG,
			// "ReadingEventReceiver with producerID: "
			// + evt.getShortProducerID());

			String text = evt.getTimestamp() + "--";
			text += evt.getShortEventType() + ":  ";

			if (eventType.equals(SensorReadingEvent.ACCELEROMETER_ON_DEVICE)) {
				AccDeviceSensorEvent acc = (AccDeviceSensorEvent) evt;
				String xValue = new DecimalFormat("#0.00").format(acc.x_mean);
				String yValue = new DecimalFormat("#0.00").format(acc.y_mean);
				String zValue = new DecimalFormat("#0.00").format(acc.z_mean);
				text += (xValue + "; " + yValue + "; " + zValue);
				setTextInGen(text);

			} else {
				if (eventType.equals(SensorReadingEvent.ACCELEROMETER_IN_G)) {
					AccSensorEventInG acc = (AccSensorEventInG) evt;
					String xValue = new DecimalFormat("#0.00").format(acc.x_mean);
					String yValue = new DecimalFormat("#0.00").format(acc.y_mean);
					String zValue = new DecimalFormat("#0.00").format(acc.z_mean);
					text += (xValue + "; " + yValue + "; " + zValue);
				} else

				// event of type blood pressure
				if (eventType.equals(BloodPressureEvent.EVENT_TYPE)) {
					BloodPressureEvent bp = (BloodPressureEvent) evt;
					text += (bp.getSystolic() + "/" + bp.getDiastolic());

					// event of type HeartRate
				} else if (eventType.equals(HeartRateEvent.EVENT_TYPE)) {
					HeartRateEvent hr = (HeartRateEvent) evt;
					text += (hr.getValue());

				} else if (eventType
						.equals(SensorReadingEvent.ACCELEROMETER_KNEE)) {
					AccSensorEventKnee acc = (AccSensorEventKnee) evt;
					text += (acc.x_mean + ", " + acc.y_mean + ", " + acc.z_mean);
				} else if (eventType
						.equals(SensorReadingEvent.ACCELEROMETER_ANKLE)) {
					AccSensorEventAnkle acc = (AccSensorEventAnkle) evt;
					text += (acc.x_mean + ", " + acc.y_mean + ", " + acc.z_mean);
				} else if (eventType.equals(SensorReadingEvent.WEIGHT_IN_KG)) {
					int i = ((WeightEventInKg) evt).getWeight();
					text += i;
				} else if (eventType.equals(SensorReadingEvent.WEIGHT_IN_LBS)) {
					int i = ((WeightEventInLbs) evt).getWeight();
					text += i;
				} else if (eventType.equals(SensorReadingEvent.BLOOD_PRESSURE)) {
					String i = ((BloodPressureEvent) evt).getSystolic() + "/"
							+ ((BloodPressureEvent) evt).getDiastolic();
					text += i;
				} else if (eventType.equals(SensorReadingEvent.HEART_RATE)) {
					int i = ((HeartRateEvent) evt).getValue();
					text += i;
				} else if (eventType
						.equals(SensorReadingEvent.BODY_TEMPERATURE_IN_CELSIUS)) {
					int i = ((BodyTemperatureEventInCelsius) evt)
							.getTemperature();
					text += i;
				} else if (eventType
						.equals(SensorReadingEvent.BODY_TEMPERATURE_IN_FAHRENHEIT)) {
					int i = ((BodyTemperatureEventInFahrenheit) evt)
							.getTemperature();
					text += i;
				} else if (eventType.equals(SensorReadingEvent.HR_FIDELITY)) {
					int i = ((HRFidelityEvent) evt).getFidelity();
					int j = ((HRFidelityEvent) evt).getValue();
					text += i + "///" + j;
				} else if (eventType.equals(SensorReadingEvent.HR_VARIABILITY)) {
					long i = ((HRVariabilityEvent) evt).getHrV();
					int j = ((HRVariabilityEvent) evt).getValue();
					text += "HRV:" + i + "; HR:" + j;
				} else if (eventType.equals(NotificationEvent.EVENT_TYPE)) {
					if (((NotificationEvent) evt).severity == NotificationEvent.SEVERITY_CRITICAL) {
						text += "Critical Hr!";
					} else
						text += "normal!";
				} else if (eventType.equals(SensorReadingEvent.ACTIVITY_REHA)) {
					text = ((ActivityEventReha) evt).getTimeOfMeasurement()
							+ "--"
							+ ((ActivityEventReha) evt).getActivityName();
				} else
					Log.e(TAG, "reading unknown event:" + eventType);

				setTextInSub(text);
			}
		}

	};

	/**
	 * Sets text in Android TextView
	 * 
	 * @param id
	 *            of TextView element.
	 * @param text
	 *            that should by displayed.
	 */
	private void setTextInSub(String text) {
		if (rootView != null) {
			((TextView) rootView.findViewById(R.id.subStatus)).setText(text);
			((TextView) rootView.findViewById(R.id.subStatus))
					.setTextColor(Color.RED);
		}
	}

	private void setTextInAdv(String text, int color) {
		if (rootView != null)
			((TextView) rootView.findViewById(R.id.advStatus)).setText(text);
		((TextView) rootView.findViewById(R.id.advStatus)).setTextColor(color);
	}

	private void setTextInGen(String text) {
		if (rootView != null)
			((TextView) rootView.findViewById(R.id.genStatus)).setText(text);
	}

	/**
	 * Returns the current time as "yyyy-MM-dd hh:mm:ss".
	 * 
	 * hh:mm:ss will give you 01:00:00 for 1 PM, use kk:mm:ss to get 13:00:00
	 * 
	 * @return timestamp
	 */
	private String getTimestamp() {
		// return (String) android.text.format.DateFormat.format(
		// "yyyy-MM-dd hh:mm:ss", new java.util.Date());
		return (String) android.text.format.DateFormat.format(
				"yyyy-MM-dd kk:mm:ss", new java.util.Date());
	}

	/**
	 * Return a random number with a specific range.
	 * 
	 * @param minimum
	 *            number count.
	 * @param maximum
	 *            number count.
	 * @return random number
	 */
	private int getRandomNumber(int minimum, int maximum) {
		Random randomGenerator = new Random();
		return randomGenerator.nextInt(maximum - minimum) + minimum;
	}
}