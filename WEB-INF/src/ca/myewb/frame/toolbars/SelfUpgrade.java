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

import org.apache.log4j.Logger;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class SelfUpgrade extends Toolbar
{
	private UserModel currentUser;

	public SelfUpgrade(UserModel theUser) throws Exception
	{
		super();
		this.title = "Actions";
		this.currentUser = theUser;
		this.template = "frame/toolbars/selfupgrade.vm";
	}

	public void setUpCtx(Context ctx) throws Exception
	{
		ctx.put("mode", getMode());

		if (currentUser.isMember(Helpers.getGroup("Exec")))
		{
			ctx.put("isExec", "yes");
		}
		else
		{
			ctx.put("isExec", "no");
		}
	}

	public String getMode() throws Exception
	{
		if (currentUser.isAdmin())
		{
			return "";
		}

		GroupModel reg = Helpers.getGroup("Regular");
		Logger log = Logger.getLogger(this.getClass());

		if (!currentUser.isMember(reg))
		{
			log.debug("Can pay and upgrade to regular");

			return "makeRegular";
		}
		else
		{
			if (currentUser.canRenew())
			{
				log.debug("Can renew membership");

				return "renewRegular";
			}
			else
			{
				return "";
			}
		}
	}
}
