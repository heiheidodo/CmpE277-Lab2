package com.swipetry.huiyu.swipeviewtry;

/**
 * Created by huiyu on 3/16/17.
 */

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainFragment extends Fragment {
    private Button btn_submit;
    private Spinner spinner_property;
    private Spinner spinner_state;
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
    private Button btn_refresh;

    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rootView = inflater.inflate(R.layout.main_view, container, false);
        //build database
        //DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this, "test_db");
        //SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        //submit button and spinner
        btn_submit = (Button)rootView.findViewById(R.id.submit);
        btn_refresh = (Button)rootView.findViewById(R.id.refresh);
        spinner_property = (Spinner)rootView.findViewById(R.id.spinner_property);
        spinner_property.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] properties = getResources().getStringArray(R.array.property_type);
                property = properties[i];
                //Toast.makeText(MainActivity.this, "Selected: " + properties[i], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner_state = (Spinner)rootView.findViewById(R.id.spinner_state);
        spinner_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] states = getResources().getStringArray(R.array.state);
                state = states[i];
                //Toast.makeText(MainActivity.this, "Selected: " + states[i], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edit_street = (EditText) rootView.findViewById(R.id.street);
                street = edit_street.getText().toString();
                EditText edit_city = (EditText) rootView.findViewById(R.id.city);
                city = edit_city.getText().toString();
                EditText edit_zipcode = (EditText) rootView.findViewById(R.id.zipcode);
                zipcode = edit_zipcode.getText().toString();
                //showAlertDialog();

                EditText edit_price = (EditText) rootView.findViewById(R.id.property_price);
                property_price = ParseDouble(edit_price.getText().toString());
                EditText edit_downpayment = (EditText) rootView.findViewById(R.id.down_payment);
                down_payment = ParseDouble(edit_downpayment.getText().toString());
                EditText edit_apr = (EditText) rootView.findViewById(R.id.apr);
                apr = ParseDouble(edit_apr.getText().toString());
                EditText edit_terms = (EditText) rootView.findViewById(R.id.terms);
                terms = (int)ParseDouble(edit_terms.getText().toString());


                String address = street + ", " + city + ", " + state + ", " + zipcode;
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
                    Toast.makeText(getActivity(), "Address Not Found", Toast.LENGTH_SHORT).show();
                }
                else if (edit_downpayment.getText().toString() == null || edit_apr.getText().toString() == null ||
                        edit_price.getText().toString() == null || edit_terms.getText().toString() == null)
                {
                    Toast.makeText(getActivity(), "No inputs for mortgage calculation", Toast.LENGTH_SHORT).show();
                }else {
                    if (edit_apr.getText().toString().trim().equals("")) edit_apr.setError( "APR is required!" );
                    else if (edit_terms.getText().toString().trim().equals("")) edit_apr.setError( "Terms is required!" );
                    else if (edit_downpayment.getText().toString().trim().equals("")) edit_apr.setError( "Downpayment is required!" );
                    else if (edit_price.getText().toString().trim().equals("")) edit_apr.setError( "Property price is required!" );
                    else {
                        calculatePayment(property_price, down_payment, terms, apr);
                        dialogSaveBox();}
                    //saveInfo();
                }
            }
        });

        btn_refresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //reset
                EditText edit_street = (EditText) rootView.findViewById(R.id.street);
                EditText edit_city = (EditText) rootView.findViewById(R.id.city);
                EditText edit_zipcode = (EditText) rootView.findViewById(R.id.zipcode);
                EditText edit_price = (EditText) rootView.findViewById(R.id.property_price);
                EditText edit_downpayment = (EditText) rootView.findViewById(R.id.down_payment);
                EditText edit_apr = (EditText) rootView.findViewById(R.id.apr);
                EditText edit_terms = (EditText) rootView.findViewById(R.id.terms);
                edit_street.setText(null);
                edit_city.setText(null);
                edit_zipcode.setText(null);
                edit_price.setText(null);
                edit_downpayment.setText(null);
                edit_apr.setText(null);
                edit_terms.setText(null);
            }
        });


        return rootView;
    }
    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        //alertDialog.setMessage(property + "\n" + street + "\n" + city + "\n" + state + "\n" + zipcode);
        //alertDialog.setMessage(property + "\n" + street + "\n" + city + "\n" + state + "\n" + zipcode
        //        + "\n" + property_price + "\n" + down_payment + "\n" + apr + "\n" + terms);
        //alertDialog.show();
    }
    private void saveInfo() {
        String address = street + ", " + city + ", " + state + ", " + zipcode;
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
            Toast.makeText(getActivity(), "addressNotFound, can not save this data.", Toast.LENGTH_SHORT).show();
            return;
        }
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
    private double calculatePayment(double property_price, double down_payment, double terms, double apr)
    {
        double loan_amount = property_price - down_payment;
        double number_of_payments = terms * 12;
        double monthly_apr = apr/12/100;

        payment = loan_amount * monthly_apr * Math.pow(1 + monthly_apr, number_of_payments) /
                (Math.pow(1 + monthly_apr, number_of_payments) - 1);

        payment = round(payment, 2);
        //System.out.println(payment);
        return payment;
    }

    public void dialogSaveBox() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Monthly Payment is: " + payment + "\n" + "Do you want to save it?");
        alertDialogBuilder.setPositiveButton("Save",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        saveInfo();
                        Toast.makeText(getActivity(), "Mortgage Saved", Toast.LENGTH_SHORT).show();
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        // do nothing
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
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
}
