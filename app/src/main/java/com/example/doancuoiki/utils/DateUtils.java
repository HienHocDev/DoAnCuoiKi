package com.example.doancuoiki.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    private static final String OUTPUT_PATTERN = "dd/MM/yyyy";
    private static final String[] INPUT_PATTERNS = {
            "dd/MM/yyyy",
            "d/M/yyyy",
            "dd-MM-yyyy",
            "d-M-yyyy"
    };

    private DateUtils() {
    }

    public static String fromCalendarDate(int year, int month, int dayOfMonth) {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
    }

    public static boolean isSameDate(String inputDate, String selectedDate) {
        String normalizedInput = normalize(inputDate);
        String normalizedSelected = normalize(selectedDate);
        return !normalizedInput.isEmpty() && normalizedInput.equals(normalizedSelected);
    }

    public static boolean isDueSoon(String inputDate, int days) {
        Date dueDate = parse(inputDate);
        if (dueDate == null) {
            return false;
        }

        Calendar start = Calendar.getInstance();
        clearTime(start);

        Calendar end = Calendar.getInstance();
        clearTime(end);
        end.add(Calendar.DAY_OF_MONTH, days);

        Calendar due = Calendar.getInstance();
        due.setTime(dueDate);
        clearTime(due);

        return !due.before(start) && !due.after(end);
    }

    public static String normalize(String inputDate) {
        if (inputDate == null) {
            return "";
        }

        String trimmed = inputDate.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        for (String pattern : INPUT_PATTERNS) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                inputFormat.setLenient(false);
                Date date = inputFormat.parse(trimmed);
                if (date == null) {
                    continue;
                }

                SimpleDateFormat outputFormat = new SimpleDateFormat(OUTPUT_PATTERN, Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException ignored) {
            }
        }

        return trimmed;
    }

    private static Date parse(String inputDate) {
        if (inputDate == null) {
            return null;
        }

        String trimmed = inputDate.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        for (String pattern : INPUT_PATTERNS) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                inputFormat.setLenient(false);
                return inputFormat.parse(trimmed);
            } catch (ParseException ignored) {
            }
        }

        return null;
    }

    private static void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
