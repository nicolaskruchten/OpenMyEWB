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

import java.util.Vector;

import ca.myewb.frame.PostParamWrapper;


public class ChapterEditForm extends Form
{
	private boolean isAdmin = false;

	public ChapterEditForm(String target, PostParamWrapper requestParams,
	                       boolean isAdmin)
	{
		super(target);
		this.isAdmin = isAdmin;

		if (isAdmin)
		{
			addText("Name", "Name", requestParams.get("Name"), true);
			addText("Shortname", "Short name", requestParams.get("Shortname"),
			        true);
		}

		addText("Email", "Email Address", requestParams.get("Email"), true);
		addText("Url", "Website URL", requestParams.get("Url"), true);
		
		addAddress("Address", "Mailing Address",
		           requestParams.getArray("Address"), false);

		addPhone("Phone", "Phone Number", requestParams.getArray("Phone"), false);
		addPhone("Fax", "Fax Number", requestParams.getArray("Fax"), false);
		
		if (isAdmin)
		{
			addCheckbox("Francophone", "Francophone?", requestParams.get("Francophone"), "(check if yes)");
			addCheckbox("Professional", "Professional?", requestParams.get("Professional"), "(check if yes)");
		}
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		if (isAdmin)
		{
			Vector<Character> allowed = new Vector<Character>();
			allowed.add(new Character(' '));

			isClean = (getElement("Name")).ensureAlphabetic(allowed, true)
			          && isClean;
			isClean = (getElement("Shortname")).ensureAlphabetic(new Vector<Character>(), false)
			          && isClean;

			isClean = (getElement("Email").ensureEmail()) && isClean;
			isClean = (getElement("Url").ensureUrl()) && isClean;
		}

		return isClean;
	}
}
