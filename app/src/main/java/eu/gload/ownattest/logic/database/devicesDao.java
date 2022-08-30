package eu.gload.ownattest.logic.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface devicesDao {
    @Query("Select * from devices")
    List<devices> getDeviceList();

    @Query("Select * from devices where id=:searchID")
    devices getDeviceByID(int searchID);

    @Insert
    void insertDevice(devices device);

    @Update
    void updateDevice(devices device);

    @Delete
    void deleteDevice(devices device);

}

