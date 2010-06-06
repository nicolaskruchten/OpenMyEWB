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

import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.SearchableModel;
import ca.myewb.model.UserModel;

public abstract class Whiteboard {

	protected UserModel lastEditor;
	protected int id;
	protected int numEdits;
	protected String body; 
	protected Date lastEditDate; 
	protected EventModel parentEvent;
	protected PostModel parentPost;
	protected GroupModel parentGroup;
	protected GroupModel group;
	protected Collection<SearchableModel> searchables;
	protected boolean hasfile;
	protected boolean enabled;

	public Whiteboard() {
		id = 0;
		numEdits = 0;
		body = " "; // for 'text' fields, I think they can't be empty, which caused JUnit failure
		lastEditDate = new Date();
		parentEvent = null;
		lastEditor = null;
		hasfile = false;
		enabled = true;
		searchables = new HashSet<SearchableModel>();
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean getHasfile() {
		return hasfile;
	}

	public void setHasfile(boolean hasfile) {
		this.hasfile = hasfile;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(Date lastEdit) {
		this.lastEditDate = lastEdit;
	}

	public UserModel getLastEditor() {
		return lastEditor;
	}

	public void setLastEditor(User lastEditor) {
		this.lastEditor = (UserModel)lastEditor;
	}

	public int getNumEdits() {
		return numEdits;
	}

	public void setNumEdits(int numEdits) {
		this.numEdits = numEdits;
	}

	public EventModel getParentEvent() {
		return parentEvent;
	}

	public void setParentEvent(Event parentEvent) {
		this.parentEvent = (EventModel)parentEvent;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public PostModel getParentPost() {
		return parentPost;
	}

	public void setParentPost(PostModel parentPost) {
		this.parentPost = parentPost;
	}

	public GroupModel getParentGroup() {
		return parentGroup;
	}

	public void setParentGroup(GroupModel parentGroup) {
		this.parentGroup = parentGroup;
	}

	public GroupModel getGroup() {
		return group;
	}

	public void setGroup(GroupModel group) {
		this.group = group;
	}

	public Collection<SearchableModel> getSearchables() {
		return searchables;
	}

	public void setSearchables(Collection<SearchableModel> searchables) {
		this.searchables = searchables;
	}

}