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
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.PageModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.RoleModel;
import ca.myewb.model.WhiteboardModel;

public class Group {

	protected String description;
	protected Collection<RoleModel> roles;
	protected Set<PageModel> pages;
	protected Set<PageModel> invisiblePages;
	protected Collection<PostModel> posts;
	protected Collection<EventModel> events;
	protected Collection<WhiteboardModel> whiteboards;
	protected Collection<WhiteboardModel> selfWhiteboards;
	protected String postName;
	protected boolean isPublic;
	protected boolean admin;
	protected boolean visible;
	protected boolean chapter;
	protected boolean execList;
	protected GroupChapterModel parent;
	protected Logger log;
	protected Session session;
	private String message;
	protected String shortname;
	protected int id;
	protected String name;
	protected char nationalRepType;
	protected String welcomeMessage;

	public Group() throws Exception
	{
		id = 0;
		name = "";
		description = "";
		roles = new HashSet<RoleModel>();
		pages = new HashSet<PageModel>();
		invisiblePages = new HashSet<PageModel>();
		posts = new HashSet<PostModel>();
		events = new HashSet<EventModel>();
		whiteboards = new HashSet<WhiteboardModel>();
		selfWhiteboards = new HashSet<WhiteboardModel>();
		isPublic = true;
		admin = false;
		visible = true;
		chapter = false;
		execList = false;
		nationalRepType = 0;
		welcomeMessage = null;

		log = Logger.getLogger(this.getClass());

		try
		{
			session = HibernateUtil.currentSession();
		}
		catch (Exception e)
		{
			log.fatal("Problem getting session for User object: " + e, e);
			throw e;
		}
	}

	public char getNationalRepType() {
		return nationalRepType;
	}

	public void setNationalRepType(char nationalRepType) {
		this.nationalRepType = nationalRepType;
	}

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String d) {
		description = d;
	}

	public GroupChapterModel getParent() {
		return parent;
	}

	public void setParent(GroupChapter parent) {
		this.parent = (GroupChapterModel)parent;
	}

	public Collection getRoles() {
		return roles;
	}

	private void setRoles(Collection<RoleModel> s) {
		roles = s;
	}

	public Set getPages() {
		return pages;
	}

	private void setPages(Set<PageModel> p) {
		pages = p;
	}

	public Set getInvisiblePages() {
		return invisiblePages;
	}

	private void setInvisiblePages(Set<PageModel> p) {
		invisiblePages = p;
	}

	public Collection getPosts() {
		return posts;
	}

	private void setPosts(Collection<PostModel> p) {
		posts = p;
	}

	public String getPostName() {
		return postName;
	}

	public void setPostName(String postName) {
		this.postName = postName;
	}

	public boolean getPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean getAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean getVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	protected Collection<EventModel> getEvents()
	{
		return events;
	}

	protected void setEvents(Collection<EventModel> events)
	{
		this.events = events;
	}

	
	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String groupWelcomeMessage) {
		this.welcomeMessage = groupWelcomeMessage;
	}

	public Collection<WhiteboardModel> getWhiteboards() {
		return whiteboards;
	}

	public void setWhiteboards(Collection<WhiteboardModel> whiteboards) {
		this.whiteboards = whiteboards;
	}

	public boolean isChapter()
	{
		return chapter;
	}

	public void setChapter(boolean chapter)
	{
		this.chapter = chapter;
	}

	public boolean isExecList()
	{
		return execList;
	}

	public void setExecList(boolean exec)
	{
		this.execList = exec;
	}

	public Collection<WhiteboardModel> getSelfWhiteboards() {
		return selfWhiteboards;
	}

	public void setSelfWhiteboards(Collection<WhiteboardModel> selfWhiteboards) {
		this.selfWhiteboards = selfWhiteboards;
	}

}
