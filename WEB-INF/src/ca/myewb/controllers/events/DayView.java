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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.EventList;
import ca.myewb.frame.Controller;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.GroupChapterModel;

public class DayView extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		Calendar cal = GregorianCalendar.getInstance();
		
		urlParams.processParams(new String[] {"filter",  "year", "month", "day" },
				new String[] {"nofilter",  Integer.toString(cal.get(Calendar.YEAR)), Integer.toString(cal.get(Calendar.MONTH) + 1), Integer.toString(cal.get(Calendar.DATE))});

		int requestedMonth = Integer.parseInt(urlParams.get("month"));
		int requestedYear = Integer.parseInt(urlParams.get("year"));
		int requestedDate = Integer.parseInt(urlParams.get("day"));
		cal = GregorianCalendar.getInstance();
		cal.set(Calendar.MONTH, requestedMonth - 1);
		cal.set(Calendar.YEAR, requestedYear);
		cal.set(Calendar.DATE, requestedDate);

		GroupChapterModel chapter = null;
		
		EventList events = new EventList(httpSession, hibernateSession, requestParams, urlParams, currentUser);
		
		if(!urlParams.get("filter").equals("nofilter"))
		{
			chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class, "filter");
		}
		ctx.put("filter", urlParams.get("filter"));
		
		ctx.put("events", events.listVisibleEventsForDay(cal.getTime(), chapter));

		ctx.put("theday", cal.getTime());
		ctx.put("year", cal.get(Calendar.YEAR));
		ctx.put("month", cal.get(Calendar.MONTH) + 1);
		ctx.put("day", cal.get(Calendar.DATE));
		
		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		toolbars.add(new ca.myewb.frame.toolbars.DayView());
		ctx.put("toolbars", toolbars);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Events by Day";
	}
}
