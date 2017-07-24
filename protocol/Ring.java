package protocol;

import java.util.*;

public class Ring {

	private List<Node> nodeList;
	
	public Ring(List<Node> nodeList) {
		this.nodeList = nodeList;
	}	
	
	public List<Node> getNodeList() {
		return nodeList;
	}
	
	public Node getNodeByName(String name) {
		
		for(Node n : nodeList) {
			if( name.equals(n.getPeerCredential().getPeerName()) ) {
				return n;
			}
		}
		return null;
		
	}
	
	public List<Node> activeNodes() {
		
		List<Node> result = new ArrayList<Node>();
		for(Node n : nodeList) {
			if(n.isActive())
				result.add(n);
		}
		return result;
		
	}
	
	public Set<String> crashedNodesNames() {
		
		Set<String> result = new HashSet<String>();
		for(Node n : nodeList) {
			if(!n.isActive())
				result.add(n.getPeerCredential().getPeerName());
		}
		return result;
		
	}
	
	public Node nearestActive() {
		
		Iterator<Node> it = nodeList.iterator();
		while(it.hasNext()) {
			Node n = it.next();
			if (n.isActive())
				return n;
		}
		return null;
			
	}
	
	/*
	public int nActive() {
		return activeNodes().size();
	}*/
}
