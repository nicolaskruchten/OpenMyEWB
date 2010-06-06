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

package ca.myewb.controllers.actions.users;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.EmailModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.UserModel;

public class UpgradeUser extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		urlParams.processParams(new String[] {"targetlevel", "userid"},
				new String[] {null, "-1"});

		String returnPath = path + "/chapter/MemberInfo";

		if (urlParams.get("targetlevel") == null)
		{
			throw getSecurityException("Server error on Upgrade User",
					returnPath);
		}

		UserModel targetUser = (UserModel)getAndCheckFromUrl(UserModel.class,
				"userid");
		returnPath += "/" + targetUser.getId();

		if (!Permissions.canUpdateUserStatus(currentUser, targetUser))
		{
			throw getSecurityException(
					"You don't have the right permissions to do this!",
					returnPath);
		}

		boolean isAdmin = currentUser.isAdmin();
		GroupChapterModel chapter = targetUser.getChapter();

		// Regular membership
		if (urlParams.get("targetlevel").equals("regular"))
		{
			// Add to reg group & remove from associate group
			// note for renewals, the addGroup and remGroup do nothing -
			// the checks are done in addGroup() and remGroup()
			// Add a year to the expiry
			// today

			if (targetUser.getExpiry() == null)
			{
				log.debug("Upgrading " + targetUser.getUsername()
						+ " to regular member");

				targetUser.renew(currentUser, false);

			} else
			{
				log.debug("Renewing " + targetUser.getUsername()
						+ "'s membership");

				if (targetUser.canRenew())
				{
					targetUser.renew(currentUser, false);

					// And send the renewal email
					VelocityContext mailCtx = new VelocityContext();
					mailCtx.put("name", targetUser.getFirstname() + " "
							+ targetUser.getLastname());
					mailCtx.put("helpers", new Helpers());

					Template template = Velocity.getTemplate("emails/renewal.vm");
					StringWriter writer = new StringWriter();
					template.merge(mailCtx, writer);

					EmailModel.sendEmail(targetUser.getEmail(), writer.toString());
				} 
				else
				{
					setSessionErrorMessage(("Already renewed!"));
					log.debug("not renewed, it's too soon!");
					throw new RedirectionException(returnPath);
				}

			}

			if ((targetUser.getAddress() == null)
					|| targetUser.getAddress().equals("")
					|| (targetUser.getPhone() == null)
					|| targetUser.getPhone().equals("")
					|| ((targetUser.getStudent() != 'y') && (targetUser
							.getStudent() != 'n')))
			{
				// send need info email
				VelocityContext mailCtx = new VelocityContext();
				mailCtx.put("name", targetUser.getFirstname() + " "
						+ targetUser.getLastname());
				mailCtx.put("helpers", new Helpers());

				Template template = Velocity.getTemplate("emails/upgrademoreinfo.vm");
				StringWriter writer = new StringWriter();
				template.merge(mailCtx, writer);

				EmailModel.sendEmail(targetUser.getEmail(), writer.toString());
			} else
			{
				// send thanks email
				VelocityContext mailCtx = new VelocityContext();
				mailCtx.put("name", targetUser.getFirstname() + " "
						+ targetUser.getLastname());
				mailCtx.put("helpers", new Helpers());

				Template template = Velocity.getTemplate("emails/upgrade.vm");
				StringWriter writer = new StringWriter();
				template.merge(mailCtx, writer);

				EmailModel.sendEmail(targetUser.getEmail(), writer.toString());
			}
		}

		// Upgrade to NatlRep position
		else if (urlParams.get("targetlevel").equals("natlRep"))
		{
			// Make sure target user is in a chapter
			if (chapter == null)
			{
				throw getSecurityException("User is not in a chapter!",
						returnPath);
			}
			
			// Make sure target user is in a chapter
			if (targetUser.isMember(Helpers.getGroup("Exec")))
			{
				throw getSecurityException("Cannot upgrade an Exec member to National Rep!",
						returnPath);
			}

			targetUser.upgradeToNatlRep();

			throw new RedirectionException(path + "/chapter/ExecTitle/"
					+ targetUser.getId());
		}

		// Upgrade to exec position
		else if (urlParams.get("targetlevel").equals("exec"))
		{
			// Make sure target user is in a chapter
			if (chapter == null)
			{
				throw getSecurityException("User is not in a chapter!",
						returnPath);
			}

			targetUser.upgradeToExec();

			throw new RedirectionException(path + "/chapter/ExecTitle/"
					+ targetUser.getId());
		}

		// Upgrade to NMT! oooo...
		else if (urlParams.get("targetlevel").equals("NMT") && isAdmin)
		{
			targetUser.upgradeToNMT();

			throw new RedirectionException(path + "/chapter/NMTTitle/"
					+ targetUser.getId());
		}

		// Upgrade to admin! aahhh...
		else if (urlParams.get("targetlevel").equals("admin") && isAdmin)
		{
			targetUser.upgradeToAdmin();
		}

		// Now this is an error =p
		else
		{
			throw getSecurityException("Server error... unrecognised request",
					returnPath);
		}

		setSessionMessage(("User successfully upgraded."));
		throw new RedirectionException(path + "/chapter/MemberInfo/"
				+ targetUser.getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
	
	public String oldName()
	{
		return "UpgradeUser";
	}
}
