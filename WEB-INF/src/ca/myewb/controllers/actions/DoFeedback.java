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

package ca.myewb.controllers.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.mail.internet.AddressException;

import org.apache.velocity.context.Context;

import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.FeedbackForm;
import ca.myewb.model.EmailModel;


public class DoFeedback extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		FeedbackForm f = new FeedbackForm(path + "/actions/DoFeedback",
		                                  requestParams);

		String body = Helpers.getLongName() + " feedback\n\n\n" + f.getParameter("Feedback");
		Vector<String> recipients = new Vector<String>();
		recipients.add(Helpers.getSystemEmail());
		
        try
        {
			if((f.getParameter("Email") != null) && (!f.getParameter("Email").equals("")))
			{
				EmailModel.sendEmail(f.getParameter("Email"), recipients, body);
			}
			else
			{
				EmailModel.sendEmail(Helpers.getSystemEmail(), recipients, body);
			}
        }
        catch (AddressException ex)
        {
        	// Recipient mis-entered his/her email...
        	ErrorMessage m = new ErrorMessage("Please enter a valid email address.");
        	throw getValidationException(f, m, path + "/home/Feedback");
        }

        setSessionMessage("Thanks for taking the time to give us some feedback!");

		throw new RedirectionException(path + "/home/Home");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
}
