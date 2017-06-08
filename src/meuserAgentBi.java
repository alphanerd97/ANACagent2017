import java.util.HashMap;
import java.util.List;

import negotiator.Agent;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.boaframework.BOAagent;
import negotiator.boaframework.OutcomeSpace;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.protocol.MultilateralProtocol;
import negotiator.protocol.StackedAlternatingOffersProtocol;

/*
 * @author Paul Meuser
 * Multilateral negotiating agent for 2017 ANAC competition
 * Version 1.0
 */

public class meuserAgentBi extends BOAagent{
	private OutcomeSpace bidPool;
	private Action mostRecentAction = null;
	private Bid mostRecentBid = null;
	private Bid nextBid = null;
	private double maxUtil;
	
	
	
	public meuserAgentBi() {
		super();
	}
	
	@Override
	public  void  init() {
		
		//determine available bidding space, set next bid with max utility and record max utility
		bidPool = outcomeSpace;
		nextBid = bidPool.getMaxBidPossible().getBid();
		maxUtil = getUtility(nextBid);
		
		mostRecentAction = null;
		
	}
	
	@Override
	public Action chooseAction() {
		Action output;
		//for first bid, present bid with maximum possible utility.
		if (mostRecentAction == null) {
			output = new Offer(getAgentID(), nextBid);
		}
		else if (mostRecentAction instanceof Offer) {
			updateNextBid();
			//accept bids which exceed expected utility of next bid.
			if (getUtility(mostRecentBid) >= getUtility(nextBid)) {
				output = new Accept(getAgentID(),mostRecentBid);
			}
			//otherwise offer next bid
			else {
				output = new Offer(getAgentID(), nextBid);
			}
		}
		else if (mostRecentAction instanceof Accept) {
			updateNextBid();
			//accept bids which exceed half expected utility of next bid.
			if (getUtility(mostRecentBid) >= getUtility(nextBid)*2/3) {
				output = new Accept(getAgentID(), mostRecentBid);
			}
			//otherwise offer next bid
			else {
				output = new Offer(getAgentID(), nextBid);
			}
		}
		else {
			updateNextBid();
			output =  new Offer(getAgentID(),nextBid);
		}
		
		return output;
	}
	
	//Determines next bid by conceding utility at increasing rate as deadline approaches.
	private void updateNextBid() {	
		//find the current time as fraction of total time available
		double now = timeline.getTime();
		//generate next expected utility based on current time
		double util = maxUtil * (1.0-Math.exp(5.0*(now-1.0)));
		//generate bid using expected utility
		nextBid = bidPool.getBidNearUtility(util).getBid();
	}
	
	
	public void receiveMessage(Action action ) {
		//collect most recent action, and bid
		mostRecentAction = action;
		if (mostRecentAction instanceof Offer) {
			mostRecentBid = ((Offer) mostRecentAction).getBid();
		}
		if (mostRecentAction instanceof Accept) {
			mostRecentBid = ((Accept) mostRecentAction).getBid();			
		}
	}
	

	@Override
	public void agentSetup() {
		init();
		
	}

	@Override
	public String getName() {
		return "Paul Meuser ANAC Agent";
	}
}
