package com.example.asyncstoragedojo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    Button btnCalendarPicker;
    Button btnGetData;
    public static String selectedDate;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnCalendarPicker = findViewById(R.id.pickDate);

        btnCalendarPicker.setOnClickListener(v -> {
            new DatePickerFragment().show(getSupportFragmentManager(), "datePicker");

        });
        btnGetData = findViewById(R.id.btnGetData);

        btnGetData.setOnClickListener(v -> {
            fetchHydroData(selectedDate);

        });
    }
    private void fetchHydroData(String date) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background work
            String apiUrl = "https://api.meteo.lt/v1/hydro-stations/nemajunu-vms/observations/measured/" + date;
            StringBuilder response = new StringBuilder();
            String resultMessage;
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    resultMessage = "Data fetched successfully.";
                } else {
                    resultMessage = "Error: " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultMessage = "Exception: " + e.getMessage();
            }

            // UI Thread work
            String finalResultMessage = resultMessage;
            String jsonResponse = response.toString();
            handler.post(() -> {
                Toast.makeText(this, finalResultMessage, Toast.LENGTH_LONG).show();
                if (!jsonResponse.isEmpty()) {
                    try {
                        HydroData hydroData = parseHydroData(jsonResponse);
                        StringBuilder myString = new StringBuilder();
                        myString.append("Parsed Station Name: " + hydroData.station.name + "\n");
                        myString.append("Number of observations: " + hydroData.observations.size()+ "\n");
                        for (Observation obs : hydroData.observations) {
                            myString.append("Observation water level: " + obs.waterLevel+ "\n");
                        }
                        writeFileOnInternalStorage(this, "data.txt", myString.toString());
                        Log.d(TAG, "Parsed Station Name: " + hydroData.station.name);
                        Log.d(TAG, "Number of observations: " + hydroData.observations.size());
                        if (!hydroData.observations.isEmpty()) {
                            Log.d(TAG, "First observation water level: " + hydroData.observations.get(0).waterLevel);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error", e);
                        Toast.makeText(this, "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File dir = new File(mcoContext.getFilesDir(), "data");
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            File gpxfile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private HydroData parseHydroData(String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);

        // Parse Station
        JSONObject stationJson = root.getJSONObject("station");
        JSONObject coordinatesJson = stationJson.getJSONObject("coordinates");
        Coordinates coordinates = new Coordinates(
                coordinatesJson.getDouble("latitude"),
                coordinatesJson.getDouble("longitude")
        );
        Station station = new Station(
                stationJson.getString("code"),
                stationJson.getString("name"),
                stationJson.getString("waterBody"),
                coordinates
        );

        // Parse Observations
        JSONArray observationsJson = root.getJSONArray("observations");
        List<Observation> observations = new ArrayList<>();
        for (int i = 0; i < observationsJson.length(); i++) {
            JSONObject obsJson = observationsJson.getJSONObject(i);
            Observation observation = new Observation(
                    obsJson.getString("observationTimeUtc"),
                    obsJson.optDouble("waterLevel", Double.NaN), // Use optDouble for safety
                    obsJson.optDouble("waterTemperature", Double.NaN)
            );
            observations.add(observation);
        }

        return new HydroData(station, observations);
    }
}
