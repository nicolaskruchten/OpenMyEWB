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
import ca.myewb.frame.forms.PlacementEditForm;
import ca.myewb.model.PlacementModel;


public class SavePlacement extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		PlacementEditForm form = new PlacementEditForm(path
		                                           + "/actions/SavePlacement/"
		                                           + urlParams.getParam(),
		                                           requestParams);

		Message m = form.validate();

		// No messages: changes are valid
		if (m != null)
		{
				throw getValidationException(form, m,
				                             path + "/volunteering/EditPlacement/"
				                             + urlParams.getParam());
		}

		PlacementModel placement;
		int placementID;

		if (urlParams.getParam().equals("new"))
		{
			placement = PlacementModel.newPlacementModel();
			placementID = placement.getId();
		}
		else
		{
			placement = (PlacementModel)getAndCheckFromUrl(PlacementModel.class);
			placementID = placement.getId();
		}


		String name = form.getParameter("Name");
		String accountingID = form.getParameter("Accountingid");
		String startDate = form.getParameter("Startdate");
		String endDate = form.getParameter("Enddate");
		String country = form.getParameter("Country");
		String town = form.getParameter("Town");
		String desc = form.getParameter("Description");
		boolean longterm = form.getParameter("Longterm").equals("on");
		
		placement.save(name, accountingID, startDate, endDate, country, town, desc, longterm);

		// Leave a message in the session
		setSessionMessage(("Placement Info Updated"));

		// Redirect to somewhere
		throw new RedirectionException(path + "/volunteering/PlacementInfo/" + placementID);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "SavePlacement";
	}
}
