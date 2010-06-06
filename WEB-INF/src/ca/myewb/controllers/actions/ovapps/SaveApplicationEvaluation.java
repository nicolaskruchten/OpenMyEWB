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
import ca.myewb.frame.forms.ApplicationEvaluationForm;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.EvaluationCriteriaModel;


public class SaveApplicationEvaluation extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ApplicationModel app = (ApplicationModel)getAndCheckFromUrl(ApplicationModel.class);
		ApplicationEvaluationForm form = new ApplicationEvaluationForm(path
		                                           + "/actions/SaveApplicationEvaluation/"
		                                           + urlParams.getParam(),
		                                           requestParams, app.getSession().getEvalCriteria());

		Message m = form.validate();

		// No messages: changes are valid
		if (m != null)
		{
				throw getValidationException(form, m,
				                             path + "/volunteering/ApplicationInfo/"
				                             + urlParams.getParam());
		}
		
		for(EvaluationCriteriaModel crit : app.getSession().getEvalCriteria())
		{
			if(!form.getParameter("eval-" + crit.getId()).equals(""))
			{
				int res = Integer.parseInt(form.getParameter("eval-" + crit.getId()));
				app.evaluateForCriteria(crit, res);
			}
		}
		
		app.getEvaluation().setNotes(form.getParameter("Notes"));
		
		// Leave a message in the session
		setSessionMessage(("Application Evaluation Saved"));

		// Redirect to somewhere
		throw new RedirectionException(path + "/volunteering/ApplicationList/name/asc/" + app.getSession().getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "SaveApplicationEvaluation";
	}
}
