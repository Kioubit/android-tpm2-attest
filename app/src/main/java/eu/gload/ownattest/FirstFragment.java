package eu.gload.ownattest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

import eu.gload.ownattest.databinding.FragmentFirstBinding;
import eu.gload.ownattest.logic.Attestation;
import eu.gload.ownattest.logic.Callback;
import eu.gload.ownattest.logic.ThreadRunner;
import eu.gload.ownattest.logic.database.devices;

public class FirstFragment extends Fragment implements Callback {


    private FragmentFirstBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonAdd.setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));

        // -----
        tr.requestDeviceList();
        binding.buttonScan.setOnClickListener(view12 -> startScanBarcode());
        GetNonce();
        binding.spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                GetNonce();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        binding.buttonEditNonce.setOnClickListener(view14 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Edit nonce");
            final EditText input = new EditText(getContext());
            builder.setView(input);
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if (input.getText().toString().length() == 0 ) {
                    return;
                }
               binding.textviewNonce.setText(input.getText().toString());
               nonce = input.getText().toString();
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
        });

        binding.buttonDelete.setOnClickListener(view13 -> {
            devices device = (devices) binding.spinner1.getSelectedItem();
            if (device == null) {
                Toast.makeText(getContext(),"No device selected",Toast.LENGTH_LONG).show();
                return;
            }
            tr.DeleteDevice(device);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    //------------------------------------------------------------------------------//
    private  ThreadRunner tr;

    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private void init(){
        tr = ThreadRunner.getInstance(this);

        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Scanned. Please wait...", Toast.LENGTH_LONG).show();
                Object selectedItem = binding.spinner1.getSelectedItem();
                if (selectedItem == null)  {
                    Toast.makeText(getContext(),"No device selected",Toast.LENGTH_SHORT).show();
                    return;
                }
                tr.PerformAttestation((devices) selectedItem,result.getContents(),nonce);
            }
        });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        scanBarcode();
                    } else {
                        Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String nonce;
    private void GetNonce() {
        nonce = Attestation.GetNonce();
        binding.textviewNonce.setText(nonce);
    }

    public void startScanBarcode() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            scanBarcode();
        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA);
        }
    }
    public void scanBarcode() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setBeepEnabled(false);
        scanOptions.setTorchEnabled(false);
        scanOptions.setBarcodeImageEnabled(false);
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        barcodeLauncher.launch(scanOptions);
    }


    @Override
    public void DeviceListReady(List<devices> devicesList) {
        if (binding == null) {
            return;
        }
        Spinner dropdown = binding.spinner1;
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(),
                android.R.layout.simple_spinner_item,
                devicesList);
        dropdown.setAdapter(adapter);
    }

    @Override
    public void DeviceDeleted() {
        tr.requestDeviceList();
    }

    @Override
    public void DeviceAdded(ThreadRunner.CustomError error) {
        if (error != null) {
            Toast.makeText(getContext(),error.getErrorMessage(),Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(),"Device added",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ChangesAccepted() {
        Toast.makeText(getContext(),"Changes accepted", Toast.LENGTH_LONG).show();
    }

    @Override
    public void AttestationResultReady(Attestation.AttestationResult result) {
        Intent intent = new Intent(getContext(), ResultActivity.class);
        intent.putExtra("result", result);
        ResultActivityForResultLauncher.launch(intent);
    }
    ActivityResultLauncher<Intent> ResultActivityForResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 10) {
                    tr.requestDeviceList();
                }
            });
}