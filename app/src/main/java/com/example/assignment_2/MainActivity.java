package com.example.assignment_2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseBlood;
    EditText editTextUserId;
    EditText editTextSystolic;
    EditText editTextDiastolic;

    Button buttonAddBlood;

    ListView lvBlood;
    List<BloodPressure> bloodPressureList;
    List<BloodPressure> userPressureList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup databaseBlood with a reference to the real database
        databaseBlood = FirebaseDatabase.getInstance().getReference("users");

        editTextUserId = findViewById(R.id.editTextUserId);
        editTextSystolic = findViewById(R.id.editTextSystolic);
        editTextDiastolic = findViewById(R.id.editTextDiastolic);
        buttonAddBlood = findViewById(R.id.buttonAddBlood);

        buttonAddBlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBlood();
            }
        });

        lvBlood = findViewById(R.id.lvStudents);
        bloodPressureList = new ArrayList<BloodPressure>();

        lvBlood.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                BloodPressure bloodPressure = bloodPressureList.get(position);

                showUpdateDialog(bloodPressure.getUserId(),
                        bloodPressure.getSystolic(),
                        bloodPressure.getDiastolic(),
                        bloodPressure.getDate(),
                        bloodPressure.getTime()
                );

                return false;
            }
        });



    }

    private void checkForCrisis(int systolic, int diastolic) {
        if(systolic > 180 || diastolic > 120){
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Your are experiencing a hypertensive crisis. Consult your doctor immediately.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private String checkCondition(int systolic, int diastolic) {
        if(systolic > 180 || diastolic > 120){
            return "Hypertensive Crisis";
        }
        else if(systolic >= 140 || diastolic >= 90){
            return "High Blood Pressure (Stage 2)";
        }
        else if(systolic >= 130 || diastolic >= 80){
            return "High Blood Pressure (Stage 1)";
        }
        else if(systolic >= 120){
            return "Elevated";
        }
        else {
            return "Normal";
        }
    }

    private void addBlood() {
        String userId = editTextUserId.getText().toString().trim();
        String systolic = editTextSystolic.getText().toString().trim();
        String diastolic = editTextDiastolic.getText().toString().trim();
        Date datetime = Calendar.getInstance().getTime();
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.CANADA).format(datetime);
        String time = new SimpleDateFormat("HH:mm:ss", Locale.CANADA).format(datetime);

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "You must enter a User Id.", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(systolic)) {
            Toast.makeText(this, "You must enter Systolic pressure.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(diastolic)) {
            Toast.makeText(this, "You must enter a Diastolic pressure.", Toast.LENGTH_LONG).show();
            return;
        }

        checkForCrisis(Integer.parseInt(systolic), Integer.parseInt(diastolic));

        String condition = checkCondition(Integer.parseInt(systolic), Integer.parseInt(diastolic));

        BloodPressure bloodPressure = new BloodPressure(userId, systolic, diastolic, date, time, condition);

        Task setValueTask = databaseBlood.child(userId).child(date + '-' + time).setValue(bloodPressure);

        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,"BloodPressure added.",Toast.LENGTH_LONG).show();

                editTextUserId.setText("");
                editTextSystolic.setText("");
                editTextDiastolic.setText("");
            }
        });

        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseBlood.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bloodPressureList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot bloodSnapshot : userSnapshot.getChildren()) {
                        BloodPressure bloodPressure = bloodSnapshot.getValue(BloodPressure.class);
                        bloodPressureList.add(bloodPressure);
                    }
                }

                BloodPressureListAdapter adapter = new BloodPressureListAdapter(MainActivity.this, bloodPressureList);
                lvBlood.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void updateBlood(String userId, String systolic, String diastolic, String date, String time) {
        DatabaseReference dbRef = databaseBlood.child(userId).child(date + "-" + time);

        checkForCrisis(Integer.parseInt(systolic), Integer.parseInt(diastolic));

        String condition = checkCondition(Integer.parseInt(systolic), Integer.parseInt(diastolic));

        BloodPressure bloodPressure = new BloodPressure(userId, systolic,diastolic, date, time, condition);

        Task setValueTask = dbRef.setValue(bloodPressure);

        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,
                        "BloodPressure Updated.",Toast.LENGTH_LONG).show();
            }
        });

        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "Something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(final String userId, String systolic, String diastolic, final String date, final String time) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialoge, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextSystolic = dialogView.findViewById(R.id.editTextSystolic);
        editTextSystolic.setText(systolic);

        final EditText editTextDiastolic = dialogView.findViewById(R.id.editTextDiastolic);
        editTextDiastolic.setText(diastolic);

        final Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        dialogBuilder.setTitle("Update BloodPressure");

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String systolic = editTextSystolic.getText().toString().trim();
                String diastolic = editTextDiastolic.getText().toString().trim();

                if (TextUtils.isEmpty(systolic)) {
                    editTextSystolic.setError("Systolic is required");
                    return;
                } else if (TextUtils.isEmpty(diastolic)) {
                    editTextDiastolic.setError("Diastolic is required");
                    return;
                }

                updateBlood(userId, systolic, diastolic, date, time);

                alertDialog.dismiss();
            }
        });

        final Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBlood(userId, date, time);

                alertDialog.dismiss();
            }
        });

        final Button buttonReport = dialogView.findViewById(R.id.buttonReport);
        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                createReport(userId);
            }
        });

    }

    private void createReport(final String userId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.report_dialogue, null);
        dialogBuilder.setView(dialogView);

        final TextView tvReport = dialogView.findViewById((R.id.textViewReport));

        double avgSystolic = 0;
        double avgDiastolic = 0;

        int count = 0;
        for (BloodPressure bp : bloodPressureList) {
            if(bp.getUserId().equals(userId)) {
                avgSystolic += Integer.parseInt(bp.getSystolic());
                avgDiastolic += Integer.parseInt(bp.getDiastolic());
                count ++;
            }
        }

        avgSystolic = avgSystolic / count;
        avgDiastolic = avgDiastolic / count;

        String condition = checkCondition((int)avgSystolic, (int)avgDiastolic);

        tvReport.setText(String.format(Locale.CANADA,"%s\nAverage BP: %.2f/%.2f\nCondition: %s",
                userId, avgSystolic, avgDiastolic, condition));

        final AlertDialog reportDialog = dialogBuilder.create();
        reportDialog.show();
    }

    private void deleteBlood(String userId, String date, String time) {
        DatabaseReference dbRef = databaseBlood.child(userId).child(date + "-" + time);

        Task setRemoveTask = dbRef.removeValue();
        setRemoveTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,
                        "BloodPressure Deleted.",Toast.LENGTH_LONG).show();
            }
        });

        setRemoveTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "Something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}
