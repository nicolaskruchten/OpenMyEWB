/*

    This file is part of OpenMyEWB.

    OpenMyEWB is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenMyEWB is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenMyEWB.  If not, see <http://www.gnu.org/licenses/>.

    OpenMyEWB is Copyright 2005-2009 Nicolas Kruchten (nicolas@kruchten.com), Francis Kung, Engineers Without Borders Canada, Michael Trauttmansdorff, Jon Fishbein, David Kadish

*/

package ca.myewb.controllers.events;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.EventList;
import ca.myewb.frame.Controller;
import ca.myewb.model.GroupChapterModel;

public class MonthView extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		
		Calendar cal = GregorianCalendar.getInstance();
		int today = cal.get(Calendar.DATE);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentYear = cal.get(Calendar.YEAR);

		urlParams.processParams(new String[] {"filter", "year", "month" }, 
				new String[] {"nofilter", Integer.toString(currentYear), Integer.toString(currentMonth) });

		int requestedMonth = Integer.parseInt(urlParams.get("month"));
		int requestedYear = Integer.parseInt(urlParams.get("year"));
		
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, requestedMonth - 1);
		cal.set(Calendar.YEAR, requestedYear);

		GroupChapterModel chapter = null;
		if(!urlParams.get("filter").equals("nofilter"))
		{
			chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class, "filter");
		}
		
		EventList events = new EventList(httpSession, hibernateSession, requestParams, urlParams, currentUser);
		
		ctx.put("calendar", events.mapToDateVisibleEventsForMonth(cal.getTime(), chapter));
		ctx.put("cal", cal);
		ctx.put("datelist", events.getDatesInMonth(cal.getTime()));
		ctx.put("chapters", GroupChapterModel.getChapters());
		ctx.put("theChapter", chapter);
		ctx.put("filter", urlParams.get("filter"));
		ctx.put("year", requestedYear);
		ctx.put("month", requestedMonth);
		ctx.put("monthName", new SimpleDateFormat("MMMMM").format(cal.getTime()));
		
		if((currentMonth == requestedMonth) && (currentYear == requestedYear))
		{
			ctx.put("today", today);
		}
		else
		{
			ctx.put("today", -1);
		}


	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Calendar";
	}
}
