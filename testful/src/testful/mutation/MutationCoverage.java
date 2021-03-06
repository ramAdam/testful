package testful.mutation;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import testful.coverage.CoverageInformation;

public class MutationCoverage implements CoverageInformation {
	private static final long serialVersionUID = 302573182673397798L;

	public static final String KEY = "mut";

	@Override
	public String getKey() {
		return KEY;
	}

	public static final String NAME = "mutation score";

	@Override
	public String getName() {
		return NAME;
	}

	private final Map<String, MutationCoverageSingle> covs;

	public MutationCoverage() {
		covs = new HashMap<String, MutationCoverageSingle>();
	}

	public void add(String name, MutationCoverageSingle cov) {
		MutationCoverageSingle add = covs.get(name);
		if(add == null) {
			add = cov.createEmpty();
			covs.put(name,add);
		}
		add.merge(cov);
	}

	@Override
	public CoverageInformation createEmpty() {
		return new MutationCoverage();
	}

	@Override
	public float getQuality() {
		float qTot = 0;
		for(MutationCoverageSingle cov : covs.values())
			qTot += cov.getQuality();

		return qTot / covs.size();
	}

	public int getKilled() {
		int killed = 0;
		for(MutationCoverageSingle cov : covs.values())
			killed += cov.getKilledNum();

		return killed;
	}

	@Override
	public boolean contains(CoverageInformation info) {
		if(info instanceof MutationCoverage) {
			MutationCoverage other = (MutationCoverage) info;

			if(covs.size() > other.covs.size()) {
				for(Entry<String, MutationCoverageSingle> e : covs.entrySet()) {
					MutationCoverageSingle otherSingle = other.covs.get(e.getKey());
					if(otherSingle != null && !e.getValue().contains(otherSingle))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void merge(CoverageInformation info) {
		if(info instanceof MutationCoverage) {
			MutationCoverage other = (MutationCoverage) info;
			for(Entry<String, MutationCoverageSingle> e : other.covs.entrySet())
				add(e.getKey(), e.getValue());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(Entry<String, MutationCoverageSingle> e : covs.entrySet())
			sb.append(e.getKey()).append(": ").append(e.getValue());

		return sb.toString();
	}

	@Override
	public MutationCoverage clone() {
		MutationCoverage ret = new MutationCoverage();

		for(Entry<String, MutationCoverageSingle> e : covs.entrySet())
			ret.covs.put(e.getKey(), e.getValue().clone());

		return ret;
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeShort(covs.size());

		for (Entry<String, MutationCoverageSingle> c : covs.entrySet()) {
			out.writeUTF(c.getKey());
			c.getValue().writeExternal(out);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int size = in.readShort();

		for (int i = 0; i < size; i++) {

			String key = in.readUTF();
			MutationCoverageSingle value = new MutationCoverageSingle();
			value.readExternal(in);

			covs.put(key, value);
		}
	}
}
