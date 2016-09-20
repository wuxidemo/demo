package com.dw.merchant.activity;

import android.util.Log;
import android.widget.CalendarView;

import com.dw.merchant.R;
import com.dw.merchant.util.DateTimeUtils;

import java.util.Date;

public class DateChooseActivity extends BaseActivity {


    private static final String TAG = DateChooseActivity.class.getSimpleName() + "_lyx";

    private CalendarView calendar;

    @Override
    protected int addLayout() {
        return R.layout.activity_date_choose;
    }

    @Override
    protected void initLayout() {

        flag = "ok";

        calendar = (CalendarView) findViewById(R.id.calendarView);

        Date today = new Date();
        date = DateTimeUtils.date2Str(today, "yyyy-MM-dd");

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView arg0, int year,
                                            int monthOfYear, int dayOfMonth) {
                monthOfYear = monthOfYear + 1;

                String month = monthOfYear < 10 ? ("0" + monthOfYear) : ("" + monthOfYear);

                String day = dayOfMonth < 10 ? ("0" + dayOfMonth) : ("" + dayOfMonth);

                date = year + "-" + month + "-" + day;
                Log.i(TAG, "onSelectedDayChange: date===" + date);
            }
        });
    }
}
