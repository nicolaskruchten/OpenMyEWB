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

package ca.myewb.logic;

import java.util.List;

import ca.myewb.beans.Email;

public abstract class EmailLogic extends Email {
	
	public EmailLogic() 
	{
	}
	
	public EmailLogic(List<String> to, String sender, String shortName, String subject, String textBody, String HTMLBody) throws Exception
	{
		super();
		progress = "waiting";

		StringBuffer recipientBuffer = new StringBuffer();

		if (to.size() > 3000)
		{
			throw new IllegalStateException("Too many members in group!");
		}

		for(String s : to)
		{
			recipientBuffer.append(s);
			recipientBuffer.append(", ");
		}
		
		recipients = recipientBuffer.toString();

		this.shortName = shortName;

		this.sender = sender;

		this.subject = subject;
		
		this.textMessage = textBody;
		this.htmlMessage = HTMLBody;
	}

}
