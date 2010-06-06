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
import ca.myewb.frame.forms.element.Element;


public class ListEditForm extends Form
{
	public ListEditForm(String target, PostParamWrapper requestParams)
	{
		super(target);

		addText("Name", "Name", requestParams.get("Name"), true);
		addText("Shortname", "Shortname", requestParams.get("Shortname"), true);
		addTextArea("Description", "Description",
		            requestParams.get("Description"), true);

		Element c = addCheckbox("Public", "Is this list public?",
		                        requestParams.get("Public"), "(check if yes)");
		c.setInstructions("public means anyone can join, private means only leaders can add people");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		Vector<Character> allowed = new Vector<Character>();
		isClean = (getElement("Shortname")).ensureAlphanumeric(allowed, true)
		          && isClean;
		allowed.add(new Character(' '));
		isClean = (getElement("Name")).ensureAlphanumeric(allowed, true)
		          && isClean;

		return isClean;
	}
}
