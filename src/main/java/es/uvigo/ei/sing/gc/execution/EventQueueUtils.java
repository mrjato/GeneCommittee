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

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

import es.uvigo.ei.sing.gc.model.entities.User;

public class EventQueueUtils {
	private final static Map<String, EventQueue<Event>> USER_QUEUES = 
		new HashMap<String, EventQueue<Event>>();
	
	public static String getUserQueueName(User user) {
		return user.getEmail();
	}
	
	public static EventQueue<Event> getUserQueue(String userId) {
		try {
			return EventQueues.lookup(userId, EventQueues.APPLICATION, true);
		} catch (Exception e) {
			return EventQueueUtils.USER_QUEUES.get(userId);
		}
	}
	
	public static void destroyUserQueue() {
		final Session session = Sessions.getCurrent(false);
		
		if (session != null && session.hasAttribute("user")) {
			EventQueues.remove(((User) session.getAttribute("user")).getEmail());
		}
	}
	
	public static EventQueue<Event> getUserQueue() {
		final Session session = Sessions.getCurrent(false);
		
		if (session == null || !session.hasAttribute("user")) {
			return null;
		} else {
			final User user = (User) session.getAttribute("user");
			
			final EventQueue<Event> queue = EventQueues.lookup(
				EventQueueUtils.getUserQueueName(user), 
				EventQueues.APPLICATION, true
			);
			
			EventQueueUtils.USER_QUEUES.put(user.getEmail(), queue);
			
			return queue;
		}
	}
	
	public static UserGlobalEventListener registerUserGlobalListener() 
	throws IllegalStateException {
		final Session session = Sessions.getCurrent(false);
		
		if (session == null || !session.hasAttribute("user")) {
			return null;
		} else if (session.hasAttribute("userGlobalListener")) {
			final UserGlobalEventListener listener = (UserGlobalEventListener) session.getAttribute("userGlobalListener");
			
			EventQueueUtils.addListener(listener);
			
			return listener;
		} else {
			final User user = (User) session.getAttribute("user");
			final UserGlobalEventListener listener = new UserGlobalEventListener(user.getEmail());
			session.setAttribute("userGlobalListener", listener);
			
			EventQueueUtils.addListener(listener);
			
			return listener;
		}
		
	}
	
	public static UserGlobalEventListener unregisterUserGlobalListener() 
	throws IllegalStateException {
		final Session session = Sessions.getCurrent(false);
		
		if (session == null || !session.hasAttribute("userGlobalListener")) {
			return null;
		} else {
			final UserGlobalEventListener listener = 
				(UserGlobalEventListener) session.removeAttribute("userGlobalListener");
			
			if (listener != null && session.hasAttribute("user")) {
				EventQueueUtils.removeListener(listener);
			} 
			
			return listener;
		}
	}
	
	private static void addListener(EventListener<Event> listener) {
		if (!EventQueueUtils.getUserQueue().isSubscribed(listener))
			EventQueueUtils.getUserQueue().subscribe(listener);
	}
	
	private static void removeListener(EventListener<Event> listener) {
		EventQueueUtils.getUserQueue().unsubscribe(listener);
	}
}
