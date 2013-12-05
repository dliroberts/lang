package grammar.model;

import java.util.HashMap;
import java.util.Map;

public class ImpersonalForm implements Form {
	private static final Map<ImpersonalFormCategory, ImpersonalForm> INSTANCES =
		new HashMap<ImpersonalFormCategory, ImpersonalForm>();

	private final ImpersonalFormCategory category;
	
	public String toString() {
		return getCategory().toString();
	}
	
	private ImpersonalForm(ImpersonalFormCategory category) {
		this.category = category;
		INSTANCES.put(category, this);
	}
	
	public static ImpersonalForm getInstance(ImpersonalFormCategory category) {
		ImpersonalForm impf = INSTANCES.get(category);
		if (impf == null)
			return new ImpersonalForm(category);
		else
			return impf;
	}

	public ImpersonalFormCategory getCategory() {
		return category;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ImpersonalForm))
			return false;
		final ImpersonalForm other = (ImpersonalForm) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		return true;
	}

	public static class ImpersonalFormCategory implements FormCategory, Comparable<ImpersonalFormCategory> {
		private final String identifier;
		
		public String toString() {
			return identifier;
		}
		
		public Form[] getAllForms() {
			return new Form[] {ImpersonalForm.getInstance(this)};
		}
		
		public ImpersonalFormCategory(String identifier) {
			this.identifier = identifier;
		}

		public String getIdentifier() {
			return identifier;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((identifier == null) ? 0 : identifier.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ImpersonalFormCategory))
				return false;
			final ImpersonalFormCategory other = (ImpersonalFormCategory) obj;
			if (identifier == null) {
				if (other.identifier != null)
					return false;
			} else if (!identifier.equals(other.identifier))
				return false;
			return true;
		}

		public int compareTo(ImpersonalFormCategory o) {
			return identifier.compareTo(o.identifier);
		}
	}
}