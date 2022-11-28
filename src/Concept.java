import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class Concept {
	
	String label;
	String uri;
	String sourceTerminology;
	String rank;
	String broaderURI;
	
	List<String> synonyms;
	List<String> child_nodes;
	List<String> sibling_nodes;
	List<String> synonyms_parentNode;
	List<String> synonyms_siblingNodes;
	List<String> synonyms_childNodes;
	
	

	
	public Concept(){
		
	}
	public Concept(String label, String uri, String sourceTerminology){
		this.label=label;
		this.uri=uri;
		this.sourceTerminology=sourceTerminology;
		this.rank=null;
		this.broaderURI=null;
		synonyms=new ArrayList<String>();
		child_nodes=new ArrayList<String>();
		sibling_nodes=new ArrayList<String>();
		
		synonyms_parentNode=new ArrayList<String>();
		synonyms_childNodes=new ArrayList<String>();
		synonyms_siblingNodes=new ArrayList<String>();
	}
	
	
	
	
	public List<String> getSynonyms_parentNode() {
		return synonyms_parentNode;
	}
	public void setSynonyms_parentNode(List<String> synonyms_parentNode) {
		this.synonyms_parentNode = synonyms_parentNode;
	}
	public List<String> getSynonyms_siblingNodes() {
		return synonyms_siblingNodes;
	}
	public void setSynonyms_siblingNodes(List<String> synonyms_siblingNodes) {
		this.synonyms_siblingNodes = synonyms_siblingNodes;
	}
	public List<String> getSynonyms_childNodes() {
		return synonyms_childNodes;
	}
	public void setSynonyms_childNodes(List<String> synonyms_childNodes) {
		this.synonyms_childNodes = synonyms_childNodes;
	}
	
	public List<String> getSibling_nodes() {
		return sibling_nodes;
	}
	public void setSibling_nodes(List<String> sibling_nodes) {
		this.sibling_nodes = sibling_nodes;
	}
	public String getBroaderURI() {
		return broaderURI;
	}
	public void setBroaderURI(String broaderURI) {
		this.broaderURI = broaderURI;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public List<String> getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}
	public String getSourceTerminology() {
		return sourceTerminology;
	}
	public void setSourceTerminology(String sourceTerminology) {
		this.sourceTerminology = sourceTerminology;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUriEncoded() throws UnsupportedEncodingException{
		return URLEncoder.encode(uri, "UTF-8");
	}
	public List<String> getChild_nodes() {
		return child_nodes;
	}
	public void setChild_nodes(List<String> child_nodes) {
		this.child_nodes = child_nodes;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	

}

