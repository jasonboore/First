package com.example.mq661.govproject.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Dateadd {

    public static String mydays(String days, int num) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = sdf.parse(days);

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);


        calendar.add(Calendar.DAY_OF_YEAR, num);
        Date dt1 = calendar.getTime();
        String reStr = sdf.format(dt1);
        System.out.println(reStr);
        return reStr;

    }


}