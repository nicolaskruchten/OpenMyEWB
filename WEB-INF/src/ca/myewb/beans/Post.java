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
import ca.myewb.model.PostModel;
import ca.myewb.model.SearchableModel;
import ca.myewb.model.TagModel;
import ca.myewb.model.UserModel;
import ca.myewb.model.WhiteboardModel;

public abstract class Post {

	public static int FlagsToFeature = 7;
	public static int RepliesToFeature = 7;

	private UserModel poster;
	protected int id;
	protected GroupModel group;
	protected String subject;
	protected String intro;
	protected String body;
	protected Date date;
	protected Date lastReply;
	protected Set<TagModel> tags;
	protected PostModel parent;
	protected Collection<SearchableModel> searchables;
	protected Set<PostModel> replies;
	protected Set<UserModel> flaggedByUsers;
	protected boolean featured;
	protected boolean emailed;
	protected boolean hasfile;
	protected Collection<WhiteboardModel> whiteboards;
	
	public Post()
	{
		id = 0;
		subject = "";
		body = " "; // for 'text' fields, I think they can't be empty, which caused JUnit failure
		intro = " ";
		date = new Date();
		lastReply = new Date();
		tags = new HashSet<TagModel>();
		parent = null;
		replies = new HashSet<PostModel>();
		flaggedByUsers = new HashSet<UserModel>();
		whiteboards = new HashSet<WhiteboardModel>();
		featured = false;
		emailed = false;
		hasfile = false;
		searchables = new HashSet<SearchableModel>();
	}
	
	public UserModel getPoster() {
		return poster;
	}

	public void setPoster(User p) {
		poster = (UserModel)p;
	}
	
	public PostModel getParent() {
		return parent;
	}

	protected void setParent(Post parent) {
		this.parent = (PostModel)parent;
	}

	public int getId() {
		return id;
	}

	private void setId(int i) {
		id = i;
	}

	public GroupModel getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = (GroupModel)group;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String s) {
		subject = s;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String i) {
		intro = i;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String b) {
		body = b;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date d) {
		date = d;
	}

	public Set getTags() {
		return tags;
	}

	protected void setTags(Set<TagModel> t) {
		tags = t;
	}
	
	public Set<PostModel> getReplies() {
		return replies;
	}

	protected void setReplies(Set<PostModel> replies) {
		this.replies = replies;
	}

	public Set<UserModel> getFlaggedByUsers() {
		return flaggedByUsers;
	}

	public void setFlaggedByUsers(Set<UserModel> flaggedByUsers) {
		this.flaggedByUsers = flaggedByUsers;
	}

	public boolean getEmailed()
	{
		return emailed;
	}

	public void setEmailed(boolean emailed)
	{
		this.emailed = emailed;
	}

	public boolean getHasfile()
	{
		return hasfile;
	}

	public void setHasfile(boolean hasfile)
	{
		this.hasfile = hasfile;
	}

	public boolean isFeatured() {
		return featured;
	}

	public void setFeatured(boolean frontPageFlagged) {
		this.featured = frontPageFlagged;
	}

	public Date getLastReply() {
		return lastReply;
	}

	public void setLastReply(Date lastReply) {
		this.lastReply = lastReply;
	}

	public Collection<WhiteboardModel> getWhiteboards() {
		return whiteboards;
	}

	public void setWhiteboards(Collection<WhiteboardModel> whiteboards) {
		this.whiteboards = whiteboards;
	}

	public Collection<SearchableModel> getSearchables() {
		return searchables;
	}

	public void setSearchables(Collection<SearchableModel> searchables) {
		this.searchables = searchables;
	}

}
