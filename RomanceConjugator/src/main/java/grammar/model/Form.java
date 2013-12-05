package grammar.model;

public interface Form {
	public FormCategory getCategory();
	
	public interface FormCategory {
		public Form[] getAllForms();
	}
}