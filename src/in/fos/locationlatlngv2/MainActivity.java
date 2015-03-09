package in.fos.locationlatlngv2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {

	GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		googleMap = fm.getMap();

		new AsyncTaskRunner().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class AsyncTaskRunner extends AsyncTask<String, String, String> {

		String result;

		@Override
		protected String doInBackground(String... params) {
			publishProgress("Loading..."); // Calls onProgressUpdate()
			InputStream is = null;
			List<NameValuePair> name = new ArrayList<NameValuePair>(1);
			name.add(new BasicNameValuePair("lat", "1"));
			name.add(new BasicNameValuePair("long", "2"));
			name.add(new BasicNameValuePair("user", "3"));

			try {
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 4000;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				int timeoutSocket = 6000;
				HttpConnectionParams
						.setSoTimeout(httpParameters, timeoutSocket);

				HttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpPost httppost = new HttpPost(
						"http://feetonstreet.hostoi.com/data.php");
				httppost.setEntity(new UrlEncodedFormEntity(name));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection" + e.toString()
						+ e.getStackTrace().toString());
			}

			// convert response to string
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");
				String line = "0";

				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				is.close();
				result = sb.toString();

			} catch (Exception e) {
				Log.e("log_tag", "Error converting result " + e.toString());
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			// paring data
			String lati;
			String longi;
			String user;
			try {
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				System.out.println(jArray.toString());

				for (int i = 0; i < jArray.length(); i++) {
					json_data = jArray.getJSONObject(i);
					lati = json_data.getString("lat");
					longi = json_data.getString("long");
					user = json_data.getString("Id");
					LatLng position = new LatLng(Double.parseDouble(lati),
							Double.parseDouble(longi));
					googleMap.addMarker(new MarkerOptions().position(position));
					googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							position, 17f));
				}

			} catch (JSONException e1) {
				Toast.makeText(getBaseContext(), "No Data Found",
						Toast.LENGTH_LONG).show();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(String... text) {

		}
	}

}
