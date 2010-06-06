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
import ca.myewb.frame.forms.ApplicationQuestionForm;
import ca.myewb.model.ApplicationAnswerModel;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ApplicationQuestionModel;
import ca.myewb.model.ApplicationSessionModel;

public class AnswerApplicationQuestion extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ApplicationQuestionForm form = (ApplicationQuestionForm)checkForValidationFailure(ctx);
		urlParams.processParams(new String[]{"sessionid", "questionid"}, new String[]{"-1", "-1"});
		
		ApplicationSessionModel session = (ApplicationSessionModel)getAndCheckFromUrl(ApplicationSessionModel.class, "sessionid");
		ApplicationQuestionModel question = (ApplicationQuestionModel)getAndCheckFromUrl(ApplicationQuestionModel.class, "questionid");
		ApplicationModel app = currentUser.getAppForSession(session);
		ApplicationAnswerModel answer = app.getAnswerForQuestion(question);
		
		if (form == null)
		{
			// Load up a fresh form object
			form = new ApplicationQuestionForm(path
					+ "/actions/SaveApplicationAnswer/" + session.getId() + "/" + question.getId(), requestParams);
			if(answer != null)
			{
				form.setValue("Answer", answer.getAnswer());
			}
			else
			{
				form.setValue("Answer", "");
			}
		}

		ctx.put("form", form);
		ctx.put("question", question);
		ctx.put("numQuestions", session.getQuestions().size());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();

		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "Application Question";
	}
}