package game;

import gui.MessageArea;

import java.util.*;
import java.util.Map.Entry;

public class BestGameToShow {

	HashMap<Integer, BestGame> results;
	//List<Integer> winners;
	List<List<Integer>> orderedWins;
	
	public String printResult(GameState gs) {
		
		String s = "outcome: \n";
			
		//////////////////////////
		int i = 1;
		for(List<Integer> l : orderedWins) {
			s+= i + ") ";
			for(Integer x : l)
				s += gs.playersState[x].getPeerCredential().getPeerName() + " ";
			s += "\n";
			i++;
		}
		//////////////////////////
		
		for(Entry<Integer, BestGame> e : results.entrySet()) {
			s += gs.playersState[e.getKey()].getPeerCredential().getPeerName() + " -> combination: ";
			
			switch(e.getValue().value) {
			case 0:
				s += "0) higest cards: ";
				break;
			case 1:
				s += "1) pair: ";
				break;
			case 2:
				s += "2) double pair: ";
				break;
			case 3:
				s += "3) three of a kind: ";
				break;
			case 4:
				s += "4) stright: ";
				break;
			case 5:
				s += "5) flush (color): ";
				break;
			case 6:
				s += "6) full house: ";
				break;
			case 7:
				s += "7) four of a kind (poker): ";
				break;
			case 8:
				s += "8) straight flush: ";
				break;
				
			}
			
			for(Card c : e.getValue().cards)
				s += "(" + (c.color+1) + ", " + (c.value+1) + ") ";
			s += "\n";
		}
		
		return s.substring(0, s.length()-1);
		
		
		
	}
}
