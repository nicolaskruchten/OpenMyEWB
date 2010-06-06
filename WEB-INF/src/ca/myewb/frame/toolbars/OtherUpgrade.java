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

package ca.myewb.frame.toolbars;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Permissions;
import ca.myewb.model.UserModel;

public class OtherUpgrade extends Toolbar {
	private UserModel targetUser;

	private UserModel currentUser;

	public OtherUpgrade(UserModel currentUser, UserModel theUser)
			throws Exception {
		super();
		this.title = "Actions";
		this.targetUser = theUser;
		this.currentUser = currentUser;
		this.template = "frame/toolbars/otherupgrade.vm";
	}

	public void setUpCtx(Context ctx) throws Exception {
		if (!targetUser.isMember(Helpers.getGroup("Deleted"), false)) {
			String path = Helpers.getAppPrefix();

			if (!targetUser.isMember(Helpers.getGroup("Regular"), false)) {
				if (targetUser.isMember(Helpers.getGroup("Associate"))) {
					// Can be upgraded to regular member
					ctx.put("toRegular", path + "/actions/UpgradeUser/regular");
				}
			} else if (targetUser.canRenew()) {
				// Renew membership
				ctx.put("renewRegular", path + "/actions/UpgradeUser/regular");
			}

			// Can only go to/from Exec if they're in a chapter
			if (targetUser.getChapter() != null) {
				if ((!targetUser.isMember(Helpers.getGroup("Exec"), false))) {
					if (targetUser.isMember(Helpers.getGroup("NatlRep"))) {
						// Can be downgraded from national rep
						ctx.put("fromNatlRep", path + "/actions/DowngradeUser/natlRep");
					} else if ((targetUser.isMember(Helpers.getGroup("Regular")) || targetUser
							.isMember(Helpers.getGroup("Associate")))) {
						// Can be upgraded to national rep
						ctx.put("toNatlRep", path
								+ "/actions/UpgradeUser/natlRep");

						// Can be upgraded to exec (will be set as exec of the
						// member's current chapter)
						ctx.put("toExec", path + "/actions/UpgradeUser/exec");
					}
				} else {
					// Is exec, so can be downgraded
					ctx.put("fromExec", path + "/actions/DowngradeUser/exec");
				}
			}


			if (currentUser.isAdmin()) 
			{
				if (targetUser.isMember("NMT", false)) 
				{
					ctx.put("fromNMT", path + "/actions/DowngradeUser/NMT");
				}
				else if (!targetUser.isMember("Admin", false))
				{
					if ((targetUser.isMember("Regular") 
							|| targetUser.isMember("Associate"))) 
					{
						ctx.put("toNMT", path + "/actions/UpgradeUser/NMT");
					}
				} 
				
				if (targetUser.isMember("Admin", false)) 
				{
					ctx.put("fromAdmin", path + "/actions/DowngradeUser/admin");
				}
				else if (!targetUser.isMember("NMT", false))
				{
					if ((targetUser.isMember("Regular") 
							|| targetUser.isMember("Associate"))) 
					{
						ctx.put("toAdmin", path + "/actions/UpgradeUser/admin");
					}
				} 

				ctx.put("adminlevel", "yes");
			}

			if (Permissions.canManageSubdomainEmails(currentUser, targetUser)) {
				ctx.put("canForward", true);
			}
		}
	}
}
