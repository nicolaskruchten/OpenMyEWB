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

import ca.myewb.logic.GroupLogic;
import ca.myewb.model.UserModel;

public class Role {

	protected int id;
	protected UserModel user;
	protected GroupLogic group;
	protected Date start;
	protected Date end;
	protected String title;
	protected char level;
	
	protected Role()
	{
		id = 0;
	}

	public Role(char level) throws Exception
	{
		this();

		start = new Date();
		this.level = level;
		title = "";
	}

	public Role(char level, Date start) throws Exception
	{
		this(level);
		this.start = start;
	}

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date s) {
		start = s;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date e) {
		end = e;
	}

	public GroupLogic getGroup() {
		return group;
	}

	public void setGroup(Group g) {
		group = (GroupLogic)g;
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(User u) {
		user = (UserModel)u;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private char getLevel() {
		return level;
	}

	private void setLevel(char level) {
		this.level = level;
	}

}
