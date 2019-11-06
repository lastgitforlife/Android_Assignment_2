package com.example.assignment_2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseBlood;
    EditText editTextUserId;
    EditText editTextSystolic;
    EditText editTextDiastolic;
    EditText editTextDate;
    EditText editTextTime;

    Button buttonAddBlood;

    ListView lvBlood;
    List<BloodPressure> bloodPressureList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup databaseBlood with a reference to the real database
        databaseBlood = FirebaseDatabase.getInstance().getReference("users");

        editTextUserId = findViewById(R.id.editTextUserId);
        editTextSystolic = findViewById(R.id.editTextSystolic);
        editTextDiastolic = findViewById(R.id.editTextDiastolic);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
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

    private void addBlood() {
        String userId = editTextUserId.getText().toString().trim();
        String systolic = editTextSystolic.getText().toString().trim();
        String diastolic = editTextDiastolic.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();

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

        if (TextUtils.isEmpty(date)) {
            Toast.makeText(this, "You must enter a date.", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(time)) {
            Toast.makeText(this, "You must enter a time.", Toast.LENGTH_LONG).show();
            return;
        }

        BloodPressure bloodPressure = new BloodPressure(userId, systolic, diastolic, date, time);

        Task setValueTask = databaseBlood.child(userId).child(date + '-' + time).setValue(bloodPressure);

        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,"BloodPressure added.",Toast.LENGTH_LONG).show();

                editTextUserId.setText("");
                editTextSystolic.setText("");
                editTextDiastolic.setText("");
                editTextDate.setText("");
                editTextTime.setText("");
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

        BloodPressure bloodPressure = new BloodPressure(userId, systolic,diastolic, date, time);

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
