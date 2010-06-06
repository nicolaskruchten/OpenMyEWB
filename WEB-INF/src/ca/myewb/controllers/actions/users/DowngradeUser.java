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

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.UserModel;

public class DowngradeUser extends Controller {
	public void handle(Context ctx) throws Exception {
		urlParams.processParams(new String[] { "targetlevel", "userid" },
				new String[] { null, "-1" });

		String returnPath = path + "/chapter/MemberInfo";

		if (urlParams.get("targetlevel") == null) {
			throw getSecurityException("Server error on Downgrade User",
					returnPath);
		}

		UserModel targetUser = (UserModel) getAndCheckFromUrl(UserModel.class,
				"userid");
		
		boolean exec = targetUser.isMember(Helpers.getGroup("Exec"));
		
		returnPath += "/" + targetUser.getId();

		if (!Permissions.canUpdateUserStatus(currentUser, targetUser)) {
			throw getSecurityException(
					"You don't have the right permissions to do this!",
					returnPath);
		}

		requireConfirmation(
				"Are you sure?",
				"Downgrading this user will lock them out of certain system functions. Please only do this when " + (exec ? "exec" : "rep" ) + " terms are over of if the user was mistakenly upgraded to " + (exec ? "exec" : "rep" ) + " status.",
				returnPath, path + "/actions/DowngradeUser/"
						+ urlParams.get("targetlevel") + "/"
						+ urlParams.get("userid"), "chapter", null);

		boolean isAdmin = currentUser.isAdmin();
		GroupChapterModel chapter = targetUser.getChapter();

		// Remove an exec
		if (urlParams.get("targetlevel").equals("natlRep")) {
			// Make sure target user is in a chapter
			if (chapter == null) {
				throw getSecurityException("User is not in a chapter!",
						returnPath);
			}

			targetUser.downgradeFromNatlRep();
		} else if (urlParams.get("targetlevel").equals("exec")) {
			// Make sure target user is in a chapter
			if (chapter == null) {
				throw getSecurityException("User is not in a chapter!",
						returnPath);
			}

			if (targetUser.isLastExec()) {
				throw getSecurityException(
						"Cannot downgrade the last exec of a chapter",
						returnPath);
			}

			targetUser.downgradeFromExec();
		}

		// Remove from NMT
		else if (urlParams.get("targetlevel").equals("NMT") && isAdmin) {
			targetUser.downgradeFromNMT();
		}

		// Remove from Admins
		else if (urlParams.get("targetlevel").equals("admin") && isAdmin) {
			if (targetUser.isLastAdmin()) {
				throw getSecurityException(
						"You're the last admin, you fool, and cannot downgrade yourself!",
						returnPath);
			}
			targetUser.downgradeFromAdmin();

		}

		// And this should never happen!
		else {
			throw getSecurityException("Server error... unrecognised request",
					returnPath);
		}

		setSessionMessage(("User successfully downgraded."));
		throw new RedirectionException(path + "/chapter/MemberInfo/"
				+ targetUser.getId());
	}

	public Set<String> invisibleGroups() {
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
	
	public String oldName()
	{
		return "DowngradeUser";
	}
}
