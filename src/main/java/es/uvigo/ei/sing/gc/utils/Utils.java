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
package es.uvigo.ei.sing.gc.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Utils {
	private Utils() {}
	
	public static <K, V> Map<V, Set<K>> invertMap(Map<K, V> map) {
		return Utils.invertMap(map, new HashMap<V, Set<K>>());
	}
	
	public static <K, V, M extends Map<V, Set<K>>> M invertMap(Map<K, V> map, M invertedMap) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (!invertedMap.containsKey(entry.getValue())) {
				invertedMap.put(entry.getValue(), new HashSet<K>());
			}
			
			invertedMap.get(entry.getValue()).add(entry.getKey());
		}
		
		return invertedMap;
	}
	
	public static class StringIgnoreCaseComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}
	}
	
	public static <T> void biSort(List<T> referenceList, List<T> list, Comparator<T> comparator) {
		for (int i = 0; i < referenceList.size() - 1; i++) {
			T refValue = referenceList.get(i);
			for (int j = i+1; j < referenceList.size(); j++) {
				T value = referenceList.get(j);
				
				
				int cmp = comparator.compare(refValue, value);
				if (cmp == 0) {
					cmp = comparator.compare(list.get(i), list.get(j));
				}
				
				if (cmp > 0) {
					referenceList.set(i, value);
					referenceList.set(j, refValue);
					
					final T tmp = list.get(i);
					list.set(i, list.get(j));
					list.set(j, tmp);
					
					refValue = value;
				}
			}
		}
	}
	
	public static <R, L> void biSort(List<R> referenceList, List<L> list, Comparator<R> comparator, Comparator<L> comparatorList) {
		for (int i = 0; i < referenceList.size() - 1; i++) {
			R refValue = referenceList.get(i);
			for (int j = i+1; j < referenceList.size(); j++) {
				R value = referenceList.get(j);
				
				
				int cmp = comparator.compare(refValue, value);
				if (cmp == 0 && comparatorList != null) {
					cmp = comparatorList.compare(list.get(i), list.get(j));
				}
				
				if (cmp < 0) {
					referenceList.set(i, value);
					referenceList.set(j, refValue);
					
					final L tmp = list.get(i);
					list.set(i, list.get(j));
					list.set(j, tmp);
					
					refValue = value;
				}
			}
		}
	}
}
