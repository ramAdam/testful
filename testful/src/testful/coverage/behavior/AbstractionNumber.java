/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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

package testful.coverage.behavior;

public class AbstractionNumber extends Abstraction {

	private static final long serialVersionUID = -6456493491208206791L;

	public static final String NEGATIVE = " < 0";
	public static final String ZERO = " = 0";
	public static final String POSITIVE = " > 0";

	public static final String P_INF = "+Inf";
	public static final String N_INF = "-Inf";
	public static final String NaN = "NaN";

	private final String label;

	public AbstractionNumber(String expression, String label) {
		super(expression);
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionNumber)) return false;
		AbstractionNumber other = (AbstractionNumber) obj;
		if(label == null) {
			if(other.label != null) return false;
		} else if(!label.equals(other.label)) return false;

		return true;
	}
}
