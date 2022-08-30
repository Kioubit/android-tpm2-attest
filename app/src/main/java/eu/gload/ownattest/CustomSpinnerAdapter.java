package eu.gload.ownattest;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import eu.gload.ownattest.logic.database.devices;

public class CustomSpinnerAdapter extends ArrayAdapter<devices> {

    // Your custom values for the spinner (User)
    private final List<devices> values;

    public CustomSpinnerAdapter(Context context, int textViewResourceId,
                                List<devices> values) {
        super(context, textViewResourceId, values);
        // Your sent context
        this.values = values;
    }

    @Override
    public int getCount(){
        return values.size();
    }

    @Override
    public devices getItem(int position){
        Log.d("eeee", "getItem");
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(values.get(position).name);
        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setText(values.get(position).name);

        return label;
    }
}