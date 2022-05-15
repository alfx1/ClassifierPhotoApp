package com.android.classifierphotoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.classifierphotoapp.databinding.FragmentFirstBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    private LabelViewModel model;
    List<String> labelList = null;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.INTERNET
    };
    private static final int PERMISSIONS_ALL = 9;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(LabelViewModel.class);
        checkPermissions();

        // get labels
        new Thread(this::getLabelList).start();
//        List<String> list = new ArrayList<>();
//        list.add("bedroom");
//        list.add("livingroom");

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                model.selectedLabel = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.buttonFirst.setOnClickListener(view1 -> {
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
        });
    }

    private void checkPermissions() {
        if(!(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET) ==
                PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(
                    this.getActivity(),
                    PERMISSIONS,
                    PERMISSIONS_ALL
            );
        }
    }

    private void getLabelList() {
        try {
            URL url = new URL("http://192.168.1.21:4200");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                buf.append(line).append("\n");
            }

            // cleanup
            in.close();
            conn.disconnect();

            JSONObject json = new JSONObject(buf.toString());
            labelList = new ArrayList<>();
            JSONArray arr = json.getJSONArray("labels");
            for(int i = 0; i < arr.length(); i++)
                labelList.add(arr.getString(i));

            String msg = "got labels successfully";
            Log.w("network: ", msg);
            Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();

        } catch (IOException | JSONException e) {
            String msg = "failed to reach host";
            Log.e("", msg);
            Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();

            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}