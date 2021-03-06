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

package testful.coverage.fault;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;

import testful.model.faults.FaultyExecutionException;

/**
 * Represents a fault of the system.
 * It is automatically derivable from the {@link FaultyExecutionException}.
 * Faults with same message and same source (the stack trace) are assumed to be the same (they are equal).
 *
 * @author matteo
 */
public class Fault implements Serializable {

	private static final long serialVersionUID = 7235014552766544190L;

	/** Maximum length of recursion */
	private static final int MAX_STEP = 25;

	/** (recursion) Minimum number of iterations */
	private static final int MIN_ITER = 5;

	/** Ignore the first N calls in the stack (i.e., the last N methods called) */
	private static final int IGNORE_LAST = 5;

	private static final Logger logger = Logger.getLogger("testful.coverage.fault");

	private final String message;
	private final String exceptionName;
	private final StackTraceElement[] stackTrace;

	private final String causeMessage;
	private final String causeExceptionName;

	private final int hashCode;

	/**
	 * Creates a fault from the {@link FaultyExecutionException}.
	 * @param exc The exception thrown
	 * @param targetClassName the name of the class of the method being executed
	 */
	public Fault(FaultyExecutionException exc, String targetClassName) {
		message = exc.getMessage();
		exceptionName = exc.getClass().getName();
		stackTrace = processStackTrace(exc, targetClassName);

		Throwable cause = exc.getCause();

		if(cause == null) {
			causeMessage = null;
			causeExceptionName = null;
		} else {
			causeMessage= cause.getMessage();
			causeExceptionName = cause.getClass().getName();
		}

		hashCode =
			31*31*31*Arrays.hashCode(stackTrace) +
			31*31*exceptionName.hashCode() +
			31*((causeExceptionName == null) ? 0 : causeExceptionName.hashCode());
	}

	private Fault(String exceptionName, String message, StackTraceElement[] stackTrace, String causeExceptionName, String causeMessage) {
		this.exceptionName = exceptionName;
		this.message = message;
		this.stackTrace = stackTrace;
		this.causeExceptionName = causeExceptionName;
		this.causeMessage = causeMessage;

		hashCode =
			31*31*31*Arrays.hashCode(stackTrace) +
			31*31*exceptionName.hashCode() +
			31*((causeExceptionName == null) ? 0 : causeExceptionName.hashCode());
	}

	private static StackTraceElement[] processStackTrace(FaultyExecutionException fault, String baseClassName) {

		final StackTraceElement[] stackTrace;
		if(fault == null) stackTrace = null;
		else stackTrace = fault.getStackTrace();

		if(stackTrace == null || stackTrace.length == 0) {
			logger.fine("Empty StackTrace (" + fault + ")");

			// this seems to force the (sun) JVM to fill stack traces (in subsequent throws)!
			fault.fillInStackTrace();

			return new StackTraceElement[0];
		}

		int n = stackTrace.length;
		int base = 0;

		// remove initial elements in the stack
		if(baseClassName != null) {
			while(--n >= 0 && !baseClassName.equals(stackTrace[n].getClassName())) /* do nothing */;

			if(n >= 0) n++;
			else n = stackTrace.length;
		}

		// remove last elements in the stack (if they belongs to testful)
		while(base < n && (
				stackTrace[base].getClassName().startsWith("testful.")) ||
				(base+ 1 < n && stackTrace[base+ 1].getClassName().startsWith("testful.")) ||
				(base+ 2 < n && stackTrace[base+ 2].getClassName().startsWith("testful.")) ||
				(base+ 3 < n && stackTrace[base+ 3].getClassName().startsWith("testful.")) ||
				(base+ 4 < n && stackTrace[base+ 4].getClassName().startsWith("testful.")) ||
				(base+ 5 < n && stackTrace[base+ 5].getClassName().startsWith("testful.")) ||
				(base+ 6 < n && stackTrace[base+ 6].getClassName().startsWith("testful.")) ||
				(base+ 7 < n && stackTrace[base+ 7].getClassName().startsWith("testful.")) ||
				(base+ 8 < n && stackTrace[base+ 8].getClassName().startsWith("testful.")) ||
				(base+ 9 < n && stackTrace[base+ 9].getClassName().startsWith("testful.")) ||
				(base+10 < n && stackTrace[base+10].getClassName().startsWith("testful.")))
			base++;

		StackTraceElement[] pruned = new StackTraceElement[n - base];
		for(int i = base; i < n; i++)
			pruned[i-base] = stackTrace[i];

		// if there could be a loop in the stack trace
		if ( fault instanceof testful.coverage.stopper.TestStoppedException ||
				(fault instanceof testful.coverage.fault.UnexpectedExceptionException && fault.getCause() instanceof java.lang.StackOverflowError)) {

			return simplify(pruned);
		}

		return pruned;
	}

	/**
	 * Simplify the stack trace by detecting and removing the recursion
	 * @param stackTrace the stack trace to simplify
	 * @return the simplified stack trace (or the original one)
	 */
	public static StackTraceElement[] simplify(StackTraceElement[] stackTrace) {
		// for each initial point
		final int len = stackTrace.length-1;
		for (int initial = len; initial > 0 ; initial--) {

			// for each valid step
			for(int step = 1; step < MAX_STEP && (initial + 1 - MIN_ITER*step) >= 0; step++) {

				if(checkRecursion(stackTrace, initial, step)) {

					// We set as first element of the recursion the first in lexicographic order
					int first = 0;
					for(int i = 1; i < step; i++)
						if(stackTrace[initial-i].toString().compareTo(stackTrace[initial-first].toString()) < 0) first = i;

					StackTraceElement[] recursion = new StackTraceElement[step + 2];
					recursion[0] = new StackTraceElement(" --  recursion", "end  -- ", "", -1);
					for(int i = 0; i < step; i++)
						recursion[i+1] = stackTrace[(initial-first-step+i+1)];
					recursion[step+1] = new StackTraceElement(" -- recursion", "start -- ", "", -1);

					return recursion;
				}
			}
		}

		return stackTrace;
	}

	private static boolean checkRecursion(StackTraceElement[] pruned, int initial, int step) {
		int n = 1;
		int j = -1;
		for(int i = initial - step; i >= IGNORE_LAST || (i >= 0 && n < MIN_ITER) ; i--) {
			if(++j % step == 0) {
				j = 0;
				n++;
			}

			if(!pruned[i].equals( pruned[initial - j])) return false;
		}

		return n >= MIN_ITER;
	}

	/**
	 * @return the exceptionName
	 */
	public String getExceptionName() {
		return exceptionName;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the causeExceptionName
	 */
	public String getCauseExceptionName() {
		return causeExceptionName;
	}

	/**
	 * @return the causeMessage
	 */
	public String getCauseMessage() {
		return causeMessage;
	}

	/**
	 * @return the stackTrace
	 */
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	/**
	 * Calculate the hashCode of the fault, using the message of the exception and the (readapted) stack trace.
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Compare two faults using the message of the exception and the (readapted) stack trace.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;

		if (!(obj instanceof Fault)) return false;
		Fault other = (Fault) obj;

		if (!exceptionName.equals(other.exceptionName)) return false;

		if (!Arrays.equals(stackTrace, other.stackTrace)) return false;

		if (causeExceptionName == null) {
			if (other.causeExceptionName != null) return false;
		} else if (!causeExceptionName.equals(other.causeExceptionName)) return false;

		return true;
	}

	public static void write(Fault f, ObjectOutput out) throws IOException {

		out.writeUTF(f.exceptionName);
		out.writeUTF(f.message);

		out.writeShort(f.stackTrace.length);
		for (StackTraceElement st : f.stackTrace) {
			out.writeUTF(st.getClassName());
			out.writeUTF(st.getMethodName());
			out.writeUTF(st.getFileName());
			out.writeInt(st.getLineNumber());
		}

		if(f.causeExceptionName != null) {
			out.writeBoolean(true);
			out.writeUTF(f.causeExceptionName);

			if(f.causeMessage != null) {
				out.writeBoolean(true);
				out.writeUTF(f.causeMessage);
			} else {
				out.writeBoolean(false);
			}
		} else {
			out.writeBoolean(false);
		}
	}

	public static Fault read(ObjectInput in) throws IOException {
		String exceptionName = in.readUTF();
		String message = in.readUTF();

		short stackTraceLen = in.readShort();
		StackTraceElement[] stackTrace = new StackTraceElement[stackTraceLen];
		for (int i = 0; i < stackTraceLen; i++)
			stackTrace[i] = new StackTraceElement(in.readUTF(), in.readUTF(), in.readUTF(), in.readInt());

		String causeExceptionName = null;
		String causeMessage = null;
		if(in.readBoolean()) {
			causeExceptionName = in.readUTF();

			if(in.readBoolean())
				causeMessage = in.readUTF();
		}

		return new Fault(exceptionName, message, stackTrace, causeExceptionName, causeMessage);
	}
}
