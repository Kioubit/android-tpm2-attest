package eu.gload.ownattest.logic;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.Base64;
import java.util.List;

import eu.gload.ownattest.App;
import eu.gload.ownattest.logic.database.AppDatabase;
import eu.gload.ownattest.logic.database.devices;

public class ThreadRunner {

    private static ThreadRunner INSTANCE;
    public static synchronized ThreadRunner getInstance(Callback callback) {
        if (INSTANCE == null) {
            INSTANCE = new ThreadRunner();
            INSTANCE.startWorkThread();
        }
        if (callback == null) { return INSTANCE;}
        INSTANCE.callback = callback;
        return INSTANCE;
    }

    private Callback callback;
    private Handler WorkThreadHandler;

    private void startWorkThread() {
        HandlerThread thread = new HandlerThread("WorkThread");
        thread.start();
        WorkThreadHandler = new Handler(thread.getLooper());
    }


    private void runOnMainThread(Runnable r) {
        Looper l = App.getMContext().getMainLooper();
        Handler h = new Handler(l);
        h.post(r);
    }


    public static class CustomError{
        CustomError(String errorMsg){
            this.ErrorMessage = errorMsg;
        }
        private final String ErrorMessage;

        public String getErrorMessage() {
            return ErrorMessage;
        }
    }


    public void requestDeviceList() {
        final Runnable r  = () -> {
            List<devices> list = AppDatabase.getInstance(App.getMContext()).userDao().getDeviceList();
            runOnMainThread(() -> callback.DeviceListReady(list));
        };
        WorkThreadHandler.post(r);
    }

    public void DeleteDevice(devices device) {
        final Runnable r  = () -> {
            AppDatabase.getInstance(App.getMContext()).userDao().deleteDevice(device);
            runOnMainThread(() -> callback.DeviceDeleted());
        };
        WorkThreadHandler.post(r);
    }


    public void PerformAttestation(devices device, String input, String nonce) {
        final Runnable r  = () -> {
            Attestation.AttestationResult result = Attestation.Perform(input,device.base64Pem,nonce,device);
            runOnMainThread(() -> callback.AttestationResultReady(result));
        };
        WorkThreadHandler.post(r);
    }

    public void AddDevice(String name, String pubKey) {
        final Runnable r  = () -> {

            if (name.equals("") || pubKey.equals("")) {
                runOnMainThread(() -> callback.DeviceAdded(new CustomError("Some values not filled in")));
                return;
            }
            try {
                if (!Base64.getEncoder().encodeToString(Base64.getDecoder().decode(pubKey)).equals(pubKey)) {
                    throw new Exception();
                }
            } catch (Exception ignored) {
                runOnMainThread(() -> callback.DeviceAdded(new CustomError("Invalid public key value")));
                return;
            }

            devices device = new devices();
            device.name = name;
            device.base64Pem = pubKey;
            device.AttestationJson = "";
            AppDatabase.getInstance(App.getMContext()).userDao().insertDevice(device);
            runOnMainThread(() -> callback.DeviceAdded(null));
        };
        WorkThreadHandler.post(r);
    }

    public void AcceptChanges(Attestation.AttestationResult result) {
        final Runnable r  = () -> {
            devices newDevice = new devices();
            newDevice.id = result.DeviceID;
            newDevice.name = result.DeviceName;
            newDevice.base64Pem = result.pemBase64;
            newDevice.AttestationJson = result.NewAttestationJson;
            AppDatabase.getInstance(App.getMContext()).userDao().updateDevice(newDevice);
            runOnMainThread(() -> callback.ChangesAccepted());
        };
        WorkThreadHandler.post(r);
    }
}

