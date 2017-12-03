package com.teamkm.time_sheet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final  String TAG = "MainActivity";
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView edDate = (TextView)findViewById(R.id.edDate);
        edDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,
                        android.R.style.Theme_Holo_Light_DarkActionBar,mDateSetListener,year,month,day
                        );

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();


            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = month+"/"+dayOfMonth+"/"+year;
                edDate.setText(date);
            }
        };

    }




    public String[] calculatePayPeriods(String strdate) throws ParseException
    {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String[] strdates= new String[4];
        int[] sub = {0,-6,-7,-13};
        Date date = new SimpleDateFormat("MM/dd/yyyy").parse(strdate);

        for(int i =0;i<4;i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, sub[i]);
            Date startdate = cal.getTime();
            String reportDate = df.format(startdate);
            strdates[i]= reportDate;
        }



        return strdates;
    }


    public void Next(View view) throws ParseException
    {


        EditText edName = (EditText)findViewById(R.id.edName);
        EditText edID = (EditText)findViewById(R.id.edID);
        TextView edDate = (TextView)findViewById(R.id.edDate);

        String empName = edName.getText().toString();
        String empID = edID.getText().toString();
        String empDate = edDate.getText().toString();

        String[] strdates = calculatePayPeriods(empDate);

        Intent NextActivity = new Intent(this,Details.class);
        Bundle bundle = new Bundle();
        bundle.putString("employeename",empName);
        bundle.putString("employeeId",empID);
        bundle.putString("employeeDate",empDate);
        bundle.putStringArray("dates",strdates);

        NextActivity.putExtras(bundle);

        startActivity(NextActivity);
    }

}
