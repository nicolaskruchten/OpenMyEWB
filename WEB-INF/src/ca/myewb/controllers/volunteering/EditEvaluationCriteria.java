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
import ca.myewb.frame.forms.EvaluationCriteriaEditForm;
import ca.myewb.model.EvaluationCriteriaModel;


public class EditEvaluationCriteria extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		EvaluationCriteriaEditForm f = (EvaluationCriteriaEditForm)checkForValidationFailure(ctx);
		urlParams.processParams(new String[]{"sessionid", "criteriaid"}, new String[]{"-1", "new"});
		
		if(f == null)
		{
			f = new EvaluationCriteriaEditForm(path + "/actions/SaveEvaluationCriteria/" + urlParams.get("sessionid") + "/" + urlParams.get("criteriaid"), requestParams);

			if(!urlParams.get("criteriaid").equals("new"))
			{
				EvaluationCriteriaModel theCriteria = (EvaluationCriteriaModel)getAndCheckFromUrl(EvaluationCriteriaModel.class, "criteriaid");
				f.setValue("Criteria", theCriteria.getCriteria());
				f.setValue("TinyText", theCriteria.getColheader());
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
		return "Edit Evaluation Criteria";
	}
}
