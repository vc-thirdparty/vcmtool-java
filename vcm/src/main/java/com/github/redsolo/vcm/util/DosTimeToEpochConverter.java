package com.github.redsolo.vcm.util;

import java.util.Calendar;

public class DosTimeToEpochConverter {

    private DosTimeToEpochConverter() {
    }
    
    public static long convert(int dosTime) {
             
        int sec = 2 * (dosTime & 0x1f);
        int min = (dosTime >> 5) & 0x3f;
        int hrs = (dosTime >> 11) & 0x1f;
        int day = (dosTime >> 16) & 0x1f;
        int mon = ((dosTime >> 21) & 0xf) - 1;
        int year = ((dosTime >> 25) & 0x7f) + 1980;        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, mon, day, hrs, min, sec);    
        return cal.getTime().getTime();
    }
}
