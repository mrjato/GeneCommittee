/*
	This file is part of GeneCommittee.

	GeneCommittee is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	GeneCommittee is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with GeneCommittee.  If not, see <http://www.gnu.org/licenses/>.
*/
package es.uvigo.ei.sing.gc.execution;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

public class UserGlobalEventListener implements EventListener<Event> {
	public static final String KEY_TASK = "task";
	public static final String KEY_DATA = "data";
	public static final String KEY_ACTION = "action";
	public static final String KEY_EVENT = "event";
	
	private final String userId;
	
	public UserGlobalEventListener(String userId) {
		super();
		this.userId = userId;
	}
	
	public String getUserId() {
		return userId;
	}

	@Override
	public void onEvent(Event event) throws Exception {
		final String eventName = event.getName();
		
		final Map<String, Object> params = new HashMap<String, Object>();
		
		if (event.getData() instanceof EventTaskData) {
			final EventTaskData etData = (EventTaskData) event.getData();
			
			if (etData.getData() != null) {
				params.put(UserGlobalEventListener.KEY_DATA, etData.getData());
			}
			params.put(UserGlobalEventListener.KEY_TASK, etData.getTask());
			
			if (eventName.contains("#")) {
				final String[] eventNameSplit = eventName.split("#");
				params.put(UserGlobalEventListener.KEY_EVENT, eventNameSplit[0]);
				params.put(UserGlobalEventListener.KEY_ACTION, eventNameSplit[1]);
			} else {
				params.put(UserGlobalEventListener.KEY_EVENT, eventName);
			}
		}
		
		for (String globalCommand : GlobalEvents.getEventGlobalCommands(eventName)) {
			BindUtils.postGlobalCommand(null, null, globalCommand, params);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UserGlobalEventListener)) {
			return false;
		}
		UserGlobalEventListener other = (UserGlobalEventListener) obj;
		if (userId == null) {
			if (other.userId != null) {
				return false;
			}
		} else if (!userId.equals(other.userId)) {
			return false;
		}
		return true;
	}
}
