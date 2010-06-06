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

package ca.myewb.controllers.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Message;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.EventEditForm;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;


public class SaveEvent extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		EventEditForm form = new EventEditForm(path
		                                           + "/actions/SaveEvent/"
		                                           + urlParams.getParam(),
		                                           requestParams, Permissions.sendGroups(currentUser),
		                                           (currentUser.isMember("Exec", false) && 
		                                        		   urlParams.getParam().equals("new")));

		Message m = form.validate();

		// No messages: changes are valid
		if (m != null)
		{
				throw getValidationException(form, m,
				                             path + "/events/EditEvent/"
				                             + urlParams.getParam());
		}

		EventModel event;
		int eventID;
		
		String name = form.getParameter("Name");
		Date start = parseDateTime(form.getParameter("StartDate"), form.getParameter("StartTime"));
		Float duration = Float.parseFloat(form.getParameter("Duration"));
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.MINUTE, (int)(duration * 60.0));
		Date end = cal.getTime();
		
		String location = form.getParameter("Location");
		String notes = form.getParameter("Notes");
		int groupID = Integer.parseInt(form.getParameter("Group"));
		boolean whiteboard = form.getParameter("Whiteboard").equals("on") ;
		String tags = form.getParameter("Keywords");
		
		GroupModel group = (GroupModel)HibernateUtil.currentSession().load(GroupModel.class, groupID);
		
		if (urlParams.getParam().equals("new"))
		{
			event = EventModel.newEvent(name, start, end, location, notes, group, whiteboard, tags);
			eventID = event.getId();
		}
		else
		{
			event = (EventModel)getAndCheckFromUrl(EventModel.class);
			event.save(name, start, end, location, notes, group, whiteboard, tags);
			eventID = event.getId();
		}
		
		if( !Permissions.canUpdateEvent(currentUser, event) )
		{
			throw getSecurityException("You cannot edit this event.",
                    path + "/events/MonthView");
		}
		
		if( form.getParameter("Email").equals("on") )
		{
			throw new RedirectionException(path + "/events/SendEventEmail/" + eventID);
		}
		
		// Leave a message in the session
		setSessionMessage(("Event Info Updated"));

		// Redirect to somewhere
		throw new RedirectionException(path + "/events/EventInfo/" + eventID);
	}
	
	private Date parseDateTime(String date, String time) throws ParseException
	{
		return new SimpleDateFormat("yyyy-MM-dd kk:mm").parse(date + " " + time);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
