

public class ExpansionTerm {
	
	String expansionTerm;
	String ontology;
	String sourceTerm;
	String uri;
	boolean parentNode;
	
	
	
	boolean synonym;
	boolean childNode;
	boolean siblingNode;
	
	
	public ExpansionTerm(String label, String sourceTerminology, String uri){
		this.expansionTerm=label;		
		this.ontology=sourceTerminology;
		this.sourceTerm = null;
		this.uri =uri;
		this.synonym=false;
		this.childNode=false;
		this.siblingNode = false;
		this.parentNode=false;

	}
	
	
	
	public ExpansionTerm(String label, String sourceTerminology, String sourceTerm, String uri, boolean synonym, boolean childnode, boolean sibling,boolean parentnode){
		this.expansionTerm=label;		
		this.ontology=sourceTerminology;
		this.sourceTerm = sourceTerm;
		this.uri =uri;
		this.synonym=synonym;
		this.childNode=childnode;
		this.siblingNode = sibling;
		this.parentNode=parentnode;
		
	}
	
	
	public boolean isParentNode() {
		return parentNode;
	}



	public void setParentNode(boolean parentNode) {
		this.parentNode = parentNode;
	}

	public boolean isSiblingNode() {
		return siblingNode;
	}

	public void setSiblingNode(boolean siblingNode) {
		this.siblingNode = siblingNode;
	}

	

	public boolean isChildNode() {
		return childNode;
	}

	public void setChildNode(boolean childnode) {
		this.childNode = childnode;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getSourceTerm() {
		return sourceTerm;
	}
	public void setSourceTerm(String sourceTerm) {
		this.sourceTerm = sourceTerm;
	}

	public String getExpansionTerm() {
		return expansionTerm;
	}
	public void setExpansionTerm(String expansionTerm) {
		this.expansionTerm = expansionTerm;
	}
	public String getOntology() {
		return ontology;
	}
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}
	public boolean isSynonym() {
		return synonym;
	}
	public void setSynonym(boolean synonym) {
		this.synonym = synonym;
	}

	

}

