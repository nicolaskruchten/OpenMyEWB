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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ca.myewb.model.GroupModel;
import ca.myewb.model.SearchableModel;
import ca.myewb.model.TagModel;
import ca.myewb.model.WhiteboardModel;

public abstract class Event
{
	private Date startDate;
	private Date endDate;
	private String notes;
	private String name;
	private String location;
	private GroupModel group;
	private int id;
	protected Set<TagModel> tags;
	protected Collection<SearchableModel> searchables;
	protected Collection<WhiteboardModel> whiteboards;

	public int getId()
	{
		return id;
	}

	protected void setId(int id)
	{
		this.id = id;
	}

	protected Event()
	{
		startDate = new Date();
		endDate = new Date();
		notes = "";
		name = "";
		location = "";
		group = null;
		tags = new HashSet<TagModel>();
		whiteboards = new HashSet<WhiteboardModel>();
		searchables = new HashSet<SearchableModel>();
	}
	
	protected Event(String name2, Date date2, Date date3, String location2, String notes2, GroupModel group2)
	{
		name = name2;
		startDate = date2;
		endDate = date3;
		location = location2;
		notes = notes2;
		group = group2;
		tags = new HashSet<TagModel>();
		whiteboards = new HashSet<WhiteboardModel>();
		searchables = new HashSet<SearchableModel>();
	}

	public GroupModel getGroup()
	{
		return group;
	}
	protected void setGroup(GroupModel group)
	{
		this.group = group;
	}
	public String getLocation()
	{
		return location;
	}
	protected void setLocation(String locations)
	{
		this.location = locations;
	}
	public String getName()
	{
		return name;
	}
	protected void setName(String name)
	{
		this.name = name;
	}
	public String getNotes()
	{
		return notes;
	}
	protected void setNotes(String notes)
	{
		this.notes = notes;
	}
	public Date getStartDate()
	{
		return startDate;
	}
	protected void setStartDate(Date time)
	{
		startDate = time;
	}

	public Date getEndDate()
	{
		return endDate;
	}

	protected void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public Collection<WhiteboardModel> getWhiteboards() {
		return whiteboards;
	}

	public void setWhiteboards(Collection<WhiteboardModel> whiteboards) {
		this.whiteboards = whiteboards;
	}
	
	public Set<TagModel> getTags() {
		return tags;
	}

	public void setTags(Set<TagModel> tags) {
		this.tags = tags;
	}

	public Collection<SearchableModel> getSearchables() {
		return searchables;
	}

	public void setSearchables(Collection<SearchableModel> searchables) {
		this.searchables = searchables;
	}
	
}
