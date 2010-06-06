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
import java.util.Vector;

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.model.GroupChapterModel;


public class MemberSearchForm extends Form
{
	boolean advanced;

	public MemberSearchForm(String target, PostParamWrapper requestParams,
	                  boolean advanced, List chapters)
	           throws Exception
	{
		super(target, "search");
		addText("Firstname", "First name", requestParams.get("Firstname"), false);
		addText("Lastname", "Last name", requestParams.get("Lastname"), false);
		addText("Email", "Email address", requestParams.get("Email"), false);

		if (chapters != null)
		{
			Dropdown d = addDropdown("Chapter", "Chapter",
			                         requestParams.get("Chapter"), false);

			Iterator i = chapters.iterator();

			while (i.hasNext())
			{
				GroupChapterModel chap = (GroupChapterModel)i.next();
				d.addOption(String.valueOf(chap.getId()), chap.getName());
			}
		}

		if (advanced)
		{
			this.advanced = true;

			addText("Username", "Username", requestParams.get("Username"), false);
			addText("City", "City", requestParams.get("City"), false);
			addText("Province", "Province", requestParams.get("Province"), false);

			Dropdown d = addDropdown("Language", "Preferred Language",
			                         requestParams.get("Language"), false);
			d.addOption("en", "English");
			d.addOption("fr", "French");

			d = addDropdown("Gender", "Gender", requestParams.get("Gender"),
			                false);
			d.addOption("m", "male");
			d.addOption("f", "female");

			addText("Birth", "Year of birth", requestParams.get("Birth"), false);
			addText("Occupation", "Occupation / field",
			        requestParams.get("Occupation"), false);

			d = addDropdown("Student", "Student status?",
			                requestParams.get("Student"), false);
			d.addOption("true", "Yes");
			d.addOption("false", "No");

			addHidden("Advanced", "yes", true);
		}
		else
		{
			this.advanced = false;
		}
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		isClean = (getElement("Firstname")).ensureName() && isClean;
		isClean = (getElement("Lastname")).ensureName() && isClean;

		if (advanced)
		{
			Vector<Character> allowed = new Vector<Character>();
			allowed.add(new Character(' '));
			isClean = (getElement("Province")).ensureAlphabetic(allowed, true)
			          && isClean;

			allowed.add(new Character('-'));
			allowed.add(new Character('.'));
			allowed.add(new Character('\''));
			isClean = (getElement("City")).ensureAlphabetic(allowed, true)
			          && isClean;

			isClean = (getElement("Birth")).ensureNumeric(1890, 2005)
			          && isClean;

			allowed.remove(new Character('-'));
			allowed.add(new Character('/'));
			isClean = (getElement("Occupation")).ensureAlphabetic(allowed, true)
			          && isClean;

			if (getElement("Chapter") != null)
			{
				isClean = (getElement("Chapter")).ensureNumeric() && isClean;
			}
		}

		return isClean;
	}
}
