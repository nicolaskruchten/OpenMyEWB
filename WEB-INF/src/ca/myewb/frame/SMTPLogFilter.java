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

package ca.myewb.frame;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;


public class SMTPLogFilter extends Filter
{
	public int decide(LoggingEvent arg0)
	{
		String[] denyStrings = {"ClientAbortException", "writing PNG file", "Session already invalidated"};

		ThrowableInformation ti = arg0.getThrowableInformation();

		for (String denyString : denyStrings)
		{
			try
			{
				if ((arg0.getMessage() != null) && ((String)arg0.getMessage()).contains(denyString))
				{
					return DENY;
				}
			}
			catch(Exception e)
			{
				;
			}

			if (ti != null)
			{
				Throwable t = ti.getThrowable();

				if ((t != null) && t.toString().contains(denyString))
				{
					return DENY;
				}
			}
		}

		return NEUTRAL;
	}
}
