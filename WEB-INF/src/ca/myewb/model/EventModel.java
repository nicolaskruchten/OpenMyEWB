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

package ca.myewb.model;

import java.io.StringWriter;
import java.util.Date;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.logic.EventLogic;
import ca.myewb.logic.TagLogic;

public class EventModel extends EventLogic
{
	EventModel()
	{
		super();
	}
	
	EventModel(String name, Date date, Date date2, String location, String notes, GroupModel group, boolean whiteboard)
	{
		super(name, date, date2, location, notes, group);

		if(whiteboard)
		{
			setWhiteboard(WhiteboardModel.newWhiteboard(this, null, null));
		}
	}

	public static EventModel newEvent(String name, Date startDate, Date endDate, String location, String notes, GroupModel group, boolean whiteboard, String tags)
	{
		EventModel event = new EventModel(name, startDate, endDate, location, notes, group, whiteboard);
		HibernateUtil.currentSession().save(event);
		
		for(String tag: TagLogic.extractTagNames(tags))
		{
			event.addTag(TagModel.getOrCreateTag(tag));
		}
		
		group.addEvent(event);
		Helpers.currentDailyStats().logEventCreation();
		event.setSearchable(SearchableModel.newSearchable(null, event, null));
		
		return event;
	}
	
	public void save(String name, Date startDate, Date endDate, String location, String notes, GroupModel group, boolean whiteboard, String tags)
	{
		setName(name);
		setStartDate(startDate);
		setEndDate(endDate);
		setLocation(location);
		setNotes(notes);
		setGroup(group);
		
		for(String tag: TagLogic.extractTagNames(tags))
		{
			addTag(TagModel.getOrCreateTag(tag));
		}
		
		if( getWhiteboard() == null && whiteboard )
		{
			setWhiteboard(WhiteboardModel.newWhiteboard(this, null, null));
		}
		else if( getWhiteboard() != null ){
			getWhiteboard().setEnabled(whiteboard);
			getWhiteboard().setGroup(group);
		}
		
		getSearchable().update();
	}
	
	public void delete()
	{
		setName("*deleted* " + getName());
		setNotes("deleted " + (new Date()).toString() + ", original group was: "
		        + getGroup().getName() + "\n- - -\n" + getNotes());
		setGroup(Helpers.getGroup("DeletedPosts"));
		if(getWhiteboard() != null)
		{
			getWhiteboard().delete();
		}
		getSearchable().delete();
	}
	
	public void sendAsEmail(String sender, String subject, String body) throws Exception
	{

		String bodyText = doTemplateMerge(body, "emails/event.txt.vm");
		String bodyHTML = doTemplateMerge(body, "emails/event.html.vm");
		
		EmailModel.sendEmail(sender, 
				getGroup().getMemberEmails(), "[" + getGroup().getTotalShortname()+ "] " + subject, 
				bodyText, 
				bodyHTML, getGroup().getTotalShortname(), true);

		Helpers.currentDailyStats().logEventMailing();
	}
	
	private String doTemplateMerge(String body, String templatePath) throws Exception 
	{
		Template template = Velocity.getTemplate(templatePath);
		VelocityContext ctx = new VelocityContext();
		ctx.put("event", this);
		ctx.put("body", body);
		ctx.put("helpers", new Helpers());
	
		StringWriter writer = new StringWriter();
		template.merge(ctx, writer);
	
		String toString = writer.toString();
	
		return toString;
	}
}
