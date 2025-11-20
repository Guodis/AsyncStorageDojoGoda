package com.example.asyncstoragedojo;
import static android.app.PendingIntent.getActivity;

import static com.example.asyncstoragedojo.MainActivity.selectedDate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it.
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // The month is 0-based, so add 1 for correct display.
        String date = String.format(Locale.getDefault(), "%02d-%02d-%02d", year, month + 1, day);

        // Assign the selected date to the static field in MainActivity
        selectedDate = date;
        Toast.makeText(getActivity(), selectedDate, Toast.LENGTH_SHORT).show();
    }
}
