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

import java.util.Date;

import ca.myewb.beans.Role;

public abstract class RoleLogic extends Role {

	public RoleLogic(char lvl, Date start) throws Exception {
		super(lvl, start);
	}

	public RoleLogic() {
		super();
	}

	public void end(Date backDate) {
		end = backDate;
	}

	public boolean isLeader() {
		return (level == 'l');
	}

	public boolean isSender() {
		return (level == 's');
	}

	public boolean isMember() {
		return (level == 'm');
	}

	public boolean isRecipient() {
		return (level == 'r');
	}

	public String getFormattedLevel() {
		if (level == 'l')
		{
			return "leader";
		}
		else if (level == 's')
		{
			return "sender";
		}
		else if (level == 'm')
		{
			return "member";
		}
		else if (level == 'r')
		{
			return "recipient";
		}
		else
		{
			return "error";
		}
	}

	public boolean equals(RoleLogic r) {
		return ((user == r.getUser()) && (group == r.getGroup())
		       && (start.equals(r.getStart())) && (end.equals(r.getEnd())));
	}

}
