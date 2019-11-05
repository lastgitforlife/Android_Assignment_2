package com.example.assignment_2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

    DatabaseReference databaseStudents;
    EditText editTextUserId;
    EditText editTextSystolic;
    EditText editTextDiastolic;
    EditText editTextDate;
    EditText editTextTime;

    Spinner spinnerSchool;
    Button buttonAddStudent;

    ListView lvStudents;
    List<BloodPressure> bloodPressureList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup databaseStudents with a reference to the real database
        databaseStudents = FirebaseDatabase.getInstance().getReference("users");

        editTextUserId = findViewById(R.id.editTextUserId);
        editTextSystolic = findViewById(R.id.editTextSystolic);
        editTextDiastolic = findViewById(R.id.editTextDiastolic);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        buttonAddStudent = findViewById(R.id.buttonAddStudent);
        spinnerSchool = findViewById(R.id.spinnerSchool);

        buttonAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStudent();
            }
        });

        lvStudents = findViewById(R.id.lvStudents);
        bloodPressureList = new ArrayList<BloodPressure>();

        lvStudents.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                BloodPressure bloodPressure = bloodPressureList.get(position);

                showUpdateDialog(bloodPressure.getUserId(),
                        bloodPressure.getSystolic(),
                        bloodPressure.getDiastolic(),
                        bloodPressure.getSchool());

                return false;
            }
        });



    }

    private void addStudent() {
        String firstName = editTextUserId.getText().toString().trim();
        String lastName = editTextSystolic.getText().toString().trim();
        String school = spinnerSchool.getSelectedItem().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(this, "You must enter a first name.", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "You must enter a last name.", Toast.LENGTH_LONG).show();
            return;
        }

        String id = databaseStudents.push().getKey();
        BloodPressure bloodPressure = new BloodPressure(id, firstName, lastName, school);

        Task setValueTask = databaseStudents.child(id).setValue(bloodPressure);

        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,"BloodPressure added.",Toast.LENGTH_LONG).show();

                editTextUserId.setText("");
                editTextSystolic.setText("");
                spinnerSchool.setSelection(0);
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
        databaseStudents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bloodPressureList.clear();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    BloodPressure bloodPressure = studentSnapshot.getValue(BloodPressure.class);
                    bloodPressureList.add(bloodPressure);
                }

                BloodPressureListAdapter adapter = new BloodPressureListAdapter(MainActivity.this, bloodPressureList);
                lvStudents.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void updateStudent(String id, String firstName, String lastName, String school) {
        DatabaseReference dbRef = databaseStudents.child(id);

        BloodPressure bloodPressure = new BloodPressure(id,firstName,lastName,school);

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

    private void showUpdateDialog(final String studentId, String firstName, String lastName, String school) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialoge, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextFirstName = dialogView.findViewById(R.id.editTextFirstName);
        editTextFirstName.setText(firstName);

        final EditText editTextLastName = dialogView.findViewById(R.id.editTextLastName);
        editTextLastName.setText(lastName);

        final Spinner spinnerSchool = dialogView.findViewById(R.id.spinnerSchool);
        spinnerSchool.setSelection(((ArrayAdapter<String>)spinnerSchool.getAdapter()).getPosition(school));

        final Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        dialogBuilder.setTitle("Update BloodPressure " + firstName + " " + lastName);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editTextFirstName.getText().toString().trim();
                String lastName = editTextLastName.getText().toString().trim();
                String school = spinnerSchool.getSelectedItem().toString().trim();

                if (TextUtils.isEmpty(firstName)) {
                    editTextFirstName.setError("First Name is required");
                    return;
                } else if (TextUtils.isEmpty(lastName)) {
                    editTextLastName.setError("Last Name is required");
                    return;
                }

                updateStudent(studentId, firstName, lastName, school);

                alertDialog.dismiss();
            }
        });

        final Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteStudent(studentId);

                alertDialog.dismiss();
            }
        });

    }

    private void deleteStudent(String id) {
        DatabaseReference dbRef = databaseStudents.child(id);

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
