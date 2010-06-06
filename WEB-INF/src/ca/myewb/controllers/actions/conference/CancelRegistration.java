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

package ca.myewb.controllers.actions.conference;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.ConferenceRegistrationModel;
import ca.myewb.model.EmailModel;


public class CancelRegistration extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ConferenceRegistrationModel registration = currentUser.getRegistration();
		if(registration == null)
		{
			setSessionErrorMessage("You are not currently registered...");
		}
		
		String confirmText;
		if(!isOnConfirmLeg())
		{
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("reg", registration);
			confirmCtx.put("helpers", new Helpers());
			Template template = Velocity.getTemplate("confirmations/conferencecancellation.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}

		requireConfirmation("Are you sure you want to cancel your conference registration?", 
							confirmText,
		                    path + "/events/Conference",
		                    path + "/actions/conference.CancelRegistration", 
		                    "events", null);

		registration.cancel();

		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("reg", registration);
		velocityContext.put("helpers", new Helpers());
		Template template = Velocity.getTemplate("emails/confregcancellation.vm");
		StringWriter writer = new StringWriter();
		template.merge(velocityContext, writer);

		EmailModel.sendEmail(Helpers.getSystemEmail(), writer.toString());

		log.debug("Cancelled registration for: " + currentUser.getUsername());

		setSessionMessage("Registration successfully cancelled. Your credit card will be refunded shortly. You may now re-register with different options if you like.");


		throw new RedirectionException(path + "/events/Conference");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
