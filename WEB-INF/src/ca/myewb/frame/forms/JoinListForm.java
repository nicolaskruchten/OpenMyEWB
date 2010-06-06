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

import java.util.Iterator;
import java.util.List;

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.model.GroupModel;


public class JoinListForm extends Form
{
	public JoinListForm(String target, PostParamWrapper requestParams,
	                    List lists)
	{
		super(target);

		Dropdown d = addDropdown("List", "List", requestParams.get("List"), true);
		Iterator it = lists.iterator();

		while (it.hasNext())
		{
			GroupModel g = (GroupModel)it.next();
			d.addOption(String.valueOf(g.getId()), g.getName());
		}

		addHidden("Emails", requestParams.get("Emails"), true);
		addHidden("ActionType", requestParams.get("ActionType"), true);
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return (getElement("Emails")).ensureEmail();
	}
}
