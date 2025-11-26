package org.example.Documents;

import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

public class Test {
    public static void main(String[] args) {
        Resident resident = UserDataManager.getInstance().getCurrentResident();
        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();

    }
}
