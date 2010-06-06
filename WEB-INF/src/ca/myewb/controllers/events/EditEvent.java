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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.EventEditForm;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;


public class EditEvent extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		List<GroupModel> sendGroups = Permissions.sendGroups(currentUser);
		if(sendGroups.isEmpty())
		{
			setSessionMessage("There are no groups for which you are allowed to create events.");
			throw new RedirectionException(path + "/events/Events");
		}		
		
		EventEditForm f = (EventEditForm)checkForValidationFailure(ctx);
		urlParams.processParams(new String[]{"id", "group", "date"}, new String[]{"new", null, null});
		
		if(f == null)
		{
			f = new EventEditForm(path + "/actions/SaveEvent/" + urlParams.get("id"), requestParams,
					sendGroups, (currentUser.isMember("Exec", false) && urlParams.get("id").equals("new")));

			if(!urlParams.get("id").equals("new"))
			{
				EventModel theEvent = (EventModel)getAndCheckFromUrl(EventModel.class, "id");

				f.setValue("StartDate", editDateFormat(theEvent.getStartDate()));
				f.setValue("StartTime", editTimeFormat(theEvent.getStartDate()));
				float durationInHours = (float)(theEvent.getEndDate().getTime() - theEvent.getStartDate().getTime())/(float)(1000*60*60);
				f.setValue("Duration", new Float(durationInHours).toString());
				
				f.setValue("Name", theEvent.getName());
				f.setValue("Location", theEvent.getLocation());
				f.setValue("Notes", theEvent.getNotes());
				f.setValue("Group", Integer.toString(theEvent.getGroup().getId()));
				f.setValue("Whiteboard", (theEvent.hasActiveWhiteboard() ? "on" : "off") );
				f.setValue("Email", "off" );
				
				String keywords = new String();
				Iterator<String> it = theEvent.getSortedTags().iterator();
				while (it.hasNext())
				{
					keywords += it.next() + ", ";
				}
				
				f.setValue("Keywords", keywords);

				ctx.put("event", theEvent);
			}
			else
			{
				if(urlParams.get("group") != null && !urlParams.get("group").equals("nofilter"))
				{
					GroupModel g = (GroupModel)getAndCheckFromUrl(GroupModel.class, "group");
					f.setValue("Group", Integer.toString(g.getId()));
				}
				else
				{
					if(currentUser.isAdmin())
					{
						f.setValue("Group", Integer.toString(Helpers.getGroup("Exec").getId()));
					}
					else if(currentUser.isMember("Exec"))
					{
						f.setValue("Group", Integer.toString(currentUser.getChapter().getId()));
					}
					else
					{
						f.setValue("Group", Integer.toString(sendGroups.get(0).getId()));
					}
				}
				
				
				if(urlParams.get("date") != null)
				{
					f.setValue("StartDate", urlParams.get("date"));
				}
			}
			
		}
		else
		{
			if(!urlParams.get("id").equals("new"))
			{
				ctx.put("event", getAndCheckFromUrl(EventModel.class, "id"));
			}
		}
		
		ctx.put("form", f);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();

		s.add("Users");

		return s;
	}
	
	public int weight()
	{
		return -15;
	}

	public String displayName()
	{
		return "Add Event";
	}

	private String editDateFormat(Date date)
	{
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}
	
	private String editTimeFormat(Date date)
	{
		return new SimpleDateFormat("kk:mm").format(date);
	}
}
