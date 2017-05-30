package com.swipetry.huiyu.swipeviewtry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;

import java.io.IOException;
import java.util.List;

/**
 * Created by huiyu on 3/16/17.
 */

public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap mMap;
    private Button btn_refresh;
    public String key_id;
    private String property;
    private String street;
    private String city;
    private String state;
    private String zipcode;
    private double property_price;
    private double down_payment;
    private double apr;
    private int terms;
    private double payment;
    private String details;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.detach(this).attach(this).commit();
        View rootView = inflater.inflate(R.layout.map_view, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_refresh = (Button)rootView.findViewById(R.id.refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawAllMarkers();
            }
        });
        /*
        ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.mapView)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });
        */

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                // For showing a move to my location button
                //mMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                //LatLng sydney = new LatLng(-34, 151);
                //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        key_id = marker.getTitle();
                        details = marker.getSnippet();
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Alert");
                        alert.setMessage(details + "\n"+ "\nDo you want to delete this data?");
                        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                                key_id = null;
                            }
                        });
                        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteData(key_id);
                            }
                        });
                        alert.setNeutralButton("EDIT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View layout = inflater.inflate(R.layout.dialog_view, null);
                                EditText edit_price = (EditText) layout.findViewById(R.id.new_property_price);
                                EditText edit_downpayment = (EditText) layout.findViewById(R.id.new_down_payment);
                                EditText edit_apr = (EditText) layout.findViewById(R.id.new_apr);
                                EditText edit_terms = (EditText) layout.findViewById(R.id.new_terms);
                                property_price = ParseDouble(edit_price.getText().toString());
                                down_payment = ParseDouble(edit_downpayment.getText().toString());
                                apr = ParseDouble(edit_apr.getText().toString());
                                terms = (int)ParseDouble(edit_terms.getText().toString());
                                payment = calculatePayment(property_price, down_payment, terms, apr);

                                String[] add = details.split("\n");
                                property = add[0];
                                street = add[1];
                                city = add[2];
                                state = add[3];
                                zipcode = add[4];


                                AlertDialog.Builder temp = new AlertDialog.Builder(getActivity());
                                temp.setView(layout);
                                temp.setTitle("EDIT");
                                temp.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //do something with edt.getText().toString();
                                        deleteData(key_id);
                                        saveInfo();
                                        drawAllMarkers();

                                    }
                                });
                                temp.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //pass
                                    }
                                });
                                temp.show();


                            }
                            private double ParseDouble(String strNumber) {
                                if (strNumber != null && strNumber.length() > 0) {
                                    try {
                                        return Double.parseDouble(strNumber);
                                    } catch(Exception e) {
                                        return -1;   // or some value to mark this field is wrong. or make a function validates field first ...
                                    }
                                }
                                else return 0;
                            }
                            private double calculatePayment(double property_price, double down_payment, double terms, double apr)
                            {
                                double loan_amount = property_price - down_payment;
                                double number_of_payments = terms * 12;
                                double monthly_apr = apr/12/100;

                                double payment = loan_amount * monthly_apr * Math.pow(1 + monthly_apr, number_of_payments) /
                                        (Math.pow(1 + monthly_apr, number_of_payments) - 1);

                                payment = round(payment, 2);
                                //System.out.println(payment);
                                return payment;
                            }
                            public double round(double value, int places) {
                                if (places < 0) throw new IllegalArgumentException();

                                long factor = (long) Math.pow(10, places);
                                value = value * factor;
                                long tmp = Math.round(value);
                                return (double) tmp / factor;
                            }
                        });
                        AlertDialog theAlert = alert.show();

                        //showDialog();
                        //Toast.makeText(getActivity(),key_id,Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //drawAllMarkers();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //drawAllMarkers();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        //drawAllMarkers();
        mMapView.onStart();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void drawAllMarkers() {
        mMap.clear();
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity(), "test_db", 2);
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqliteDatabase.query("user", null, null, null, null, null, null);
        //Toast.makeText(getActivity(), "cursor set", Toast.LENGTH_SHORT).show();
        while(cursor.moveToNext()) {
            String street = cursor.getString(cursor.getColumnIndex("street"));
            String city = cursor.getString(cursor.getColumnIndex("city"));
            String state = cursor.getString(cursor.getColumnIndex("state"));
            String zipcode = cursor.getString(cursor.getColumnIndex("zipcode"));
            String key_id = cursor.getString(cursor.getColumnIndex("key_id"));
            String address = street + ", " + city + ", " + state + ", " + zipcode;
            System.out.println("-------------update-------------");
            System.out.println(address);
            String property_type = cursor.getString(cursor.getColumnIndex("property_type"));
            String note = property_type + "\n" + street + "\n" + city + "\n" + state + "\n" + zipcode;
            double loan_amount = cursor.getDouble(cursor.getColumnIndex("property_price")) - cursor.getDouble(cursor.getColumnIndex("down_payment"));
            double apr = cursor.getDouble(cursor.getColumnIndex("apr"));
            double monthly = cursor.getDouble(cursor.getColumnIndex("payment"));
            String shownDetail = note + loan_amount + '\n' + apr + "\n" + monthly;
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addresses = null;
            Address address1 = null;
            try {
                addresses = geocoder.getFromLocationName(address, 1);
            } catch (IOException e) {
                System.out.println("Exception thrown");
                System.out.println(e.toString());
            }
            if (addresses == null || addresses.isEmpty()) {
                //Toast.makeText(getActivity(), "addressNotFound", Toast.LENGTH_SHORT).show();
            } else {
                address1 = addresses.get(0);
                double geoLatitude = address1.getLatitude();
                double geoLongtitude = address1.getLongitude();
                //Toast.makeText(getActivity(), geoLatitude + " " + geoLongtitude, Toast.LENGTH_SHORT).show();
                LatLng mypoint = new LatLng(geoLatitude, geoLongtitude);
                mMap.addMarker(new MarkerOptions().position(mypoint).title(key_id).snippet(shownDetail));
                //Toast.makeText(getActivity(), shownDetail, Toast.LENGTH_SHORT).show();
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(mypoint));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(mypoint).zoom(12).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private void deleteData(String key_id) {
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity(), "test_db", 2);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        if (key_id == null) {
            return;
        }
        sqLiteDatabase.delete("user", "key_id=?", new String[]{key_id});
        System.out.println("------------delete--12----------");
        drawAllMarkers();
    }

    public void saveInfo()
    {
        ContentValues values = new ContentValues();
        values.put("property_type", property);
        values.put("street", street);
        values.put("city", city);
        values.put("state", state);
        values.put("zipcode", zipcode);
        values.put("property_price", property_price);
        values.put("down_payment", down_payment);
        values.put("apr", apr);
        values.put("terms", terms);
        values.put("payment", payment);
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity(), "test_db", 2);
        SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();

        sqliteDatabase.insert("user", null, values);

    }
}
