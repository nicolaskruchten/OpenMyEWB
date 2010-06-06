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

package ca.myewb.beans;

import java.util.Date;

public abstract class DailyStats {

	protected int id;
	protected Date day;
	protected int signups;
	protected int mailinglistsignups;
	protected int signins;
	protected int posts;
	protected int events;
	protected int eventMailings;
	protected int replies;
	protected int whiteboardEdits;
	protected int regupgrades;
	protected int regdowngrades;
	protected int deletions;
	protected int renewals;
	protected int mailinglistupgrades;
	protected int filesadded;
	

	public int getFilesadded() {
		return filesadded;
	}

	public void setFilesadded(int fielsadded) {
		this.filesadded = fielsadded;
	}

	public DailyStats()
	{
		id = 0;
		day = null;
		signups = 0;
		mailinglistsignups = 0;
		signins = 0;
		posts = 0;
		replies = 0;
		regdowngrades = 0;
		regupgrades = 0;
		deletions = 0;
		renewals = 0;
		mailinglistupgrades = 0;
		filesadded = 0;
	}
	
	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMailinglistsignups() {
		return mailinglistsignups;
	}

	public void setMailinglistsignups(int mailinglistsignups) {
		this.mailinglistsignups = mailinglistsignups;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getRegdowngrades() {
		return regdowngrades;
	}

	public void setRegdowngrades(int regdowngrades) {
		this.regdowngrades = regdowngrades;
	}

	public int getRegupgrades() {
		return regupgrades;
	}

	public void setRegupgrades(int regupgrades) {
		this.regupgrades = regupgrades;
	}

	public int getReplies() {
		return replies;
	}

	public void setReplies(int replies) {
		this.replies = replies;
	}

	public int getSignins() {
		return signins;
	}

	public void setSignins(int signins) {
		this.signins = signins;
	}

	public int getSignups() {
		return signups;
	}

	public void setSignups(int signups) {
		this.signups = signups;
	}

	public int getDeletions() {
		return deletions;
	}

	public void setDeletions(int deletions) {
		this.deletions = deletions;
	}

	public int getRenewals() {
		return renewals;
	}

	public void setRenewals(int renewals) {
		this.renewals = renewals;
	}

	public int getMailinglistupgrades() {
		return mailinglistupgrades;
	}

	public void setMailinglistupgrades(int mailinglistupgrades) {
		this.mailinglistupgrades = mailinglistupgrades;
	}

	public int getEventMailings() {
		return eventMailings;
	}

	public void setEventMailings(int eventMailings) {
		this.eventMailings = eventMailings;
	}

	public int getEvents() {
		return events;
	}

	public void setEvents(int events) {
		this.events = events;
	}

	public int getWhiteboardEdits() {
		return whiteboardEdits;
	}

	public void setWhiteboardEdits(int whiteboardEdits) {
		this.whiteboardEdits = whiteboardEdits;
	}


}
