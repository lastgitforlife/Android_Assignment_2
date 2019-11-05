package com.example.assignment_2;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class BloodPressureListAdapter extends ArrayAdapter<BloodPressure> {
    private Activity context;
    private List<BloodPressure> bloodPressureList;

    public BloodPressureListAdapter(Activity context, List<BloodPressure> bloodPressureList) {
        super(context, R.layout.list_layout, bloodPressureList);
        this.context = context;
        this.bloodPressureList = bloodPressureList;
    }

    public BloodPressureListAdapter(Context context, int resource, List<BloodPressure> objects, Activity context1, List<BloodPressure> bloodPressureList) {
        super(context, resource, objects);
        this.context = context1;
        this.bloodPressureList = bloodPressureList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.list_layout, null, true);

        TextView tvName = listViewItem.findViewById(R.id.textViewName);
        TextView tvSchool = listViewItem.findViewById(R.id.textViewSchool);

        BloodPressure bloodPressure = bloodPressureList.get(position);
        tvName.setText(bloodPressure.getSystolic()
                + " " + bloodPressure.getDiastolic());
        tvSchool.setText(bloodPressure.getSchool());

        return listViewItem;
    }

}

