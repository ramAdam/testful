/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.coverage.whiteBox;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import testful.coverage.CoverageInformation;

public class CoverageDefExp implements CoverageInformation {
	private static final long serialVersionUID = 456863110951663193L;

	public static String KEY = "de";
	public static String NAME = "Def-Exposition";

	@Override
	public String getKey() { return KEY; }

	@Override
	public String getName() { return NAME; }

	private final Map<Stack, Set<ContextualId>> defExpo;
	private int quality;

	@Deprecated
	public CoverageDefExp() {
		defExpo = new LinkedHashMap<Stack, Set<ContextualId>>();
	}

	public CoverageDefExp(Map<Stack, Set<ContextualId>> defExpo) {
		this.defExpo = defExpo;

		updateQuality();
	}

	private void updateQuality() {
		quality = 0;
		for (Set<ContextualId> e : defExpo.values())
			quality += e.size();
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(!(other instanceof CoverageDefExp)) return false;
		final CoverageDefExp o = (CoverageDefExp) other;

		if(quality < o.quality) return false;

		for (Stack deKey : o.defExpo.keySet()) {
			Set<ContextualId> tde = defExpo.get(deKey);
			if(tde == null) return false;

			Set<ContextualId> ode = o.defExpo.get(deKey);
			if(!tde.containsAll(ode)) return false;
		}

		return true;
	}

	@Override
	public CoverageInformation createEmpty() {
		return new CoverageDefExp(new LinkedHashMap<Stack, Set<ContextualId>>());
	}

	@Override
	public float getQuality() {
		return quality;
	}

	@Override
	public CoverageDefExp clone() {
		return new CoverageDefExp(defExpo);
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other instanceof CoverageDefExp) {
			for (Entry<Stack, Set<ContextualId>> e : ((CoverageDefExp) other).defExpo.entrySet()) {
				Set<ContextualId> de = defExpo.get(e.getKey());
				if(de == null) {
					de = new LinkedHashSet<ContextualId>();
					defExpo.put(e.getKey(), de);
				}
				de.addAll(e.getValue());
			}
			updateQuality();
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<Stack, Set<ContextualId>> e : defExpo.entrySet()) {
			sb.append("stack:").append(e.getKey().toString()).append("\ndefs:").append(e.getValue().toString()).append("\n");
		}

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(quality);
		out.writeInt(defExpo.size());

		for (Entry<Stack, Set<ContextualId>> e : defExpo.entrySet()) {

			Stack.write(e.getKey(), out);
			out.writeInt(e.getValue().size());
			for (ContextualId v : e.getValue()) {
				out.writeInt(v.getId());
				Stack.write(v.getContext(), out);
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		quality = in.readInt();

		int size = in.readInt();
		for (int i = 0; i < size; i++) {

			Stack key = Stack.read(in);

			int valueSize = in.readInt();
			Set<ContextualId> value = new HashSet<ContextualId>(valueSize*3/2);
			for (int j = 0; j < valueSize; j++)
				value.add(new ContextualId(in.readInt(), Stack.read(in)));

			defExpo.put(key, value);
		}
	}
}
