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

package ca.myewb.controllers.volunteering;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.forms.PlacementEditForm;
import ca.myewb.model.PlacementModel;


public class EditPlacement extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		PlacementEditForm f = (PlacementEditForm)checkForValidationFailure(ctx);
		urlParams.processParams(new String[]{"id"}, new String[]{"new"});
		
		if(f == null)
		{
			f = new PlacementEditForm(path + "/actions/SavePlacement/" + urlParams.get("id"), requestParams);

			if(!urlParams.get("id").equals("new"))
			{
				PlacementModel thePlacement = (PlacementModel)getAndCheckFromUrl(PlacementModel.class, "id");
				f.setValue("Name", thePlacement.getName());
				f.setValue("Startdate", thePlacement.getStartdate());
				f.setValue("Enddate", thePlacement.getEnddate());
				f.setValue("Accountingid", thePlacement.getAccountingid());
				f.setValue("Country", thePlacement.getCountry());
				f.setValue("Town", thePlacement.getTown());
				f.setValue("Description", thePlacement.getDescription());
				if(thePlacement.isLongterm())
				{
					f.setValue("Longterm", "on");
				}
			}
		}
		
		ctx.put("form", f);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();

		s.add("Admin");

		return s;
	}
	
	public int weight()
	{
		return -15;
	}

	public String displayName()
	{
		return "Edit Placement";
	}
}
