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

package ca.myewb.frame.forms;

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;


public class ListMembershipForm extends Form
{
	public ListMembershipForm(String target, PostParamWrapper requestParams,
	                          boolean isLeader, boolean isNormalList)
	{
		super(target); //target URL should be ModifyListMembership and include listid as URL param

		if (isLeader)
		{
			Element e = addTextArea("Emails", "Email addresses",
			                        requestParams.get("Emails"), true);
			e.setInstructions("one email address per line, no commas or other delimiters");

			Radio l = addRadio("ActionType", "Action",
			                   requestParams.get("ActionType"), true);
			l.setNumAcross(1);

			l.addOption("add", "Add members to list");
			l.addOption("remove", "Remove members from list");
			l.addOption("upsender", "Allow members to send to list");
			l.addOption("downsender", "Remove members' sender rights");

			if (isNormalList) //list is not chapter or exec
			{
				l.addOption("upleader", "Make members leaders of the list");
				l.addOption("downleader", "Remove members' leader rights");
			}
			else
			{
				l.setInstructions("note: this is a special list, so you cannot add or remove leaders");
			}
		}
		else
		{
			Text text = addText("Emails", "Email address", requestParams.get("Emails"), true);
			text.setInstructions("&nbsp;");
			text.setSize(250);
			addHidden("ActionType", requestParams.get("ActionType"), true);
			addHidden("Redirect", requestParams.get("Redirect"), false);
		}
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return (getElement("Emails")).ensureEmailList();
	}
}
