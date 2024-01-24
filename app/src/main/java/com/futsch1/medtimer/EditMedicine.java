package com.futsch1.medtimer;

import static com.futsch1.medtimer.ActivityCodes.EXTRA_ID;
import static com.futsch1.medtimer.ActivityCodes.EXTRA_INDEX;
import static com.futsch1.medtimer.ActivityCodes.EXTRA_MEDICINE;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futsch1.medtimer.adapters.ReminderViewAdapter;
import com.futsch1.medtimer.adapters.ReminderViewHolder;
import com.futsch1.medtimer.database.Medicine;
import com.futsch1.medtimer.database.MedicineWithReminders;
import com.futsch1.medtimer.database.Reminder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class EditMedicine extends AppCompatActivity {

    MedicineViewModel medicineViewModel;
    EditText editMedicineName;
    int medicineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medicine);

        medicineViewModel = new ViewModelProvider(this).get(MedicineViewModel.class);
        medicineId = getIntent().getIntExtra(EXTRA_ID, 0);

        editMedicineName = findViewById(R.id.editMedicineName);
        final Observer<List<MedicineWithReminders>> nameObserver = newList -> {
            if (newList != null) {
                Medicine medicine = newList.get(getIntent().getIntExtra(EXTRA_INDEX, 0)).medicine;
                editMedicineName.setText(medicine.name);
            }
        };

        ExtendedFloatingActionButton fab = findViewById(R.id.addReminder);
        fab.setOnClickListener(view -> {
            Reminder reminder = new Reminder(medicineId);
            medicineViewModel.insertReminder(reminder);
        });

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        medicineViewModel.getMedicines().observe(this, nameObserver);

        final ImageButton button = findViewById(R.id.saveMedicineButton);
        button.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(editMedicineName.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
            } else {
                String word = editMedicineName.getText().toString();
                replyIntent.putExtra(EXTRA_MEDICINE, word);
                replyIntent.putExtra(EXTRA_ID, medicineId);
                setResult(RESULT_OK, replyIntent);
            }
            finish();
        });

        RecyclerView recyclerView = findViewById(R.id.reminderList);
        final ReminderViewAdapter adapter = new ReminderViewAdapter(new ReminderViewAdapter.ReminderDiff(), medicineViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        medicineViewModel.getReminders(medicineId).observe(this, adapter::submitList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RecyclerView recyclerView = findViewById(R.id.reminderList);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ReminderViewHolder viewHolder = (ReminderViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));

            medicineViewModel.updateReminder(viewHolder.reminder);
        }
    }
}