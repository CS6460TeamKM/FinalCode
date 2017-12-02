package com.teamkm.time_sheet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void Next(View view)
    {


        EditText edName = (EditText)findViewById(R.id.edName);
        EditText edID = (EditText)findViewById(R.id.edID);
        EditText edDate = (EditText)findViewById(R.id.edDate);

        String empName = edName.getText().toString();
        String empID = edID.getText().toString();
        String empDate = edDate.getText().toString();


        Intent NextActivity = new Intent(this,Details.class);
        Bundle bundle = new Bundle();
        bundle.putString("employeename",empName);
        bundle.putString("employeeId",empID);
        bundle.putString("employeeDate",empDate);

        NextActivity.putExtras(bundle);

        startActivity(NextActivity);
    }

}
