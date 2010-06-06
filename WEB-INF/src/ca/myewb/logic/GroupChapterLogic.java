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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.myewb.beans.GroupChapter;
import ca.myewb.model.GroupModel;

public abstract class GroupChapterLogic extends GroupChapter {

	public GroupChapterLogic() throws Exception {
		super();
	}

	public GroupChapterLogic(GroupLogic exec) throws Exception {
		super(exec);
	}

	public String postName() {
		return (getName());
	}
	public String getAddress() {
		
		if(address1 == null)
			return null;
		
		return address1 + "\n" + suite + "\n" + address2 + "\n" 
		+ city + "\n" + province + "\n" + postalcode + "\n" + country;
	}

	public void setAddress(String address) {
		
		
		if (address == null)
		{
			setAddress1(null);
			setSuite(null);
			setAddress2(null);
			setCity(null);
			setProvince(null);
			setPostalcode(null);
			setCountry(null);
		}
		else
		{
			String[] splitAddress = address.split("\n");
			setAddress1(	(splitAddress.length > 0 && splitAddress[0] != null) ? splitAddress[0] : "");
			setSuite(		(splitAddress.length > 1 && splitAddress[1] != null) ? splitAddress[1] : "");
			setAddress2(	(splitAddress.length > 2 && splitAddress[2] != null) ? splitAddress[2] : "");
			setCity(		(splitAddress.length > 3 && splitAddress[3] != null) ? splitAddress[3] : "");
			setProvince(	(splitAddress.length > 4 && splitAddress[4] != null) ? splitAddress[4] : "");
			setPostalcode(	(splitAddress.length > 5 && splitAddress[5] != null) ? splitAddress[5] : "");
			setCountry(		(splitAddress.length > 6 && splitAddress[6] != null) ? splitAddress[6] : "");
		}
		
	}

	public Set<GroupModel> getVisibleChildren() {
		HashSet<GroupModel> hs = new HashSet<GroupModel>();
		Iterator it = children.iterator();
	
		while (it.hasNext())
		{
			GroupLogic gp = (GroupLogic)it.next();
	
			if (gp.getVisible())
			{
				hs.add((GroupModel)gp);
			}
		}
	
		return hs;
	}

	public boolean equals(GroupChapterLogic g) {
		return (super.equals(g));
	}

	public void addChild(GroupLogic child) {
		child.setParent(this);
		children.add((GroupModel)child);
	}

}
