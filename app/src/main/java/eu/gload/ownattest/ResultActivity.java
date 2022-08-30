package eu.gload.ownattest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

import eu.gload.ownattest.logic.Attestation;
import eu.gload.ownattest.logic.ThreadRunner;

public class ResultActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        Attestation.AttestationResult result = (Attestation.AttestationResult)  intent.getSerializableExtra("result");

        final TextView tv_resultType = findViewById(R.id.tv_resultType);
        final TextView tv_deviceName = findViewById(R.id.tv_deviceName);
        final TextView tv_description = findViewById(R.id.tv_description);
        final Button btn_acceptChanges = findViewById(R.id.btn_acceptChanges);
        final Button btn_OK = findViewById(R.id.btn_ok);


        btn_OK.setOnClickListener(view -> {
            setResult(0);
            finish();
        });

        tv_resultType.setText(result.type.name());
        tv_deviceName.setText(result.DeviceName);

        switch (result.type) {
            case FAILED:
                tv_resultType.setBackgroundColor(Color.parseColor("#ff4326"));
                tv_description.setText(result.FailReason);
                break;
            case CHANGED:
                tv_resultType.setBackgroundColor(Color.parseColor("#ffd900"));
                tv_description.setText(differencesToString(result.differences));
                btn_acceptChanges.setEnabled(true);
                btn_acceptChanges.setOnClickListener(view -> {
                    ThreadRunner.getInstance(null).AcceptChanges(result);
                    setResult(10);
                    finish();
                });
                break;
            case OK:
                tv_resultType.setBackgroundColor(Color.parseColor("#2bc241"));
                tv_description.setText(result.NewAttestationJson);
                break;
        }


    }

    private String differencesToString(Map<Integer, Attestation.SerializablePair<String, String>> differences) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Attestation.SerializablePair<String, String>> entry : differences.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue().first).append(" --> ").append(entry.getValue().second).append("\n");
        }
        return sb.toString();
    }

}