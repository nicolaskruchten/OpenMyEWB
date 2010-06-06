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

import java.util.HashSet;
import java.util.Set;

import ca.myewb.model.EventModel;
import ca.myewb.model.PostModel;

public abstract class Tag {

	protected int id;
	protected String name;
	protected String uniqueName;

	protected Set<PostModel> posts;
	protected Set<EventModel> events;
	
	public Tag()
	{
		id = 0;
		name = "";
		uniqueName = "";
		posts = new HashSet<PostModel>();
		events = new HashSet<EventModel>();
	}

	private int getId() {
		return id;
	}

	private void setId(int i) {
		id = i;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}
	
	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public Set getPosts() {
		return posts;
	}

	private void setPosts(Set<PostModel> p) {
		posts = p;
	}

	public Set<EventModel> getEvents() {
		return events;
	}

	public void setEvents(Set<EventModel> events) {
		this.events = events;
	}

}
