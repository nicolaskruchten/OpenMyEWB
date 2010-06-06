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

package ca.myewb.controllers.actions.ovapps;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Message;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.OVAssignmentForm;
import ca.myewb.model.PlacementModel;
import ca.myewb.model.UserModel;


public class DoOvAssignment extends Controller
{
	public void handle(Context ctx) throws Exception
	{

		OVAssignmentForm f = new OVAssignmentForm(path + "/actions/DoOvAssignment/" + urlParams.getParam(),
                requestParams, PlacementModel.getUnassignedPlacements());
                
        Message m = f.validate();

		// No messages: changes are valid
		if (m != null)
		{
				throw getValidationException(f, m,
				                             path + "/volunteering/AssignOv/"
				                             + urlParams.getParam());
		};

		UserModel targetUser = (UserModel)getAndCheckFromUrl(UserModel.class);
		PlacementModel thePlacement = (PlacementModel)getAndCheck(PlacementModel.class, new Integer(f.getParameter("Placement")));

		targetUser.assignPlacement(thePlacement);

		

		setSessionMessage(("OV successfully assigned."));
		throw new RedirectionException(path + "/volunteering/PlacementInfo/" + f.getParameter("Placement"));
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "DoOvAssignment";
	}
}
