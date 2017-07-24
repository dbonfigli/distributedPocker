package game;

import game.PlayerState.PlayerChoiceEnum;

import java.io.Serializable;
import java.util.*;

public class PPlaying implements Serializable, Cloneable {

	private List<Integer> activeP;
	
	public PPlaying(PlayerState[] ps) {
		activeP = new ArrayList<Integer>();
		int i = 0;
		for(PlayerState pc : ps) {
			if( pc != null && pc.isOnline() &&
				pc.getTotalMoney() > 0 &&	
				!pc.getChoice().equals(PlayerChoiceEnum.FOLD))
				activeP.add(i);
			i++;
		}
	}
	
	private PPlaying() { }
	
	public PPlaying clone() {
		PPlaying ppl = new PPlaying();
		ppl.activeP = new ArrayList<Integer>();
		for(Integer i : activeP)
			ppl.activeP.add(i);
		return ppl;
	}
	
	public void makeFirst(int el) {
		List<Integer> nl = new ArrayList<Integer>();
		
		int begin = activeP.indexOf(el);
		for(int i=begin; i < begin + activeP.size(); i++)
			nl.add( activeP.get(i % activeP.size()) );
		
		activeP = nl;
	}
	
	public List<Integer> getActive() {
		return activeP;
	}
		
	
}
