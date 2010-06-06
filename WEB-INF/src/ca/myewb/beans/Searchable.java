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

package ca.myewb.beans;

import java.util.Date;

import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.WhiteboardModel;

public class Searchable
{
	protected int id;
	protected String body;
	protected PostModel parentPost;
	protected EventModel parentEvent;
	protected WhiteboardModel parentWhiteboard;
	protected GroupModel group;
	protected Date date;
	
	public Searchable() 
	{
		
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public GroupModel getGroup()
	{
		return group;
	}

	public void setGroup(GroupModel group)
	{
		this.group = group;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public EventModel getParentEvent()
	{
		return parentEvent;
	}

	public void setParentEvent(EventModel parentEvent)
	{
		this.parentEvent = parentEvent;
	}

	public PostModel getParentPost()
	{
		return parentPost;
	}

	public void setParentPost(PostModel parentPost)
	{
		this.parentPost = parentPost;
	}

	public WhiteboardModel getParentWhiteboard()
	{
		return parentWhiteboard;
	}

	public void setParentWhiteboard(WhiteboardModel parentWhiteboard)
	{
		this.parentWhiteboard = parentWhiteboard;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}
}
