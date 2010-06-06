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

import java.util.List;
import java.util.Vector;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.model.GroupModel;


public class ExecTitleForm extends Form
{
	public ExecTitleForm(String target, PostParamWrapper requestParams, boolean showStudentLists, boolean showProLists)
	{
		super(target);

		addText("Title", "Title", requestParams.get("Title"), true).setInstructions("&nbsp;");
		if(showStudentLists || showProLists)
		{
			List<GroupModel> repLists2 = Helpers.getNationalRepLists(showStudentLists, showProLists);
			for(GroupModel grp: repLists2)
			{
				addCheckbox(grp.getShortname(), grp.getPostName().substring(4) + "?",
			            requestParams.get(grp.getShortname()), "(check if yes)");
			}
		}
		addHidden("Targetid", requestParams.get("Targetid"), true);
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		Vector<Character> allowed = new Vector<Character>();
		allowed.add(new Character(' '));
		allowed.add(new Character('-'));
		allowed.add(new Character(','));
		allowed.add(new Character('&'));
		allowed.add(new Character('.'));

		isClean = (getElement("Title")).ensureAlphabetic(allowed, true)
		          && isClean;

		return isClean;
	}
}
