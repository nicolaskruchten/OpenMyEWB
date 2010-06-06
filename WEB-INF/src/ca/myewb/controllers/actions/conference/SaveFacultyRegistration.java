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

import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.CreditCardTransaction;
import ca.myewb.frame.forms.FacultyRegistrationForm;
import ca.myewb.model.FacultyRegistrationModel;
import ca.myewb.model.TicketOrderModel;
import ca.myewb.model.UserModel;


public class SaveFacultyRegistration extends Controller
{
	public void handle(Context ctx) throws Exception
	{

		// Create & validate form object
		FacultyRegistrationForm form = new FacultyRegistrationForm(path + "/actions/SaveFacultyRegistration", requestParams);

		Message m = form.validate();
		
		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m, path + "/events/FacultyDay");
		}

		String confirmText;
		if(!isOnConfirmLeg())
		{
			VelocityContext confirmCtx = new VelocityContext();		
			int numRooms = 0;

			if(form.getParameter("HotelWed").equals("on"))
			{
				numRooms ++;
			}
			if(form.getParameter("HotelThu").equals("on"))
			{
				numRooms ++;
			}
			if(form.getParameter("HotelFri").equals("on"))
			{
				numRooms ++;
			}
			if(form.getParameter("HotelSat").equals("on"))
			{
				numRooms ++;
			}
			confirmCtx.put("num", numRooms);
			confirmCtx.put("helpers", new Helpers());
			confirmCtx.put("banquet", form.getParameter("BanquetTicket").equals("on"));
			Template template = Velocity.getTemplate("confirmations/facultyregistration.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}
		
		requireConfirmation("Confirm: pay for the following?",
		                    confirmText,
		                    path + "/events/FacultyDay",
		                    path + "/actions/SaveFacultyRegistration",
		                    "events", form, false);
		
		form = new FacultyRegistrationForm(path + "/actions/SaveFacultyRegistration", requestParams);

		if(form.getParameter("SignUp").equals("on"))
		{
			if(UserModel.getUserForEmail(form.getParameter("Email")) == null)
			{
				UserModel.newMailingListSignUp(form.getParameter("Email"));
			}
		}
		
		
		//set up transaction

		CreditCardTransaction trn = new CreditCardTransaction();
		trn.setContactInfo(form.getParameter("Name"), form.getParameter("Email"), form.getParameter("Phone"),
				form.getParameter("Address"));
		
		trn.setCardInfo(form.getParameter("Name"), 
				form.getParameter("Creditcardnumber"), 
				form.getParameter("Creditcardexpirydate").substring(0, 2), 
				form.getParameter("Creditcardexpirydate").substring(2));

		trn.addItem("09-facultyreg", 1, 50, "Faculty Day registration");
		
		int numRooms = 0;

		if(form.getParameter("HotelWed").equals("on"))
		{
			numRooms ++;
		}
		if(form.getParameter("HotelThu").equals("on"))
		{
			numRooms ++;
		}
		if(form.getParameter("HotelFri").equals("on"))
		{
			numRooms ++;
		}
		if(form.getParameter("HotelSat").equals("on"))
		{
			numRooms ++;
		}
		
		if(numRooms != 0)
		{
			trn.addItem("09-facultyroom", numRooms, 150, "Faculty nights at the hotel");
		}
		
		if(form.getParameter("BanquetTicket").equals("on"))
		{
			trn.addItem("09-facultyticket", 1, 100, "Gala Banquet Ticket (Reduced Price)");
		}
		
		trn.attemptTransaction("facultyreg");
		
		//wrap up
		
		if(trn.isSucceeded())
		{
			trn.sendReceipt("Conference Registration");
			FacultyRegistrationModel reg = FacultyRegistrationModel.newFacultyReg(form.getParameter("Address"),
					form.getParameter("Affiliation"),
					form.getParameter("Email"),
					form.getParameter("Headset").equals("y"),
					form.getParameter("Name"),
					form.getParameter("Needs"),
					trn.getOrderNumber(),
					form.getParameter("Language"),
					form.getParameter("Phone"),
					form.getParameter("HotelWed").equals("on"),
					form.getParameter("HotelThu").equals("on"),
					form.getParameter("HotelFri").equals("on"),
					form.getParameter("HotelSat").equals("on"),
					form.getParameter("BanquetTicket").equals("on"));
			
			if(form.getParameter("BanquetTicket").equals("on"))
			{
				TicketOrderModel.newOrder(form.getParameter("Address"),
					form.getParameter("Email"),
					form.getParameter("Name"),
					1,
					trn.getOrderNumber(),
					form.getParameter("Language"),
					form.getParameter("Needs"),
					form.getParameter("Phone"));
			}
			
			throw new RedirectionException(path + "/events/FacultyReceipt/" + reg.getId());
		}
		else
		{
			setSessionErrorMessage("Transaction NOT successfully processed: " + trn.getOutput().get("messageText"));
			throw new RedirectionException(path + "/events/FacultyDay");
		}

	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public boolean secureAccessRequired()
	{
		return true;
	}
	
	public String oldName()
	{
		return "SaveFacultyRegistration";
	}
}
