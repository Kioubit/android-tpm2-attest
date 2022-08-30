package eu.gload.ownattest.logic.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class devices {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "device_name")
    public String name;

    @ColumnInfo(name = "base64_pub_pem")
    public String base64Pem;

    public String AttestationJson;
}


