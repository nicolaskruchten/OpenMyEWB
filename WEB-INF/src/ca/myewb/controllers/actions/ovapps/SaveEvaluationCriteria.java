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
import ca.myewb.frame.forms.EvaluationCriteriaEditForm;
import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.EvaluationCriteriaModel;


public class SaveEvaluationCriteria extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		urlParams.processParams(new String[]{"sessionid", "criteriaid"}, new String[]{"-1", "new"});
		EvaluationCriteriaEditForm form = new EvaluationCriteriaEditForm(path
		                                           + "/actions/SaveEvaluationCriteria/"
		                                           + urlParams.get("sessionid") + "/" + urlParams.get("criteriaid"),
		                                           requestParams);

		Message m = form.validate();

		// No messages: changes are valid
		if (m != null)
		{
				throw getValidationException(form, m,
				                             path + "/volunteering/EditEvaluationCriteria/"
				                             + urlParams.get("sessionid") + "/" + urlParams.get("criteriaid"));
		}

		ApplicationSessionModel session = (ApplicationSessionModel)getAndCheckFromUrl(ApplicationSessionModel.class, "sessionid");
		EvaluationCriteriaModel criteria;

		String criteriaText = form.getParameter("Criteria");
		String tinyText = form.getParameter("TinyText");

		if (urlParams.get("criteriaid").equals("new"))
		{
			criteria = session.addCriteria(criteriaText, tinyText);
		}
		else
		{
			criteria = (EvaluationCriteriaModel)getAndCheckFromUrl(EvaluationCriteriaModel.class, "criteriaid");
			criteria.save(criteriaText, tinyText);
		}
		
		// Leave a message in the session
		setSessionMessage(("Evaluation Criteria Added"));

		// Redirect to somewhere
		throw new RedirectionException(path + "/volunteering/EvaluationCriteriaList/" + session.getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "SaveEvaluationCriteria";
	}
}
