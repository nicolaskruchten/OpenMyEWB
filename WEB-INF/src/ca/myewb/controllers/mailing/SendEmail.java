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

package ca.myewb.controllers.mailing;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.forms.EmailEditForm;
import ca.myewb.model.GroupModel;


public class SendEmail extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupModel list = (GroupModel)getAndCheckFromUrl(GroupModel.class);

		if (!Permissions.canSendEmailToGroup(currentUser, list))
		{
			throw getSecurityException("Can't send mail to this list!",
			                           path + "/mailing/ListInfo/"
			                           + urlParams.getParam());
		}

		ctx.put("list", list);

		EmailEditForm form = (EmailEditForm)checkForValidationFailure(ctx);

		if (form == null)
		{
			// First try: create a fresh form
			form = new EmailEditForm(path + "/actions/DoSendEmail/"
			                         + list.getId(), requestParams,
			                         list, currentUser);
		}

		ctx.put("form", form);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "New Email";
	}

	public int weight()
	{
		return -1;
	}
}
