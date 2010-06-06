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


import ca.myewb.beans.DailyStats;

public abstract class DailySatsLogic extends DailyStats {
	
	public DailySatsLogic()
	{
		super();
	}
	
	public void logEventCreation()
	{
		setEvents(getEvents() + 1);
	}
	
	public void logWhiteboardEdit()
	{
		setWhiteboardEdits(getWhiteboardEdits() + 1);
	}
	
	public void logEventMailing()
	{
		setEventMailings(getEventMailings() + 1);
	}
	
	public void logSignup() {
		signups++;
	}

	public void logMailinglistsignup() {
		mailinglistsignups++;
	}

	public void logSignin() {
		signins++;
	}

	public void logPost() {
		posts++;
	}

	public void logReply() {
		replies++;
	}

	public void logRegupgrade() {
		regupgrades++;
	}

	public void logRegdowngrade() {
		regdowngrades++;
	}

	public void logDeletion() {
		deletions++;
	}

	public void logRenewal() {
		renewals++;
	}

	public void logMailinglistupgrade() {
		mailinglistupgrades++;
	}
	
	public void logFilesAdded()
	{
		filesadded++;
	}
}
