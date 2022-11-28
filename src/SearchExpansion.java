import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * class for search expansion
 * takes a list of search terms as input and generates a csv file
 * as output with related terms from ontologies
 * @author Felicitas Loeffler
 *
 */
public class SearchExpansion {

	/**
	 * 
	 */
	private static final String GENUS = "genus";


	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger("searchExpansionLogger");
	
	
	/***********************************************
	 * GFBIO TS settings:
	 */
	
	private static final String GFBIO_TS = "https://terminologies.gfbio.org/api/terminologies";
	
	/**
	 * looks for exact, partial or approximate matches,
	 * matchType={exact|included|regex}; default:exact
	 */
	public static String matchType= "exact";
	
	/**
	 * first_hit parameter to stop at the first terminology where a match is found
	 * first_hit={true|false}; default:false
	 *
	 */
	public static boolean first_hit=false;
	
	/**
	 * Search can be limited to internal terminologies by providing the parameter internal_only
	 * internal_only={true|false}; default:false
	 */
	public static boolean internal_only=true;
	/**
	 * parameter to specify the terminologies
	 * terminologies=<terminology_name1>[, <terminology_name2>,...]
	 */
	
	/**
	 * full signature:
	 * Signature: /terminologies/search?query=<string>&terminologies=<terminology_name1>[, <terminology_name2>,...]&match_type={exact|included|regex}&first_hit={true|false}&internal_only={true|false}
	 */
	
	/************************************************
	/* Bioportal Connection
	 * 
	 */
	public static String API_KEY=""; //get an API key from Bioportal
	
	
	
	/**
	 * HashMap containing all search terms as keys and a list of Concepts as objects
	 */
	public static HashMap searchMap = new HashMap();
	public static TreeMap<String,ExpansionTerm> expansionMap =new TreeMap<String,ExpansionTerm>();
	
	
	/**
	 * mapper BioPortal API
	 */

	 final static ObjectMapper mapper = new ObjectMapper();
	 final static StringBuffer mimirquery = new StringBuffer();

	
	
	/**
	 * input: comma separated list with search terms, terms within commas are considered as one term, 
	 * e.g., quercus robur, lepidoptera
	 * output: csv file with expanded terms
	 * @param args
	 * @throws Exception 
	 */
	
	
	public static void main(String[] args) throws Exception {
		
		//set logger level
		logger.setLevel(Level.INFO);
		
		
		// Initiate a new Scanner
        Scanner userInputScanner = new Scanner(System.in);
 
        // input: user
        System.out.print("please enter a user's name the search terms belongs to: ");
        String  user = userInputScanner.nextLine();
 
        // input: search terms
        System.out.print("\nplease enter the search terms comma separated: ");
        String searchTerms_string = userInputScanner.nextLine();
 	
		//save input terms in a list
		String[] terms = searchTerms_string.split(",");
		//System.out.println("terms:"+terms.length);
		
		LinkedList<String> searchTermList = new LinkedList<String>();
		
		for(int i=0; i<terms.length;i++){
			searchTermList.add(terms[i]);
		}
		List<Concept> conceptList=null;
		
		
		
		//look for matching concepts in ontologies
		for (Iterator<String> it =searchTermList.iterator();it.hasNext();) {
			String searchterm = it.next();
			
			logger.log(Level.INFO,"******* Search for:"+searchterm+" ********");
			
			//check GFBio TS
			conceptList = findMatchingConceptsAtGFBioTS(searchterm,matchType,first_hit, internal_only );
		
			
			
			searchMap.put(searchterm, conceptList);
			
			//if nothing has been found on TS check BP
			if(conceptList == null || conceptList.size()==0){
				logger.log(Level.WARNING,"*** no concepts have been found on GFBio TS for searchterm '"+searchterm+"', please check Bioportal ***");
				
				//append at least the term itself
				if(mimirquery.length()==0)
					mimirquery.append("(");
				
				mimirquery.append("(root:"+searchterm.toLowerCase()+")");
			}
			
			//check Bioportal
			//List<Concept> conceptListBP = findMatchingConceptsAtBioportal(searchterm);
			
			if(it.hasNext())
				mimirquery.append(") AND (");
			
        }
		
		//close bracket in mimirquery
		mimirquery.append(")");
		
		logger.log(Level.WARNING, "*** Expanded query: "+ mimirquery.toString()+" ***");
		
		//Create csv file with statistics
		CSVWriter writer=null;
		try {
			writer = new CSVWriter(new FileWriter("searchExpansion_"+user+".csv"), ';');
			logger.log(Level.WARNING, "csv file created for user: "+user);
			
			BeanToCsv<ExpansionTerm> beanToCsv = new BeanToCsv<>();
		    ColumnPositionMappingStrategy<ExpansionTerm> strategy = new ColumnPositionMappingStrategy<>();
		    strategy.setType(ExpansionTerm.class);
		    String[] columns = new String[]{"ontology","uri","sourceTerm","expansionTerm","synonym","childNode","siblingNode","parentNode"};
		    strategy.setColumnMapping(columns);

		    List<ExpansionTerm> expansionList = new ArrayList<ExpansionTerm>();
		    
		    expansionList.addAll(expansionMap.values());
		    beanToCsv.write(strategy, writer, expansionList);
			
			
			 writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	    
	}

	private static List<Concept> findMatchingConceptsAtBioportal(String searchterm) throws Exception {
		String requestUrl = "https://data.bioontology.org";
		
		JsonNode annotations;
		String urlParameters;
		
		String textToAnnotate = URLEncoder.encode(searchterm, "ISO-8859-1");
		
		// Annotations with hierarchy
        //urlParameters = "max_level=3&text=" + textToAnnotate;
        //annotations = jsonToNode(get(requestUrl + "/annotator?" + urlParameters));
        //printAnnotations(annotations);

		// Get labels, synonyms, and definitions with returned annotations
        urlParameters = "text=" + textToAnnotate+"&ontologies=NCIT,OBOE-SBC,GO,GO-EXT,RH-MESH,CMO,VTO&expand_class_hierarchy=true&class_hierarchy_max_level=1";
        //,NCIT,OBOE-SBC,GO,RH-MESH
        annotations = jsonToNode(get(requestUrl + "/annotator?" + urlParameters));
       
        printAnnotations(annotations);
        
        JsonNode collection = jsonToNode(get(requestUrl+"/ontologies/GO-EXT/classes/http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2FGO_0018995/children"));
        //System.out.println("\tid: " + collection.get("@id").asText());
        /*if (collection.isArray() && collection.elements().hasNext()) {
            for (JsonNode details : collection) {
            	JsonNode classDetails = jsonToNode(get(details.get("annotatedClass").get("links").get("self").asText()));
            	System.out.println("\tprefLabel: " + details.get("collection").asText());
            }
       }*/
        
		return null;
	}
	
	private static void printAnnotations(JsonNode annotations) {
        for (JsonNode annotation : annotations) {
            // Get the details for the class that was found in the annotation and print
            JsonNode classDetails = jsonToNode(get(annotation.get("annotatedClass").get("links").get("self").asText()));
            System.out.println("Class details");
            System.out.println("\tid: " + classDetails.get("@id").asText());
            System.out.println("\tprefLabel: " + classDetails.get("prefLabel").asText());
            System.out.println("\tontology: " + classDetails.get("links").get("ontology").asText());
            System.out.println("\tparents: " + classDetails.get("links").get("parents").asText());
            System.out.println("\tchildren: " + classDetails.get("links").get("children").asText());
            System.out.println("\n");

            JsonNode hierarchy = annotation.get("hierarchy");
            // If we have hierarchy annotations, print the related class information as well
            if (hierarchy.isArray() && hierarchy.elements().hasNext()) {
                System.out.println("\tHierarchy annotations");
                for (JsonNode hierarchyAnnotation : hierarchy) {
                    classDetails = jsonToNode(get(hierarchyAnnotation.get("annotatedClass").get("links").get("self").asText()));
                    System.out.println("\t\tClass details");
                    System.out.println("\t\t\tid: " + classDetails.get("@id").asText());
                    System.out.println("\t\t\tprefLabel: " + classDetails.get("prefLabel").asText());
                    System.out.println("\t\t\tontology: " + classDetails.get("links").get("ontology").asText());
                }
            }
        }
    }
	
	private static JsonNode jsonToNode(String json) {
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

	
	private static String get(String urlToGet){
		URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
	}

	/**
	 * find a matching concept at GFBio's Terminology Server for a given search term
	 * @param searchterm
	 * @param matchType
	 * @param firstHit
	 * @param internalOnly
	 * @return
	 */
	private static List<Concept> findMatchingConceptsAtGFBioTS(String searchterm, String matchType, boolean firstHit, boolean internalOnly) {
		String searchterm_encoded = null;
		List<Concept> conceptList = new ArrayList<Concept>();
		
		try {
			searchterm_encoded = URLEncoder.encode(searchterm,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		if(searchterm_encoded!=null){
			String requestUrl = GFBIO_TS+"/search?query="+searchterm_encoded+"&match_type="+matchType+"&first_hit="+firstHit+"&internal_only="+internalOnly+"";
			logger.log(Level.INFO,"*** Get term URIs from GFBio Terminology Server, request URL: "+requestUrl+"***");
			
			
			//fetch term URI
			 CloseableHttpClient httpclient = HttpClients.createDefault();
		     try {
		            HttpGet httpget = new HttpGet(requestUrl);
		            CloseableHttpResponse response = httpclient.execute(httpget);
		            HttpEntity responseEntity = null;
		            
		            responseEntity = response.getEntity();
		            String result=null;
		    	  	if(responseEntity!=null) {
		         	 
		         	 
		             result = EntityUtils.toString(responseEntity);
		             JSONObject TSresult = new JSONObject(result);
		             logger.log(Level.INFO, result);
		             
		             JSONArray resultsArray = TSresult.getJSONArray("results");
						
						if(resultsArray!=null && resultsArray.length()>0){
							//Map<String,String> uriMap = new TreeMap();
							String sourceTerminology=null;
							String uri=null;
							String conceptLabel = null;
							String[] synonyms=null;
							long ontologyId = 0;
							
							for(int i=0; i<resultsArray.length();i++){
								
								JSONObject hit = resultsArray.getJSONObject(i);
								
								//label
								if(hit.getString("label")!=null){
									conceptLabel = hit.getString("label");
								}
								
								
								//URI & sourceTerminology
								uri = hit.getString("uri");
								
								if(hit.getString("uri").contains("ENVO"))
									sourceTerminology="ENVO";
								else if(hit.getString("uri").contains("BCO"))
									sourceTerminology="BCO";
								else if(hit.getString("uri").contains("CHEBI"))
									sourceTerminology="CHEBI";
								else if(hit.getString("uri").contains("PATO"))
									sourceTerminology="PATO";		
								else
									sourceTerminology=hit.getString("sourceTerminology");
								
								
								//ontologyId
								try{
									if(hit.getString("externalID")!=null){
										ontologyId = hit.getLong("externalID");
									}
								}
								catch(org.json.JSONException je){
									logger.log(Level.INFO,"No 'externalID' found in JSONObject for uri: "+uri+".");
								}
								
								//create a new Concept
								if (conceptLabel!=null&&uri!=null &&sourceTerminology!=null){
									Concept concept = new Concept(conceptLabel,uri,sourceTerminology);
								
									//add original search term to expansionList
									ExpansionTerm exp = new ExpansionTerm(conceptLabel,sourceTerminology,uri);
									expansionMap.put(conceptLabel,exp);
									
									//add label to StringBuffer for mimirquery
									if(mimirquery.length()==0)
										mimirquery.append("((root:"+conceptLabel.toLowerCase()+")");
									else
										mimirquery.append(" OR (root:"+conceptLabel.toLowerCase()+")");
									
									logger.log(Level.INFO,"*** new concept found in "+sourceTerminology+" ontology ***");
									
									logger.log(Level.INFO, "concept label:"+conceptLabel+", uri:"+uri);
									
									
									//add synonyms if available
									try{
										if(hit.getJSONArray("synonyms")!=null){
											JSONArray synonymArray = hit.getJSONArray("synonyms");
											logger.log(Level.INFO,"synonyms: "+synonymArray.toString());
											
											for (int j=0; j<synonymArray.length();j++){
												String synonym = synonymArray.getString(j);
												concept.getSynonyms().add(synonym);
												
												//add synonyms to expansionList
												ExpansionTerm expSyn = new ExpansionTerm(synonym,sourceTerminology,uri);
												expSyn.setSourceTerm(conceptLabel);
												expSyn.setSynonym(true);
												expansionMap.put(synonym,expSyn);
												
												//add label to StringBuffer for mimirquery
												mimirquery.append(" OR (root:"+synonym.toLowerCase()+")");
											}
											
										}
									}
									catch(org.json.JSONException je){
										logger.log(Level.INFO,"No synonyms found in JSONObject for uri: "+uri+".");
										
									}		
									conceptList.add(concept);
								}
							
							}
						}
		    	  	}
		            
		     } 
		     catch(Exception e){
		    	 e.printStackTrace();
		     }
		     finally {    	  	
		            try {
						httpclient.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		     }    
		}
		
		//check if childnodes exist
		if(conceptList!=null){
			for(Concept concept : conceptList){
				List<String> childNodes=getChildNodes(concept.getSourceTerminology(), concept.getLabel(), concept.getUri(),true,false);
				concept.setChild_nodes(childNodes);
			}
		}
	
		//get parent and sibling nodes
		conceptList = getParentAndSiblingNodes(conceptList);
		
		
		
		
		return conceptList;
	}

	/**
	 * get parent and sibling nodes if rank=genus or if no child nodes exist (node=leaf)
	 * @param conceptList
	 * @return
	 */
	private static List<Concept> getParentAndSiblingNodes(List<Concept> conceptList) {
		if(conceptList!=null){
			for(Concept concept : conceptList){
				String requestUrl = GFBIO_TS+"/"+concept.getSourceTerminology()+"/term?uri="+concept.getUri();
				logger.log(Level.INFO,"*** Get term URIs from GFBio Terminology Server, request URL: "+requestUrl+"***");
				
				//fetch term URI
				 CloseableHttpClient httpclient = HttpClients.createDefault();
			     try {
			            HttpGet httpget = new HttpGet(requestUrl);
			            CloseableHttpResponse response = httpclient.execute(httpget);
			            HttpEntity responseEntity = response.getEntity();

			    	  	if(responseEntity!=null) {		         	 			         	 
			             
			    	  		String result = EntityUtils.toString(responseEntity);
			             
			    	  		JSONObject TSresult = new JSONObject(result);
			    	  		logger.log(Level.INFO, result);
			             
			    	  		JSONArray resultsArray = TSresult.getJSONArray("results");
							
							if(resultsArray!=null && resultsArray.length()>0){
								
								for(int i=0; i<resultsArray.length();i++){								
									JSONObject hit = resultsArray.getJSONObject(i);							
									
									
									//rank
									if(hit.getString("RANK")!=null){
										concept.setRank(hit.getString("RANK"));
										
									}
									
								}
								
							}
			    	  	}
			     }
			     catch(org.json.JSONException je){
						logger.log(Level.INFO,"No rank found in JSONObject for uri: "+concept.getUri()+".");
						
					}
			 
			     catch(Exception e){
			    	 e.printStackTrace();
			     }
			     finally {    	  	
			            try {
							httpclient.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			     }
			     
			     logger.log(Level.INFO, "RANK: "+concept.getRank());
			     
			     //look for parent and sibling nodes if no child nodes can be found or search term is a genus 
			     if((concept.getRank()!=null && concept.getRank().equals(GENUS))||concept.getChild_nodes().size()==0){
			    	String broaderURI = getBroaderURIForConcept(concept.getSourceTerminology(), concept.getUri(), concept.getLabel());
			    	concept.setBroaderURI(broaderURI); 
			    	
			    	//get synonyms for broader term
			    	List<String> synonymsParent = getSynonymsForBroaderTerm(concept.getSourceTerminology(),concept.getBroaderURI(), concept.getLabel());
			    	concept.setSynonyms_parentNode(synonymsParent);
			    	
			    	//get childNodes for broader term
				     List<String> children = getChildNodes(concept.getSourceTerminology(),concept.getLabel(), concept.getBroaderURI(),false,true);
				     concept.setSibling_nodes(children);
				     
				     //get synonyms for sibling Nodes
				     List<String> synonymsSiblings = getSynonymsForChildNodes(concept.getSourceTerminology(),concept.getLabel(),concept.getSibling_nodes(),false,true);
				     concept.setSynonyms_siblingNodes(synonymsSiblings);
				     
				     //remove redundant data 
				     //childnodes for a genus are already in childNode-List
				     if(concept.getSibling_nodes().size()>0){
				    	 
				    	 List<String> childNodes = concept.getChild_nodes();
				    	 List<String> siblingNodes = concept.getSibling_nodes();
				    	 				    	 
				    	 logger.log(Level.INFO,"siblingNodes size: "+siblingNodes.size()+".");
				    	 
				    	 for(String child: childNodes){
				    		  
				    		 if(siblingNodes.contains(child)){			    			 
				    			 siblingNodes.remove(child);
				    		 }			    	
				    	 }
				    	 logger.log(Level.INFO,"siblingNodes size: "+siblingNodes.size()+".");
				    	 
				    	 concept.setSibling_nodes(siblingNodes);
				     }
				     
			     }
			     
			}
		}
		return conceptList;
	}

	private static List<String> getSynonymsForChildNodes(
			String sourceTerminology, String conceptLabel, List<String> child_or_siblings,
			boolean childNodes, boolean siblingNodes) {
		
		List<String> synonyms=null;
		
		if(child_or_siblings!=null){
			
			for(String entityURI : child_or_siblings){
				
				synonyms = getSynonymsForTerm(sourceTerminology,entityURI);
				
				if(synonyms!=null && synonyms.size()>0){
					for(String synonym : synonyms){
					
						ExpansionTerm exp = new ExpansionTerm(synonym, sourceTerminology, entityURI);
						exp.setSynonym(true);
						exp.setChildNode(childNodes);
						exp.setSiblingNode(siblingNodes);
						expansionMap.put(synonym, exp);
						
						//add label to StringBuffer for mimirquery
						mimirquery.append(" OR (root:"+synonym.toLowerCase()+")");
					}
				
				}
			}
			
			
		}
		
		
		return synonyms;
	}

	private static List<String> getSynonymsForTerm(String sourceTerminology,
			String entityURI) {
		
		List<String> synonyms = null;
		
		//get synonyms for broader term
	     String requestUrl = GFBIO_TS+"/"+sourceTerminology+"/term?uri="+entityURI;
			logger.log(Level.INFO,"*** Get term URIs from GFBio Terminology Server, request URL: "+requestUrl+"***");
			
			//fetch term URI
			 CloseableHttpClient httpclient = HttpClients.createDefault();
		     try {
		            HttpGet httpget = new HttpGet(requestUrl);
		            CloseableHttpResponse response = httpclient.execute(httpget);
		            HttpEntity responseEntity = response.getEntity();

		    	  	if(responseEntity!=null) {		         	 			         	 
		             
		    	  		String result = EntityUtils.toString(responseEntity);
		             
		    	  		JSONObject TSresult = new JSONObject(result);
		    	  		logger.log(Level.INFO, result);
		             
		    	  		JSONArray resultsArray = TSresult.getJSONArray("results");
						
						if(resultsArray!=null && resultsArray.length()>0){
							
							for(int i=0; i<resultsArray.length();i++){								
								JSONObject hit = resultsArray.getJSONObject(i);	
								
								try{
									if(hit.getString("synonyms")!=null){
										String synonym = hit.getString("synonyms");									
										synonyms.add(synonym);
									}
								}
								catch(Exception e){
									logger.log(Level.INFO, "No synonyms found for requestURL: "+requestUrl);
								}
								
								
								try{
									if(hit.getJSONArray("synonyms")!=null){
										synonyms=new ArrayList<String>();
										JSONArray synonymArray = hit.getJSONArray("synonyms");
										
										for (int j=0; j<synonymArray.length();j++){
											String synonym = synonymArray.getString(j);
										
											synonyms.add(synonym);
											
										}
									
									}
								}
								 catch(Exception e){
									 logger.log(Level.INFO, "No synonyms found for requestURL: "+requestUrl);
								  }
							}
						}
		    	  	}
		     }
		     catch(Exception e){
		    	e.printStackTrace();
		     }
		     finally {    	  	
	           try {
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		     } 
		
		
		return synonyms;
	
	}

	private static List<String> getSynonymsForBroaderTerm(
			String sourceTerminology, String broaderURI, String conceptLabel) {
		
		List<String> synonymsParentNode = null;
		
		//get synonyms for broader term
	     String requestUrl = GFBIO_TS+"/"+sourceTerminology+"/term?uri="+broaderURI;
			logger.log(Level.INFO,"*** Get term URIs from GFBio Terminology Server, request URL: "+requestUrl+"***");
			
			//fetch term URI
			 CloseableHttpClient httpclient = HttpClients.createDefault();
		     try {
		            HttpGet httpget = new HttpGet(requestUrl);
		            CloseableHttpResponse response = httpclient.execute(httpget);
		            HttpEntity responseEntity = response.getEntity();

		    	  	if(responseEntity!=null) {		         	 			         	 
		             
		    	  		String result = EntityUtils.toString(responseEntity);
		             
		    	  		JSONObject TSresult = new JSONObject(result);
		    	  		logger.log(Level.INFO, result);
		             
		    	  		JSONArray resultsArray = TSresult.getJSONArray("results");
						
						if(resultsArray!=null && resultsArray.length()>0){
							
							for(int i=0; i<resultsArray.length();i++){								
								JSONObject hit = resultsArray.getJSONObject(i);	
								
								try{
									if(hit.getString("synonyms")!=null){
										String synonym = hit.getString("synonyms");
										
										ExpansionTerm exp = new ExpansionTerm(synonym,sourceTerminology,broaderURI);
										exp.setParentNode(true);
										exp.setSourceTerm(conceptLabel);
										exp.setSynonym(true);
										expansionMap.put(synonym,exp);
										
										//add label to StringBuffer for mimirquery
										mimirquery.append(" OR (root:"+synonym.toLowerCase()+")");
										
										synonymsParentNode.add(synonym);
									}
								}
								catch(Exception e){
									logger.log(Level.INFO, "No synonyms found for requestURL: "+requestUrl);
								}
								
								
								if(hit.getJSONArray("synonyms")!=null){
									synonymsParentNode=new ArrayList<String>();
									JSONArray synonymArray = hit.getJSONArray("synonyms");
									
									for (int j=0; j<synonymArray.length();j++){
										String synonym = synonymArray.getString(j);
									
										ExpansionTerm exp = new ExpansionTerm(synonym,sourceTerminology,broaderURI);
										exp.setParentNode(true);
										exp.setSourceTerm(conceptLabel);
										exp.setSynonym(true);
										expansionMap.put(synonym,exp);
										
										//add label to StringBuffer for mimirquery
										mimirquery.append(" OR (root:"+synonym.toLowerCase()+")");
										
										synonymsParentNode.add(synonym);
										
									}
								
								}
							}
						}
		    	  	}
		     }
		     catch(Exception e){
		    	e.printStackTrace();
		     }
		     finally {    	  	
	           try {
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		     } 
		
		
		return synonymsParentNode;
	}

	private static String getBroaderURIForConcept(String sourceTerminology, String conceptURI, String conceptLabel) {
		String requestUrl = GFBIO_TS+"/"+sourceTerminology+"/broader?uri="+conceptURI;
		logger.log(Level.INFO,"*** Get term URIs from GFBio Terminology Server, request URL: "+requestUrl+"***");
		
		//fetch term URI
		 CloseableHttpClient httpclient = HttpClients.createDefault();
	     try {
	            HttpGet httpget = new HttpGet(requestUrl);
	            CloseableHttpResponse response = httpclient.execute(httpget);
	            HttpEntity responseEntity = response.getEntity();

	    	  	if(responseEntity!=null) {		         	 			         	 
	             
	    	  		String result = EntityUtils.toString(responseEntity);
	             
	    	  		JSONObject TSresult = new JSONObject(result);
	    	  		logger.log(Level.INFO, result);
	             
	    	  		JSONArray resultsArray = TSresult.getJSONArray("results");
					
					if(resultsArray!=null && resultsArray.length()>0){
						
						for(int i=0; i<resultsArray.length();i++){								
							JSONObject hit = resultsArray.getJSONObject(i);	
							
							
							if(hit.getString("broaderuri")!=null){
								ExpansionTerm exp = new ExpansionTerm(hit.getString("broaderlabel"),sourceTerminology,hit.getString("broaderuri"));
								exp.setParentNode(true);
								exp.setSourceTerm(conceptLabel);
								expansionMap.put(hit.getString("broaderlabel"),exp);
								
								//add label to StringBuffer for mimirquery
								mimirquery.append(" OR (root:"+hit.getString("broaderlabel").toLowerCase()+")");
								
								return hit.getString("broaderuri");
							}
						}
					}
	    	  	}
	     }
	     catch(Exception e){
	    	e.printStackTrace();
	     }
	     finally {    	  	
           try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     } 
			
	     
	     
	    	
		return null;
	}

	/**
	 * check if childnodes are available, if yes, childnodes will be added as a list to the concept
	 * @param conceptList
	 * @return
	 */
	private static List<String> getChildNodes(String sourceTerminology, String sourceLabel, String conceptURI, boolean childNode, boolean sibling) {
		
		List<String> children = new ArrayList<String>();
		String requestUrl = GFBIO_TS+"/"+sourceTerminology+"/allnarrower?uri="+conceptURI;
		logger.log(Level.INFO,"*** Get term URIs from GFBio Terminology Server, request URL: "+requestUrl+"***");
		
		//fetch term URI
		 CloseableHttpClient httpclient = HttpClients.createDefault();
	     try {
	            HttpGet httpget = new HttpGet(requestUrl);
	            CloseableHttpResponse response = httpclient.execute(httpget);
	            HttpEntity responseEntity = null;
	            
	            responseEntity = response.getEntity();
	            String result=null;
	    	  	if(responseEntity!=null) {
	         	 
	         	 
	             result = EntityUtils.toString(responseEntity);
	             JSONObject TSresult = new JSONObject(result);
	             logger.log(Level.INFO, result);
	             
	             JSONArray resultsArray = TSresult.getJSONArray("results");
					
					if(resultsArray!=null && resultsArray.length()>0){
						for(int i=0; i<resultsArray.length();i++){
							String narrowerLabel = null;
							String narrowerURI = null;
							
							JSONObject hit = resultsArray.getJSONObject(i);

							//narroweruri
							if(hit.getString("narroweruri")!=null){
								narrowerURI=hit.getString("narroweruri");
							}
							//narrowerlabel
							if(hit.getString("narrowerlabel")!=null){
								narrowerLabel=hit.getString("narrowerlabel");
							}
							
							if(narrowerLabel!=null && narrowerURI!=null){
								children.add(narrowerURI);
								ExpansionTerm exp = new ExpansionTerm(narrowerLabel, sourceTerminology, narrowerURI);
								exp.setSourceTerm(sourceLabel);
								exp.setChildNode(childNode);
								exp.setSiblingNode(sibling);
								
								//childnodes have priority and are not allowed to be overwritten!
								if(!expansionMap.keySet().contains(narrowerLabel)){
									expansionMap.put(narrowerLabel,exp);
									
									String[] narrArray = narrowerLabel.split(" ");
									
									//check if first part of the narrowerLabel is a concept
									//species names consist of genus + specific naming
									//can be skipped for mimirquery since they will be found with the genus anyway
									if(narrArray.length>1){
										String firstPartOfNarrowerLabel = narrArray[0];
										
										if(!expansionMap.keySet().contains(firstPartOfNarrowerLabel) 
												&& !firstPartOfNarrowerLabel.equals("unclassified")){
											
											
										/*	
											Pattern p = Pattern.compile("[0-9]+");
											Matcher m = p.matcher(narrowerLabel);
											boolean b = m.matches();
											System.out.println("Matcher: "+b);*/
											
											//add label to StringBuffer for mimirquery
											mimirquery.append(" OR (root:"+narrowerLabel.toLowerCase()+")");
										}
									}else{
										mimirquery.append(" OR (root:"+narrowerLabel.toLowerCase()+")");
									}
									
									
								}
								
								
							}
						}
						
					}
	    	  	}
	     }
	     catch(Exception e){
	    	 e.printStackTrace();
	     }
	     finally {    	  	
	            try {
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	     }   
					
				
		
		return children;
	
	}
}
