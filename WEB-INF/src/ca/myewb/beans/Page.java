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

import ca.myewb.model.GroupModel;

public abstract class Page {

	protected int id;
	protected String name;
	protected String oldName;
	private String displayName;
	protected Set<GroupModel> groups;
	protected Set<GroupModel> invisibleGroups;
	protected String area;
	private int weight;
	
	public Page()
	{
		id = 0;
		name = "";
		groups = new HashSet<GroupModel>();
		invisibleGroups = new HashSet<GroupModel>();
	}

	protected int getId() {
		return id;
	}

	private void setId(int i) {
		id = i;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Set getGroups() {
		return groups;
	}

	public void setGroups(Set<GroupModel> g) {
		groups = g;
	}

	public Set getInvisibleGroups() {
		return invisibleGroups;
	}

	public void setInvisibleGroups(Set<GroupModel> g) {
		invisibleGroups = g;
	}

	public String getOldName()
	{
		return oldName;
	}

	public void setOldName(String oldName)
	{
		this.oldName = oldName;
	}

}
