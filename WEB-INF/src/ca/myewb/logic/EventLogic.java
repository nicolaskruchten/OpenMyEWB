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

package ca.myewb.logic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ca.myewb.beans.Event;
import ca.myewb.model.GroupModel;
import ca.myewb.model.SearchableModel;
import ca.myewb.model.TagModel;
import ca.myewb.model.WhiteboardModel;

public abstract class EventLogic extends Event
{
	protected EventLogic()
	{
		super();
	}

	protected EventLogic(String name, Date date, Date date2, String location, String notes, GroupModel group)
	{
		super(name, date, date2, location, notes, group);
	}
	
	public int getYear()
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(getStartDate());
		return cal.get(Calendar.YEAR);
	}
	
	public int getMonth()
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(getStartDate());
		return cal.get(Calendar.MONTH) + 1;
	}
	
	public int getDay()
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(getStartDate());
		return cal.get(Calendar.DATE);
	}
	
	public TreeSet<String> getSortedTags() {
		TreeSet<String> sorted = new TreeSet<String>();
	
		for (TagLogic tag : tags)
		{
			sorted.add(tag.getName());
		}
	
	
		return sorted;
	}


	public void addTag(TagLogic t) {
		Logger.getLogger(this.getClass()).info(tags.size());
		Logger.getLogger(this.getClass()).info(t.getName());
		tags.add((TagModel)t);
		Logger.getLogger(this.getClass()).info("Tag " + t.getName() + " added to event " + this.getName());
		t.addEvent(this);
	}

	public void remTag(TagLogic t) {
		tags.remove(t);
		t.remEvent(this);
	}
	
	public boolean hasActiveWhiteboard()
	{
		if(getWhiteboard() == null)
		{
			return false;
		}
		else if(!getWhiteboard().isEnabled())
		{
			return false;
		}
		
		return true;
	}
	
	public boolean hasStartTime()
	{
		return !(new SimpleDateFormat("kk:mm").format(getStartDate())).equals("24:00");
	}
	

	public WhiteboardModel getWhiteboard() 
	{
		if(whiteboards.size() == 0)
			return null;
		
		return whiteboards.iterator().next();
	}

	public void setWhiteboard(WhiteboardModel whiteboard) 
	{
		if(whiteboards.size() != 0)
			whiteboards.remove(whiteboards.iterator().next());
		
		whiteboards.add(whiteboard);
	}
	

	public SearchableModel getSearchable()
	{
		if(searchables.size() == 0)
			return null;
		
		return searchables.iterator().next();
	}

	public void setSearchable(SearchableModel searchable)
	{
		if(searchables.size() != 0)
			searchables.remove(searchables.iterator().next());
		
		searchables.add(searchable);
	}

}
