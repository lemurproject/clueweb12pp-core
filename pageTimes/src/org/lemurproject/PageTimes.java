package org.lemurproject;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: shriphani
 * Date: 7/2/13
 * Time: 2:13 AM
 */
public class PageTimes {

    public static Calendar cluewebStart = new GregorianCalendar(2012, 01, 01);
    public static Calendar cluewebEnd = new GregorianCalendar(2012, 06, 30);

    public static ArrayList<Date> datesInText(String text) {
        ArrayList<Date> dates = new ArrayList<Date>();

        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(text);

        for (DateGroup group : groups) {
            List groupDates = group.getDates();
            dates.addAll(groupDates);
        }

        return dates;
    }

    public static boolean inTimeRange(Date date) {
        return (date.after(cluewebStart.getTime()) && date.before(cluewebEnd.getTime()));
    }
}
