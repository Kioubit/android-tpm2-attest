package eu.gload.ownattest.logic;

import java.util.List;

import eu.gload.ownattest.logic.database.devices;

public interface Callback {
    void DeviceListReady(List<devices> devicesList);
    void DeviceDeleted();
    void DeviceAdded(ThreadRunner.CustomError error);
    void ChangesAccepted();
    void AttestationResultReady(Attestation.AttestationResult result);
}
