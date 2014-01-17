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
package es.uvigo.ei.sing.gc.view;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.execution.AbortException;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.wekabridge.io.operations.LoadClassificationData;

import org.zkoss.zul.Paging;


public final class ZKUtils {
	public static final int DEFAULT_TIMEOUT = 1000;
	private ZKUtils() {}
	
	public static boolean unsafeActivate(Desktop desktop) {
		if (Events.inEventListener()) {
			return true;
		} else if (desktop == null || !desktop.isServerPushEnabled()) {
			return false;
		} else {
			try {
				Executions.activate(desktop);
				
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	public static boolean safeActivate(Desktop desktop) {
		return ZKUtils.safeActivate(desktop, ZKUtils.DEFAULT_TIMEOUT);
	}
	
	public static boolean safeActivate(Desktop desktop, long timeout) {
		if (Events.inEventListener()) {
			return true;
		} else if (desktop == null || !desktop.isServerPushEnabled()) {
			return false;
		} else {
			try {
				return Executions.activate(desktop, timeout);
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	public static boolean safeDeactivate(Desktop desktop) {
		if (Events.inEventListener()) {
			return true;
		} else if (desktop == null || !desktop.isServerPushEnabled()) {
			return false;
		} else {
			try {
				Executions.deactivate(desktop);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	public static void emptyComponent(Component component) {
		final List<Object> children = new ArrayList<Object>(component.getChildren());
		
		for (Object child : children) {
			if (!child.getClass().equals(Paging.class)) // Paging can not be removed
				component.removeChild((Component) child);
		}
	}
	
	public static void emptyComponent(Component component, Class<? extends Component> ... childClasses) {
		final List<Object> children = new ArrayList<Object>(component.getChildren());
		
		for (Object child : children) {
			for (Class<? extends Component> childClass : childClasses) {
				if (childClass.isInstance(child)) {
					component.removeChild((Component) child);
					break;
				}
			}
		}
	}

	public static <T extends Event> void schedule(T event, EventListener<T> eventListener) {
		final Desktop desktop = event.getPage().getDesktop();
		
		ZKUtils.schedule(event, desktop, eventListener);
	}
	
	public static <T extends Event> void schedule(T event, Desktop desktop, EventListener<T> eventListener) {
		Executions.schedule(desktop, eventListener, event);
	}
	
	public static <T extends Event> void scheduleIfNeeded(T event, EventListener<T> eventListener) {
		if (Events.inEventListener()) {
			try {
				eventListener.onEvent(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			ZKUtils.schedule(event, eventListener);
		}
	}
	
	public static <T extends Event> void scheduleIfNeeded(T event, Desktop desktop, EventListener<T> eventListener) {
		if (Events.inEventListener()) {
			try {
				eventListener.onEvent(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			ZKUtils.schedule(event, desktop, eventListener);
		}
	}
	
	public static class SessionObject<T> {
		private final Session session;
		private final T object;
		private final boolean isCurrent;
		
		public SessionObject(Session session, T object, boolean isCurrent) {
			super();
			this.session = session;
			this.object = object;
			this.isCurrent = isCurrent;
		}
		
		public Session getSession() {
			return session;
		}
		
		public T getObject() {
			return object;
		}
		
		public boolean isCurrent() {
			return isCurrent;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T hLoad(Class<T> clazz, Serializable id, Session session) throws HibernateException {
		return (T) session.load(clazz, id);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T hGet(Class<T> clazz, Serializable id, Session session) throws HibernateException {
		return (T) session.get(clazz, id);
	}
	
	public static <T> SessionObject<T> hLoad(Class<T> clazz, Serializable id) throws HibernateException {
		return ZKUtils.hLoadOrGet(clazz, id, false, true);
	}
	
	public static <T> SessionObject<T> hGet(Class<T> clazz, Serializable id) throws HibernateException {
		return ZKUtils.hLoadOrGet(clazz, id, false, false);
	}
	
	public static <T> SessionObject<T> hLoad(Class<T> clazz, Serializable id, boolean fallback) throws HibernateException {
		return ZKUtils.hLoadOrGet(clazz, id, fallback, true);
	}
	
	public static <T> SessionObject<T> hGet(Class<T> clazz, Serializable id, boolean fallback) throws HibernateException {
		return ZKUtils.hLoadOrGet(clazz, id, fallback, false);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> SessionObject<T> hLoadOrGet(Class<T> clazz, Serializable id, boolean fallback, boolean isLoad) throws HibernateException {
		try {
			return new SessionObject<T>(
				HibernateUtil.currentSession(), 
				(T) (isLoad ? HibernateUtil.currentSession().load(clazz, id) : HibernateUtil.currentSession().get(clazz, id)),
				true
			);
		} catch (HibernateException he) {
			if (fallback) {
				Session session = null; 
				
				try {
					session = HibernateUtil.getSessionFactory().openSession();

					return new SessionObject<T>(
						session,
						(T) (isLoad ? session.load(clazz, id) : session.get(clazz, id)),
						false
					);
				} catch (HibernateException he2) {
					if (session != null) {
						if (session.getTransaction().isActive()) {
							try {
								session.getTransaction().rollback();
							} catch (HibernateException he3) {
								he3.printStackTrace();
							}
						}
					}
					
					throw he2;
				} finally {
					if (session != null) {
						if (session.getTransaction().isActive()) {
							try {
								session.getTransaction().commit();
							} catch (HibernateException he3) {
								he3.printStackTrace();
							}
						}
					}
				}
			} else {
				throw he;
			}
		}
	}

	public static Data loadDataFromMedia(Media media) throws ArchiveException, IOException,
			AbortException, Exception {
		final Reader reader;
		if (media.isBinary()) { 
			// It seems that, in Windows, big text files are uploaded as binary.
			final String fileName = media.getName().toLowerCase();
			if (fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
				reader = new InputStreamReader(media.getStreamData());
			} else {
				InputStream is;
				try {
					final CompressorStreamFactory factory = new CompressorStreamFactory();
					is = factory.createCompressorInputStream(new BufferedInputStream(media.getStreamData()));
				} catch (CompressorException ce) {
					final ArchiveStreamFactory factory = new ArchiveStreamFactory();
					is = factory.createArchiveInputStream(new BufferedInputStream(media.getStreamData()));
					
					if (((ArchiveInputStream) is).getNextEntry().isDirectory())
						throw new IOException("Invalid archive file format");
				}
				
				reader = new InputStreamReader(is);
			}
		} else {
			reader = media.getReaderData();
		}
		
		return LoadClassificationData.loadData(
			reader, media.getName(), null, null, true, null
		);
	}
}
