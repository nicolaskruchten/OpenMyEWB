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

package ca.myewb.controllers.common;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;
import org.hibernate.Session;

import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.UserModel;


public class GenericConfirm extends Controller
{
	public GenericConfirm(HttpSession httpSession, Session hibernate,
	                      PostParamWrapper requestParams,
	                      GetParamWrapper urlParams, UserModel currentUser)
	{
		super();
		this.httpSession = httpSession;
		this.hibernateSession = hibernate;
		this.requestParams = requestParams;
		this.currentUser = currentUser;
		this.urlParams = urlParams;
	}

	public void setUpContext(Context ctx) throws RedirectionException
	{
		Object dateStamp = getInterpageVar("datestamp");
		if(dateStamp == null)
		{
			setSessionErrorMessage("We're sorry, but the previous request caused a server error and could not be completed. The system administrators have been automatically notified.");
			throw new RedirectionException(Helpers.getDefaultURL());
		}
		String dateString = dateStamp.toString();
		ctx.put("confirmForm", getStoredForm(dateString));//getInterpageVar("storedConfirmForm"));
		ctx.put("datestamp", dateStamp);
		ctx.put("bigMessage", getInterpageVar("bigMessage"));
		ctx.put("littleMessage", getInterpageVar("littleMessage"));
		ctx.put("confirmURL", getInterpageVar("confirmURL"));
	}

	public void handle(Context ctx) throws Exception
	{
		// You should never come here directly!
		throw getSecurityException("Someone accessed common/GenericConfirm directly!",
		                           path + "/home/Home");
	}

	public static List<String> getCommonNeededInterpageVars()
	{
		Vector<String> vars = new Vector<String>();
		vars.add("bigMessage");
		vars.add("littleMessage");
		vars.add("cancelURL");
		vars.add("confirmURL");
		vars.add("datestamp");

		return vars;
	}
}
